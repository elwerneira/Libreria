# Libros

## Ejecutar

Desde esta carpeta (la que contiene este README y el pom.xml), con un JDK 21 o superior:

En este equipo, la conexion a Oracle Cloud mediante la wallet ya esta configurada en `config/application-local.properties`. Ese archivo contiene los accesos y esta excluido de Git. La wallet permanece fuera del proyecto, en `../Wallet_Biblioteca`.

Ejecutar en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-27.0.1.0.3'
.\mvnw.cmd spring-boot:run
```

En otro equipo:

1. Instalar un JDK compatible de version 21 o superior y ajustar JAVA_HOME a su ubicacion.
2. Copiar o clonar la carpeta activa del proyecto, la que contiene este README y el pom.xml.
3. Copiar la wallet por separado a una carpeta de ese equipo, conservando todos sus archivos.
4. Crear `config/application-local.properties` con los accesos de la misma base y la ruta real de la wallet:

```properties
spring.datasource.username=USUARIO
spring.datasource.password=CLAVE
spring.datasource.url=jdbc:oracle:thin:@biblioteca_low?TNS_ADMIN="C:/ruta/con espacios/Wallet_Biblioteca"
```

Las comillas en TNS_ADMIN conservan los espacios de la ruta. El soporte de wallet se incluye mediante `oraclepki`. No subir el archivo local ni la wallet a Git: al clonar el repositorio estos archivos no se descargan.

5. Tener conexion a Internet y acceso de red a Oracle Cloud (TCPS, puerto 1522). Si la base restringe las IP de origen, el nuevo equipo debe tener acceso autorizado.
6. Desde la carpeta que contiene pom.xml, ejecutar `.\mvnw.cmd spring-boot:run`. Maven Wrapper descarga las herramientas y dependencias necesarias en la primera ejecucion; no hace falta instalar Maven por separado.

Al usar la misma base se accede a los mismos libros; no hay que crear otra base ni ejecutar nuevamente el SQL. El puerto 8080 debe estar disponible. Para usar variables de entorno en lugar del archivo local, los nombres de Spring son SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME y SPRING_DATASOURCE_PASSWORD.

La tabla LIBROS ya fue creada en Oracle mediante JPA. Se conserva `ddl-auto=update`: al iniciar, Hibernate puede crear o actualizar su estructura. `sql/libros.sql` queda como alternativa para crearla manualmente en una base nueva; no se debe volver a ejecutar sobre la tabla existente.

Para ejecutar las pruebas locales:

```powershell
.\mvnw.cmd test
```

Estas pruebas usan H2 en memoria y no modifican Oracle.

## Pruebas en Postman

URL base: `http://localhost:8080`.

| Metodo | Ruta | Resultado esperado |
| --- | --- | --- |
| POST | `/libros` | 201, libro creado y cabecera Location con su ID |
| GET | `/libros/{id}` | 200, libro encontrado |
| PUT | `/libros/{id}` | 200, libro actualizado |
| DELETE | `/libros/{id}` | 204, sin cuerpo |

Cuerpo JSON para POST y PUT (Body > raw > JSON):

```json
{
  "titulo": "Papelucho",
  "autor": "Marcela Paz",
  "anioPublicacion": 1947,
  "genero": "Infantil"
}
```

El ID lo genera Oracle; para GET, PUT y DELETE se usa el recibido en POST. PUT reemplaza los cuatro atributos y conserva el ID de la URL. Un ID inexistente devuelve 404; los campos incompletos o invalidos devuelven 400.

Para evidenciar persistencia en Oracle, comprobar el registro con `SELECT * FROM libros;` despues de POST y PUT, y verificar su ausencia despues de DELETE.

## Alcance y pendientes

Se implementan los cuatro metodos solicitados, los cinco atributos y persistencia JPA.

Verificacion del 25 de octubre de 2026:

- Conexion mTLS con wallet confirmada a Oracle 23 mediante `biblioteca_low`.
- Tabla LIBROS creada con sus cinco columnas.
- Pruebas HTTP reales mediante PowerShell: POST 201, GET 200, PUT 200 y DELETE 204. Consulta posterior al DELETE: 404.
- Se confirmo por JDBC que el registro creado mediante HTTP estaba almacenado en Oracle.
- Solo se elimino el libro ID 1 creado para esa verificacion; no se modificaron registros previos.
- Cinco pruebas automatizadas aprobadas, sin errores.

Las pruebas HTTP anteriores se ejecutaron desde PowerShell; aun no se han capturado evidencias en Postman. Falta publicar el repositorio remoto y compartir su enlace con el docente.

La subcarpeta `Libros/` es una copia del proyecto inicial que se conserva y se excluye de Git. El proyecto activo es la carpeta que contiene este README.
