import { useState } from "react";
import { Alert, Container } from "react-bootstrap";
import BebidaForm from "./components/BebidaForm.jsx";
import BebidaList from "./components/BebidaList.jsx";
import { pesos } from "./utils/formato.js";

/**
 * Estructura sugerida de la interfaz. Cada bloque es un componente propio
 * dentro de src/components/:
 *
 *   BebidaList      tabla del catalogo, con filtro por nombre
 *   BebidaForm      alta y edicion de una bebida
 *   VentaForm       registro de una venta
 *   VentaHistorial  listado de ventas con su estado y motivo
 *
 * Ningun componente calcula precios ni decide si una venta se autoriza:
 * esos datos vienen del backend.
 */
export default function App() {
  const [editando, setEditando] = useState(null); // bebida en edicion, o null
  const [version, setVersion] = useState(0);      // sube cuando cambian los datos
  const [aviso, setAviso] = useState(null);

  function editar(bebida) {
    setAviso(null);
    setEditando(bebida);
    window.scrollTo({ top: 0, behavior: "smooth" }); // el formulario esta arriba
  }

  function alGuardar(bebida, esNueva) {
    setAviso(`"${bebida.nombre}" fue ${esNueva ? "creada" : "actualizada"}. Precio: ${pesos(bebida.precio)}.`);
    setEditando(null);
    setVersion((v) => v + 1);
  }

  return (
    <Container className="py-4">
      <h1 className="mb-1">Fonda San Belarmino</h1>
      <p className="text-muted">Control de bebidas y ventas</p>

      {aviso && (
        <Alert variant="success" dismissible onClose={() => setAviso(null)}>
          {aviso}
        </Alert>
      )}

      {/* La key cambia al pasar a otra bebida o despues de guardar: React crea
          un formulario nuevo, con sus valores iniciales, en vez de reusar el anterior. */}
      <BebidaForm
        key={editando ? `editar-${editando.id}` : `nueva-${version}`}
        bebida={editando}
        onGuardada={alGuardar}
        onCancelar={() => setEditando(null)}
      />

      <BebidaList onEditar={editar} version={version} />
    </Container>
  );
}
