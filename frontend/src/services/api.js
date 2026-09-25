// Unico punto del frontend que conoce la direccion del backend.
// Los componentes importan estas funciones y no usan fetch directamente.

const API = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

/**
 * Error de la API con lo que el componente necesita para mostrarlo:
 * - status:  codigo HTTP (0 si el backend no respondio)
 * - codigo:  campo "error" del cuerpo (VALIDACION, LIMITE_EXCEDIDO, ...)
 * - campos:  en un 400, { campo: mensaje } para marcar cada input
 * - message: texto listo para mostrar al usuario
 */
export class ApiError extends Error {
  constructor(status, codigo, message, campos = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.codigo = codigo;
    this.campos = campos;
  }
}

/** Hace la peticion y lanza ApiError cuando falla la red o el status no es 2xx. */
async function pedir(ruta, { method = "GET", body } = {}) {
  const opciones = { method };
  // Content-Type solo cuando hay cuerpo: en un GET provocaria un preflight CORS de mas.
  if (body !== undefined) {
    opciones.headers = { "Content-Type": "application/json" };
    opciones.body = JSON.stringify(body);
  }

  let res;
  try {
    res = await fetch(`${API}${ruta}`, opciones);
  } catch {
    // fetch solo lanza si no hubo respuesta: backend apagado, sin red o CORS bloqueado.
    throw new ApiError(0, "SIN_CONEXION", "No se pudo conectar con el servidor.");
  }

  if (!res.ok) {
    // El backend responde { error, mensaje } o { error: "VALIDACION", campos }.
    // Si el cuerpo no es JSON (por ejemplo un 403 de CORS), se usa un texto generico.
    const cuerpo = await res.json().catch(() => ({}));
    const mensaje =
      cuerpo.mensaje ??
      (cuerpo.campos ? "Revisa los campos marcados." : `Error del servidor (HTTP ${res.status}).`);
    throw new ApiError(res.status, cuerpo.error ?? "ERROR_HTTP", mensaje, cuerpo.campos ?? {});
  }

  return res.status === 204 ? null : res.json();
}

export function listarBebidas(nombre) {
  // El filtrado lo hace el servidor: aqui solo se agrega ?nombre= si viene.
  const filtro = nombre?.trim();
  const query = filtro ? `?${new URLSearchParams({ nombre: filtro })}` : "";
  return pedir(`/bebidas${query}`);
}

export function crearBebida(datos) {
  return pedir("/bebidas", { method: "POST", body: datos });
}

export function actualizarBebida(id, datos) {
  return pedir(`/bebidas/${id}`, { method: "PUT", body: datos });
}

export function eliminarBebida(id) {
  return pedir(`/bebidas/${id}`, { method: "DELETE" });
}

export function restringirVenta(id) {
  return pedir(`/bebidas/${id}/restriccion`, { method: "PATCH" });
}

export function registrarVenta(bebidaId, unidades) {
  return pedir("/ventas", { method: "POST", body: { bebidaId, unidades } });
}

export function listarVentas() {
  return pedir("/ventas");
}
