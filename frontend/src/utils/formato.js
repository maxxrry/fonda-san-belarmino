// Solo formatea numeros que ya calculo el backend: 4200 -> "$4.200".
export const pesos = (valor) => valor.toLocaleString("es-CL", { style: "currency", currency: "CLP" });
