import { useEffect, useState } from "react";
import { Alert, Badge, Button, Spinner, Table } from "react-bootstrap";
import { listarVentas } from "../services/api.js";
import { fechaHora, pesos, textoMotivo } from "../utils/formato.js";

/**
 * Historial de ventas, autorizadas y rechazadas, tal como lo entrega la API
 * (las mas recientes primero). En las rechazadas muestra el motivo informado.
 *
 * Props:
 * - version: al cambiar (por ejemplo, tras una venta) se vuelve a pedir.
 */
export default function VentaHistorial({ version = 0 }) {
  const [ventas, setVentas] = useState([]);
  const [recarga, setRecarga] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let vigente = true;
    setCargando(true);
    setError(null);
    listarVentas()
      .then((datos) => vigente && setVentas(datos))
      .catch((e) => vigente && setError(e.message))
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
  }, [version, recarga]);

  return (
    <section className="mb-5">
      <h2 className="h4">Historial de ventas</h2>

      {cargando ? (
        <div className="text-center py-4">
          <Spinner animation="border" role="status" />
          <div className="text-muted mt-2">Cargando ventas…</div>
        </div>
      ) : error ? (
        <Alert variant="danger">
          <p className="mb-2">No se pudo cargar el historial: {error}</p>
          <Button variant="outline-danger" size="sm" onClick={() => setRecarga((n) => n + 1)}>
            Reintentar
          </Button>
        </Alert>
      ) : ventas.length === 0 ? (
        <Alert variant="secondary">Todavía no hay ventas registradas.</Alert>
      ) : (
        <Table striped bordered hover responsive className="align-middle">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Bebida</th>
              <th className="text-end">Unidades</th>
              <th className="text-end">Total</th>
              <th>Estado</th>
              <th>Motivo</th>
            </tr>
          </thead>
          <tbody>
            {ventas.map((v) => {
              const autorizada = v.estado === "AUTORIZADA";
              return (
                <tr key={v.id}>
                  <td className="text-nowrap">{fechaHora(v.fecha)}</td>
                  <td>{v.nombre}</td>
                  <td className="text-end">{v.unidades}</td>
                  {/* Una rechazada no cobra: se muestra un guion en vez de $0. */}
                  <td className="text-end">{autorizada ? pesos(v.total) : "—"}</td>
                  <td>
                    {autorizada
                      ? <Badge bg="success">Autorizada</Badge>
                      : <Badge bg="danger">Rechazada</Badge>}
                  </td>
                  <td>{v.motivo ? textoMotivo(v.motivo) : "—"}</td>
                </tr>
              );
            })}
          </tbody>
        </Table>
      )}
    </section>
  );
}
