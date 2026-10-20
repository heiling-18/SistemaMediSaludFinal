# Sistema de Gestión de Citas Médicas — MediSalud

Aplicación de consola en Java con arquitectura MVC, patrones de diseño (State, Observer, Strategy, Decorator, Builder, Facade, Singleton), concurrencia (`Thread` y `AtomicLong`), persistencia en MySQL mediante JDBC (`PreparedStatement`) y reportes con `Stream API`. Incluye la funcionalidad de **Cancelación de citas con motivo** y reporte de cancelaciones agrupadas por médico.

## Requisitos previos

- **JDK 17** o superior
- **Docker** y **Docker Compose** (con Docker Desktop activo)
- Conector JDBC `lib/mysql-connector-j-8.3.0.jar` (incluido en el proyecto)

## 1. Base de datos con Docker Compose

Levantar el contenedor de MySQL desde la raíz del proyecto:

```bash
docker compose up -d
```

Si la base de datos ya existía previamente sin la columna `motivo_cancelacion`, aplicar la migración sin pérdida de datos:

```bash
# Linux / macOS / Git Bash:
docker compose exec -T mysql mysql -uroot -proot < sql/002-motivo-cancelacion.sql

# Windows PowerShell:
Get-Content sql/002-motivo-cancelacion.sql | docker compose exec -T mysql mysql -uroot -proot
```

## 2. Compilar el proyecto

- **Linux / macOS / Git Bash:**
  ```bash
  javac -d out $(find src/main/java -name "*.java")
  ```

- **Windows (PowerShell):**
  ```powershell
  javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src/main/java).FullName
  ```

## 3. Ejecutar el proyecto

- **Linux / macOS / Git Bash:**
  ```bash
  java -cp "out:lib/mysql-connector-j-8.3.0.jar" com.medisalud.Principal
  ```

- **Windows (PowerShell o `cmd`):**
  ```powershell
  java -cp "out;lib/mysql-connector-j-8.3.0.jar" com.medisalud.Principal
  ```

- **Desde Visual Studio Code:** abrir [`src/main/java/com/medisalud/Principal.java`](src/main/java/com/medisalud/Principal.java) y presionar **`F5`** o hacer clic en **Run**.

## Menú de opciones

1. **Registrar paciente**
2. **Registrar médico**
3. **Agendar cita**
4. **Consultar historia clínica**
5. **Facturar consulta**
6. **Generar reporte** *(incluye columna `Motivo` en citas del médico y sección `Cancelaciones por médico`)*
7. **Cancelar cita** *(con validación de motivo, estado, fecha y notificación en segundo plano)*
8. **Salir**
