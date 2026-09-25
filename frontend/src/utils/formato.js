// Solo formatean datos que ya entrego el backend: aqui no se calcula ni se decide nada.

// 4200 -> "$4.200"
export const pesos = (valor) => valor.toLocaleString("es-CL", { style: "currency", currency: "CLP" });

// "2026-09-25T12:13:06.49" -> "25-09-2026, 12:13" (año completo y reloj de 24 horas)
export const fechaHora = (iso) =>
  new Date(iso).toLocaleString("es-CL", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23",
  });

// Texto de cada motivo de rechazo que devuelve la API. Si llega uno nuevo, se muestra el codigo.
const MOTIVOS = {
  VENTA_RESTRINGIDA: "Venta restringida",
  LIMITE_EXCEDIDO: "Límite por cliente excedido",
  STOCK_INSUFICIENTE: "Stock insuficiente",
};
export const textoMotivo = (codigo) => MOTIVOS[codigo] ?? codigo;
