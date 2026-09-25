# Fonda San Belarmino — Tarea DSY1104 (EA3: Integración REST)

App full stack para controlar bebidas y ventas de una fonda. El enunciado completo está en `README.md`: es la fuente de verdad y manda sobre este archivo.

## Contexto

- Trabajo individual. Tengo que poder **explicar cada línea** ante el docente.
- Explícame cada capa antes de pasar a la siguiente. No implementes todo de una.
- Responde en español chileno, directo y conciso.

## Stack y comandos

- Backend: Java 21, Spring Boot 3.3.5, Spring Data JPA, Bean Validation. Paquete base `cl.dsy1104.fonda`.
- Frontend: React 18, Vite 5, React-Bootstrap.
- BD: H2 en memoria (perfil por defecto). MySQL con el perfil `mysql`, usando las variables `DB_USER` y `DB_PASSWORD`.

```bash
# Backend (http://localhost:8080/api)
cd backend && mvn spring-boot:run
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=mysql
cd backend && mvn test

# Frontend (http://localhost:5173)
cd frontend && npm install && npm run dev
cd frontend && npm run build
```

## Arquitectura (obligatoria)

`Controller → Service → Repository → BD`. Cada capa habla solo con la siguiente.

- **Controller**: solo HTTP. Recibe el DTO con `@Valid`, llama al service y traduce el resultado a un código de estado. Nunca accede al repository ni tiene lógica de negocio.
- **Service**: la ÚNICA capa con reglas de negocio (precio, restricciones, stock, estado de la venta).
- **Repository**: interfaces de Spring Data, sin reglas de negocio.
- **Errores**: todos los errores pasan por un solo `@RestControllerAdvice`. Ningún controller arma respuestas de error por su cuenta.
- **Frontend**: no calcula precios ni decide ventas. Los componentes solo consumen `src/services/api.js` y nunca usan `fetch` directo.
- Convenciones Java: `PascalCase` para clases, `camelCase` para métodos y atributos, un paquete por responsabilidad.

## Reglas de negocio

Precio (lo calcula el service al exponer la bebida y al vender):
- `ALCOHOLICA`: precio base $3.500. Sube 20% si `certificada` es false.
- `SIN_ALCOHOL`: precio base $2.000. Sube 10% si `azucarPorLitro` > 80.

Venta: las verificaciones van en este orden y se detienen en la primera que falla. La venta rechazada **también se guarda** en el historial.
1. Si `ventaRestringida` → 409 `VENTA_RESTRINGIDA`.
2. Si es alcohólica y las unidades superan el límite → 409 `LIMITE_EXCEDIDO`.
3. Si el stock es menor que las unidades → 409 `STOCK_INSUFICIENTE`.
4. Si pasa todo: descuenta stock, calcula `total = precio × unidades`, guarda la venta y responde 201 con `Location`.

El límite (3) se lee con `@Value("${fonda.limite-unidades-por-cliente}")`. **Nunca escribas el número 3 en el código.**

## Validaciones (en el DTO, con 400 y errores campo por campo)

- `nombre`: no puede ser nulo ni vacío.
- `volumenML`: entre 100 y 3000.
- `stock`: mayor o igual a 0.
- `gradosAlcohol`: obligatorio y entre 0.5 y 45 si el tipo es `ALCOHOLICA`; null en el otro tipo.
- `azucarPorLitro`: obligatorio y mayor o igual a 0 si el tipo es `SIN_ALCOHOL`; null en el otro tipo.

## Endpoints

| Método | Ruta | Respuestas |
|---|---|---|
| GET | `/api/bebidas?nombre=` | 200 |
| GET | `/api/bebidas/{id}` | 200 · 404 |
| POST | `/api/bebidas` | 201 + Location · 400 |
| PUT | `/api/bebidas/{id}` | 200 · 400 · 404 |
| DELETE | `/api/bebidas/{id}` | 204 · 404 |
| PATCH | `/api/bebidas/{id}/restriccion` | 200 · 404 |
| POST | `/api/ventas` | 201 · 409 · 404 |
| GET | `/api/ventas` | 200 |

## Contrato JSON (backend y frontend deben usar estos nombres)

```
BebidaResponse: id, nombre, tipo, volumenML, stock, gradosAlcohol, certificada, azucarPorLitro, ventaRestringida, precio
VentaRequest:   bebidaId, unidades
VentaResponse:  id, bebidaId, nombre, unidades, total, estado, motivo, fecha
Error 400:      { "error": "VALIDACION", "campos": { "<campo>": "<mensaje>" } }
Error 404/409:  { "error": "<CODIGO>", "mensaje": "<texto>" }
```

Resultados esperados con los datos de `data.sql`:
- Chicha alcohólica: precio 4200.
- Chicha sin alcohol: precio 2200.
- Vender 2 Pisco Sour: total 7000.

## Estado actual

- ✅ `model/`: `Bebida`, `Venta`, `TipoBebida`, `EstadoVenta`, `MotivoRechazo`.
- ✅ `defer-datasource-initialization=true` en los dos `.properties`.
- ⬜ `repository/`, `service/`, `controller/`, `dto/`, `config/` (CORS desde `fonda.cors.origen`), `exception/`.
- ⬜ Frontend: `api.js` es solo el esqueleto (funciones con `TODO`). Faltan `components/` (`BebidaList`, `BebidaForm`, `VentaForm`, `VentaHistorial`) y montarlos en `App.jsx`.
- ⬜ Tests, `.gitignore` y `.github/workflows/build.yml`.
- ⚠️ Pendiente decidir qué pasa al eliminar una bebida con ventas (la FK falla): responder 409 o hacer borrado lógico.
- ⚠️ Pendiente decidir si se agrega `GET /api/ventas/{id}`: el `Location` del POST apunta ahí y hoy daría 404.
- ⚠️ Revisar que `data.sql` funcione en MySQL (en H2 ya carga).

## Cómo trabajar

- Orden: repository → service → controller → frontend.
- Después de cada capa: compila, levanta la app y prueba con los `curl` de la sección 7 del README.
- Propón un commit por capa con un mensaje claro, pero **no hagas commit ni push sin preguntarme**.
- Nunca subas `target/`, `node_modules/`, `dist/`, `.env` ni credenciales. El repo es público.
- No inventes nombres de columnas: la entidad tiene que coincidir con `data.sql` (por ejemplo `volumen_ml`).
