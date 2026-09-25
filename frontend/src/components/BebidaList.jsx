import { useEffect, useState } from "react";
import { Alert, Badge, Button, Form, InputGroup, Spinner, Table } from "react-bootstrap";
import { eliminarBebida, listarBebidas, restringirVenta } from "../services/api.js";

const TIPOS = { ALCOHOLICA: "Alcohólica", SIN_ALCOHOL: "Sin alcohol" };

// Solo formatea el numero que calculo el backend: 4200 -> "$4.200".
const pesos = (valor) => valor.toLocaleString("es-CL", { style: "currency", currency: "CLP" });

/**
 * Tabla del catalogo con filtro por nombre. El filtro lo aplica la API
 * (?nombre=), no este componente. Tambien permite restringir y eliminar.
 */
export default function BebidaList() {
  const [bebidas, setBebidas] = useState([]);
  const [texto, setTexto] = useState("");     // lo que se esta escribiendo
  const [nombre, setNombre] = useState("");   // el filtro ya enviado a la API
  const [recarga, setRecarga] = useState(0);  // cambiarlo vuelve a pedir la lista
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);   // fallo al cargar la lista
  const [aviso, setAviso] = useState(null);   // resultado de eliminar o restringir
  const [procesandoId, setProcesandoId] = useState(null);

  useEffect(() => {
    // Si el efecto se vuelve a ejecutar antes de que llegue la respuesta,
    // la respuesta vieja se descarta para no pisar a la nueva.
    let vigente = true;
    setCargando(true);
    setError(null);
    listarBebidas(nombre)
      .then((datos) => vigente && setBebidas(datos))
      .catch((e) => vigente && setError(e.message))
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
  }, [nombre, recarga]);

  const recargar = () => setRecarga((n) => n + 1);

  function buscar(evento) {
    evento.preventDefault(); // evita que el formulario recargue la pagina
    setAviso(null);
    setNombre(texto);
    // Si el texto no cambio, setNombre no provoca un nuevo render: recargar()
    // asegura que "Buscar" siempre vuelva a consultar la API.
    recargar();
  }

  function limpiar() {
    setAviso(null);
    setTexto("");
    setNombre("");
  }

  // Envuelve una accion sobre una fila: bloquea sus botones, informa y recarga.
  async function ejecutar(bebida, accion, exito) {
    setProcesandoId(bebida.id);
    setAviso(null);
    try {
      await accion(bebida.id);
      setAviso({ variante: "success", texto: exito });
      recargar();
    } catch (e) {
      setAviso({ variante: "danger", texto: e.message });
    } finally {
      setProcesandoId(null);
    }
  }

  function eliminar(bebida) {
    if (window.confirm(`¿Eliminar "${bebida.nombre}"?`)) {
      ejecutar(bebida, eliminarBebida, `"${bebida.nombre}" fue eliminada.`);
    }
  }

  function restringir(bebida) {
    ejecutar(bebida, restringirVenta, `La venta de "${bebida.nombre}" quedó restringida.`);
  }

  return (
    <section className="mb-5">
      <h2 className="h4">Catálogo de bebidas</h2>

      <Form onSubmit={buscar} className="mb-3">
        <InputGroup>
          <Form.Control
            placeholder="Filtrar por nombre"
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            aria-label="Filtrar por nombre"
          />
          <Button type="submit" variant="primary">Buscar</Button>
          <Button variant="outline-secondary" onClick={limpiar} disabled={!texto && !nombre}>
            Limpiar
          </Button>
        </InputGroup>
      </Form>

      {aviso && (
        <Alert variant={aviso.variante} dismissible onClose={() => setAviso(null)}>
          {aviso.texto}
        </Alert>
      )}

      {cargando ? (
        <div className="text-center py-4">
          <Spinner animation="border" role="status" />
          <div className="text-muted mt-2">Cargando bebidas…</div>
        </div>
      ) : error ? (
        <Alert variant="danger">
          <p className="mb-2">No se pudo cargar el catálogo: {error}</p>
          <Button variant="outline-danger" size="sm" onClick={recargar}>Reintentar</Button>
        </Alert>
      ) : bebidas.length === 0 ? (
        <Alert variant="secondary">
          {nombre ? `No hay bebidas que coincidan con "${nombre}".` : "No hay bebidas registradas."}
        </Alert>
      ) : (
        <Table striped bordered hover responsive className="align-middle">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Tipo</th>
              <th className="text-end">Volumen</th>
              <th className="text-end">Stock</th>
              <th className="text-end">Precio</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {bebidas.map((b) => (
              <tr key={b.id}>
                <td>{b.nombre}</td>
                <td>{TIPOS[b.tipo] ?? b.tipo}</td>
                <td className="text-end">{b.volumenML} ml</td>
                <td className="text-end">{b.stock}</td>
                <td className="text-end">{pesos(b.precio)}</td>
                <td>
                  {b.ventaRestringida
                    ? <Badge bg="danger">Restringida</Badge>
                    : <Badge bg="success">Disponible</Badge>}
                </td>
                <td className="text-nowrap">
                  <Button
                    size="sm"
                    variant="outline-warning"
                    className="me-2"
                    disabled={b.ventaRestringida || procesandoId === b.id}
                    onClick={() => restringir(b)}
                  >
                    Restringir
                  </Button>
                  <Button
                    size="sm"
                    variant="outline-danger"
                    disabled={procesandoId === b.id}
                    onClick={() => eliminar(b)}
                  >
                    Eliminar
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
    </section>
  );
}
