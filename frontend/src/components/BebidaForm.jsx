import { useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Spinner } from "react-bootstrap";
import { actualizarBebida, crearBebida } from "../services/api.js";

// Valores del formulario como texto: asi se guardan en los inputs.
function valoresIniciales(bebida) {
  const texto = (valor) => (valor == null ? "" : String(valor));
  return {
    nombre: bebida?.nombre ?? "",
    tipo: bebida?.tipo ?? "ALCOHOLICA",
    volumenML: texto(bebida?.volumenML),
    stock: texto(bebida?.stock),
    gradosAlcohol: texto(bebida?.gradosAlcohol),
    certificada: bebida?.certificada ?? false,
    azucarPorLitro: texto(bebida?.azucarPorLitro),
  };
}

// Campo vacio -> null, para que el backend responda "es obligatorio".
const numero = (texto) => (texto.trim() === "" ? null : Number(texto));

/**
 * Crea (bebida = null) o edita una bebida. No valida por su cuenta: envia
 * los datos y muestra bajo cada campo los errores del 400 del backend.
 */
export default function BebidaForm({ bebida = null, onGuardada, onCancelar }) {
  const editando = bebida !== null;
  const [valores, setValores] = useState(() => valoresIniciales(bebida));
  const [errores, setErrores] = useState({});   // { campo: mensaje } del 400
  const [errorGeneral, setErrorGeneral] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const esAlcoholica = valores.tipo === "ALCOHOLICA";

  // Actualiza un campo y borra su error: el usuario ya lo esta corrigiendo.
  function cambiar(campo, valor) {
    setValores((v) => ({ ...v, [campo]: valor }));
    setErrores(({ [campo]: _, ...resto }) => resto);
  }

  // Los atributos del otro tipo van null: estan ocultos y no deben enviarse.
  function armarDatos() {
    return {
      nombre: valores.nombre,
      tipo: valores.tipo,
      volumenML: numero(valores.volumenML),
      stock: numero(valores.stock),
      gradosAlcohol: esAlcoholica ? numero(valores.gradosAlcohol) : null,
      certificada: esAlcoholica ? valores.certificada : null,
      azucarPorLitro: esAlcoholica ? null : numero(valores.azucarPorLitro),
    };
  }

  async function guardar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErrores({});
    setErrorGeneral(null);
    try {
      const datos = armarDatos();
      const guardada = editando
        ? await actualizarBebida(bebida.id, datos)
        : await crearBebida(datos);
      onGuardada(guardada, !editando);
    } catch (e) {
      // 400: errores por campo. Otro error (404, sin conexion): mensaje general.
      if (Object.keys(e.campos ?? {}).length > 0) {
        setErrores(e.campos);
      }
      setErrorGeneral(e.message);
    } finally {
      setEnviando(false);
    }
  }

  // Props comunes de un campo: valor, cambio y marca de error.
  const campo = (nombre) => ({
    value: valores[nombre],
    onChange: (e) => cambiar(nombre, e.target.value),
    isInvalid: Boolean(errores[nombre]),
  });

  return (
    <Card className="mb-4">
      <Card.Header as="h2" className="h5">
        {editando ? `Editar: ${bebida.nombre}` : "Nueva bebida"}
      </Card.Header>
      <Card.Body>
        {errorGeneral && <Alert variant="danger">{errorGeneral}</Alert>}

        {/* noValidate: la validacion la hace el backend, no el navegador. */}
        <Form noValidate onSubmit={guardar}>
          <Row className="g-3">
            <Form.Group as={Col} md={6} controlId="bebida-nombre">
              <Form.Label>Nombre</Form.Label>
              <Form.Control {...campo("nombre")} />
              <Form.Control.Feedback type="invalid">{errores.nombre}</Form.Control.Feedback>
            </Form.Group>

            <Form.Group as={Col} md={6} controlId="bebida-tipo">
              <Form.Label>Tipo</Form.Label>
              <Form.Select {...campo("tipo")}>
                <option value="ALCOHOLICA">Alcohólica</option>
                <option value="SIN_ALCOHOL">Sin alcohol</option>
              </Form.Select>
              <Form.Control.Feedback type="invalid">{errores.tipo}</Form.Control.Feedback>
            </Form.Group>

            <Form.Group as={Col} md={3} controlId="bebida-volumen">
              <Form.Label>Volumen (ml)</Form.Label>
              <Form.Control type="number" {...campo("volumenML")} />
              <Form.Control.Feedback type="invalid">{errores.volumenML}</Form.Control.Feedback>
            </Form.Group>

            <Form.Group as={Col} md={3} controlId="bebida-stock">
              <Form.Label>Stock</Form.Label>
              <Form.Control type="number" {...campo("stock")} />
              <Form.Control.Feedback type="invalid">{errores.stock}</Form.Control.Feedback>
            </Form.Group>

            {esAlcoholica ? (
              <>
                <Form.Group as={Col} md={3} controlId="bebida-grados">
                  <Form.Label>Grados de alcohol</Form.Label>
                  <Form.Control type="number" step="0.1" {...campo("gradosAlcohol")} />
                  <Form.Control.Feedback type="invalid">{errores.gradosAlcohol}</Form.Control.Feedback>
                </Form.Group>

                <Form.Group as={Col} md={3} controlId="bebida-certificada" className="d-flex align-items-end">
                  <Form.Check
                    type="switch"
                    label="Certificada por el proveedor"
                    checked={valores.certificada}
                    onChange={(e) => cambiar("certificada", e.target.checked)}
                    isInvalid={Boolean(errores.certificada)}
                    feedback={errores.certificada}
                    feedbackType="invalid"
                  />
                </Form.Group>
              </>
            ) : (
              <Form.Group as={Col} md={6} controlId="bebida-azucar">
                <Form.Label>Azúcar (g/L)</Form.Label>
                <Form.Control type="number" {...campo("azucarPorLitro")} />
                <Form.Control.Feedback type="invalid">{errores.azucarPorLitro}</Form.Control.Feedback>
              </Form.Group>
            )}
          </Row>

          <div className="mt-3 d-flex gap-2">
            <Button type="submit" disabled={enviando}>
              {enviando && <Spinner animation="border" size="sm" className="me-2" />}
              {editando ? "Guardar cambios" : "Crear bebida"}
            </Button>
            {editando && (
              <Button variant="outline-secondary" onClick={onCancelar} disabled={enviando}>
                Cancelar
              </Button>
            )}
          </div>
        </Form>
      </Card.Body>
    </Card>
  );
}
