import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Spinner } from "react-bootstrap";
import { listarBebidas, registrarVenta } from "../services/api.js";
import { pesos, textoMotivo } from "../utils/formato.js";

// Campo vacio -> null, para que el backend responda "es obligatorio".
const numero = (texto) => (texto === "" ? null : Number(texto));

/**
 * Registra una venta. No decide nada: envia bebida y unidades, y muestra el
 * total que calculo el backend o el motivo del rechazo (409).
 *
 * Props:
 * - version: al cambiar, se vuelven a pedir las bebidas (el stock pudo cambiar).
 * - onIntento(): se llama despues de cada venta, autorizada o rechazada,
 *   porque las dos quedan registradas y cambian el historial.
 */
export default function VentaForm({ version = 0, onIntento }) {
  const [bebidas, setBebidas] = useState([]);
  const [errorCarga, setErrorCarga] = useState(null);
  const [bebidaId, setBebidaId] = useState("");
  const [unidades, setUnidades] = useState("1");
  const [errores, setErrores] = useState({});     // { campo: mensaje } del 400
  const [resultado, setResultado] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    let vigente = true;
    setErrorCarga(null);
    listarBebidas()
      .then((datos) => vigente && setBebidas(datos))
      .catch((e) => vigente && setErrorCarga(e.message));
    return () => {
      vigente = false;
    };
  }, [version]);

  async function vender(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErrores({});
    setResultado(null);
    try {
      const venta = await registrarVenta(numero(bebidaId), numero(unidades));
      setResultado({
        variante: "success",
        titulo: "Venta autorizada",
        texto: `${venta.unidades} × ${venta.nombre}. Total cobrado: ${pesos(venta.total)}.`,
      });
      // Venta exitosa: el formulario vuelve a su estado inicial para la siguiente.
      // En un 409 no se limpia, para que se pueda corregir y reintentar.
      setBebidaId("");
      setUnidades("1");
      onIntento();
    } catch (e) {
      if (e.status === 409) {
        // Venta rechazada: quedo guardada en el historial con su motivo.
        setResultado({
          variante: "warning",
          titulo: `Venta rechazada: ${textoMotivo(e.codigo)}`,
          texto: e.message,
        });
        onIntento();
      } else {
        // 400 (campos), 404 (bebida eliminada) o sin conexion.
        setErrores(e.campos ?? {});
        setResultado({ variante: "danger", titulo: "No se pudo registrar la venta", texto: e.message });
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Card className="mb-4">
      <Card.Header as="h2" className="h5">Registrar venta</Card.Header>
      <Card.Body>
        {errorCarga && (
          <Alert variant="danger">No se pudieron cargar las bebidas: {errorCarga}</Alert>
        )}

        {resultado && (
          <Alert variant={resultado.variante} dismissible onClose={() => setResultado(null)}>
            <strong>{resultado.titulo}.</strong> {resultado.texto}
          </Alert>
        )}

        <Form noValidate onSubmit={vender}>
          <Row className="g-3 align-items-start">
            <Form.Group as={Col} md={7} controlId="venta-bebida">
              <Form.Label>Bebida</Form.Label>
              <Form.Select
                value={bebidaId}
                onChange={(e) => setBebidaId(e.target.value)}
                isInvalid={Boolean(errores.bebidaId)}
              >
                <option value="">Selecciona una bebida…</option>
                {/* Informa precio, stock y restriccion, pero no oculta ni bloquea nada:
                    si la venta no procede, lo dice el backend. */}
                {bebidas.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.nombre} ({b.tipo === "ALCOHOLICA" ? "alcohólica" : "sin alcohol"})
                    {" — "}{pesos(b.precio)} · stock {b.stock}
                    {b.ventaRestringida ? " · restringida" : ""}
                  </option>
                ))}
              </Form.Select>
              <Form.Control.Feedback type="invalid">{errores.bebidaId}</Form.Control.Feedback>
            </Form.Group>

            <Form.Group as={Col} md={2} controlId="venta-unidades">
              <Form.Label>Unidades</Form.Label>
              <Form.Control
                type="number"
                value={unidades}
                onChange={(e) => setUnidades(e.target.value)}
                isInvalid={Boolean(errores.unidades)}
              />
              <Form.Control.Feedback type="invalid">{errores.unidades}</Form.Control.Feedback>
            </Form.Group>

            <Col md={3} className="d-flex align-items-end" style={{ minHeight: "4.5rem" }}>
              <Button type="submit" disabled={enviando} className="w-100">
                {enviando && <Spinner animation="border" size="sm" className="me-2" />}
                Vender
              </Button>
            </Col>
          </Row>
        </Form>
      </Card.Body>
    </Card>
  );
}
