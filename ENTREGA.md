# ENTREGA — Prueba práctica: Cancelación de citas con motivo

## 1. Lista de archivos creados y modificados

### Archivos creados
- `sql/002-motivo-cancelacion.sql`: Script de migración `ALTER TABLE` que agrega la columna `motivo_cancelacion VARCHAR(200) NULL` a la tabla `citas` sin borrar ni perder datos existentes (R1).
- `src/main/java/com/medisalud/exception/CancelacionTardiaException.java`: Nueva excepción propia de dominio lanzada cuando se intenta cancelar una cita cuya fecha es anterior a la fecha actual (`LocalDate.now()`) (RN3, R3).
- `.gitignore`: Excluye la carpeta de compilación `out/`, artefactos temporales y configuraciones locales del IDE.
- `ENTREGA.md`: Documento de sustento técnico y resultados de pruebas de la funcionalidad.

### Archivos modificados
- `sql/medisalud.sql`: Se actualizó la definición `CREATE TABLE IF NOT EXISTS citas` para incluir `motivo_cancelacion VARCHAR(200) NULL` en instalaciones limpias (R1).
- `src/main/java/com/medisalud/entity/Cita.java`: Se agregó el atributo `motivoCancelacion`, el constructor de reconstrucción con estado y motivo (sin disparar notificaciones al leer de BD), el método de dominio `cancelar(String motivo)` que valida RN1 y cambia el estado notificando a los observadores, `obtenerTextoMotivo()` para manejar `Sin motivo registrado` (RN6) y el ajuste en `toString()` (R3).
- `src/main/java/com/medisalud/persistencia/jdbc/CitaDAO.java`: Se actualizaron las consultas `INSERT ... ON DUPLICATE KEY UPDATE` y `SELECT` con `PreparedStatement` para persistir y leer `motivo_cancelacion`, y se ajustó `mapearCita` para usar el constructor de reconstrucción sin invocar `cambiarEstado(...)` al listar (R2).
- `src/main/java/com/medisalud/persistencia/jdbc/ConexionBD.java`: Verifica automáticamente en la primera conexión que la columna `motivo_cancelacion` esté presente en la tabla `citas`.
- `src/main/java/com/medisalud/service/ServicioCitas.java`: Se reemplazó `cancelarCita(String)` por `cancelarCita(String codigo, String motivo)` aplicando RN1–RN5 y se agregó `contarCancelacionesPorMedico()` con `Stream API` (R3, R4).
- `src/main/java/com/medisalud/Principal.java`: Se inyecta el `ObservadorCitaNotificacion` en `ServicioCitas` para garantizar que los cambios de estado sobre citas recuperadas desde JDBC notifiquen al `HiloNotificaciones` en segundo plano (RN5).
- `src/main/java/com/medisalud/controlador/ControladorMediSalud.java`: Se agregaron los métodos delegados `cancelarCita(String codigoCita, String motivo)` y `generarReporteCancelacionesPorMedico()` (R4).
- `src/main/java/com/medisalud/vista/VistaConsola.java`: Se agregó la opción `7. Cancelar cita`, se renumeró `Salir` a `8`, se añadió la columna `Motivo` al reporte de citas por médico y la cuarta sección `Cancelaciones por medico` (R4).
- `README.md`: Documentación actualizada de compilación, migración y ejecución.

---

## 2. Decisión sobre `cancelarCita(String)`

Se **eliminó** por completo el método antiguo `cancelarCita(String codigo)` de `ServicioCitas` y se reemplazó por `cancelarCita(String codigo, String motivo)`.
- **Por qué:** La regla de negocio RN1 establece que el motivo de cancelación es **obligatorio** para toda nueva cancelación. Eliminar la sobrecarga sin motivo garantiza en tiempo de compilación que ninguna capa del sistema pueda cancelar una cita omitiendo el motivo ni saltándose las validaciones de longitud y fecha (RN1 y RN3).

---

## 3. Dónde se aplicó cada concepto

| Concepto | Archivo | Método(s) |
|---|---|---|
| **Patrón State** | `src/main/java/com/medisalud/entity/EstadoCita.java` y `src/main/java/com/medisalud/service/ServicioCitas.java` | `EstadoCita.puedeTransicionarA(EstadoCita)` invocado desde `ServicioCitas.cancelarCita(String, String)` |
| **Patrón Observer** | `src/main/java/com/medisalud/entity/Cita.java` y `src/main/java/com/medisalud/notificacion/ObservadorCitaNotificacion.java` | `Cita.cancelar(String)` → `Cita.cambiarEstado(EstadoCita)` → `ObservadorCitaNotificacion.notificarCambioEstado(Cita)` |
| **Excepción propia** | `src/main/java/com/medisalud/exception/CancelacionTardiaException.java` y `src/main/java/com/medisalud/service/ServicioCitas.java` | Constructor `CancelacionTardiaException(String, LocalDate)` lanzada en `ServicioCitas.cancelarCita(String, String)` |
| **`PreparedStatement`** | `src/main/java/com/medisalud/persistencia/jdbc/CitaDAO.java` | `CitaDAO.guardar(Cita)`, `CitaDAO.buscarPorCodigo(String)` y `CitaDAO.listarConFiltro(String, String)` |
| **Stream API** | `src/main/java/com/medisalud/service/ServicioCitas.java` | `ServicioCitas.contarCancelacionesPorMedico()` (`stream` + `filter` + `Collectors.groupingBy` + `Collectors.counting` + `sorted`) |

---

## 4. Resultado de los 12 casos de prueba

| # | Caso de prueba | Estado | Observaciones |
|---|---|---|---|
| 1 | `T01`, motivo `Paciente viaja` | ✅ | Cambia a `CANCELADA` con su motivo y `HiloNotificaciones` imprime `[notificacion] La cita T01 ahora esta CANCELADA`. |
| 2 | `T02` (`CONFIRMADA`), motivo `Medico en congreso` | ✅ | Cambia a `CANCELADA` con el motivo indicado. |
| 3 | `T03` (`ATENDIDA`), motivo `Error` | ✅ | Rechazado con `TransicionInvalidaException`; la cita permanece `ATENDIDA`. |
| 4 | `T01` otra vez, motivo `Otro` | ✅ | Rechazado con `TransicionInvalidaException` antes de modificar el motivo; conserva `Paciente viaja`. |
| 5 | `TPASADA` (fecha de ayer), motivo `Tarde` | ✅ | Rechazado con `CancelacionTardiaException` indicando el código `TPASADA` y su fecha; permanece `PENDIENTE`. |
| 6 | `ZZZ`, motivo `X` | ✅ | Rechazado con `CitaNoEncontradaException`; la vista informa el mensaje y el menú continúa operando. |
| 7 | `T05`, motivo vacío o solo espacios | ✅ | Rechazado con `IllegalArgumentException` por motivo obligatorio; `T05` sigue `PENDIENTE`. |
| 8 | `T05`, motivo de 201 caracteres | ✅ | Rechazado con `IllegalArgumentException` por superar los 200 caracteres; `T05` sigue `PENDIENTE`. |
| 9 | Cerrar (opción 8), reabrir y consultar reporte (opción 6) de `M009` | ✅ | `T01` y `T02` persisten en MySQL con estado `CANCELADA` y sus motivos respectivos. |
| 10 | Reporte (opción 6) de `M010` (incluye `C004` previa) | ✅ | `C004` muestra `Sin motivo registrado` en la columna `Motivo`, sin `NullPointerException`. |
| 11 | Opción 6 → sección «Cancelaciones por médico» | ✅ | Muestra el conteo agrupado por médico ordenado de mayor a menor cantidad usando Stream API. |
| 12 | Opción 8 (Salir) | ✅ | Detiene `HiloNotificaciones` de forma ordenada (`detener()` + `join()`) y finaliza el programa. |

---

## 5. Riesgo o mejora identificada

Actualmente la validación de cancelación tardía (`cita.getFecha().isBefore(LocalDate.now())`) utiliza la fecha local de la máquina donde corre el cliente Java y solo contempla la fecha (`LocalDate`) sin la hora de la cita (`LocalDateTime`). En un entorno productivo distribuido sería recomendable almacenar fecha y hora (`DATETIME`), comparar contra la zona horaria oficial de la clínica (o la hora del servidor de base de datos) y exigir una ventana mínima de anticipación (por ejemplo, cancelar con al menos 2 horas de antelación).
