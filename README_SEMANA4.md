# Libros Semana 4

Actividad de Desarrollo Full Stack III: Implementando lineamientos empresariales. Se reutiliza la logica de Libros para crear un arquetipo Maven propio, generar un microservicio desde esa plantilla y desplegarlo en AWS Academy con GitHub Actions.

## Que contiene la entrega

| Carpeta | Para que sirve |
| --- | --- |
| `src` | Microservicio base de Libros y pruebas H2 |
| `arquetipo-libros` | Plantilla Maven propia que incluye el patron Builder |
| `target/generado/libros-generado` | Proyecto generado durante la verificacion; no se sube a Git |
| `.github/workflows/semana4.yml` | Integracion continua y despliegue |
| `infra/academy.json` | Definicion de EC2 y su grupo de seguridad |
| `deploy` | Actualizacion de la aplicacion mediante AWS Systems Manager |
| `postman` | Coleccion de pruebas del CRUD |
| `scripts/actualizar-arquetipo.py` | Actualiza la plantilla desde una lista explicita de archivos del proyecto base |

El proyecto conserva los atributos `id`, `titulo`, `autor`, `anioPublicacion` y `genero`. La base de datos sigue siendo Oracle Biblioteca. La tabla LIBROS y sus registros existentes se conservan; no se vuelven a ejecutar los scripts SQL sobre ella.

## Patron de diseno Builder

`Libro` utiliza `@Builder` de Lombok. En `LibroService.crear` se construye un libro nuevo paso a paso, indicando titulo, autor, anio y genero. El ID no forma parte del Builder: lo asigna Oracle. De esta forma un POST con el ID de otro libro no modifica ese registro.

El constructor sin argumentos se mantiene para JPA y JSON. Las validaciones siguen aplicandose en el controlador. El arquetipo incluye tanto el Builder como las pruebas que comprueban este comportamiento.

## Arquetipo y proyecto generado

Se necesita un JDK compatible con Java 21. Maven Wrapper viene incluido. Desde la raiz:

```powershell
.\mvnw.cmd -B -ntp -f arquetipo-libros/pom.xml install
.\mvnw.cmd -B -ntp org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate "-DarchetypeGroupId=cl.duoc.fullstack" "-DarchetypeArtifactId=libros-arquetipo" "-DarchetypeVersion=1.0.0" "-DgroupId=cl.duoc.fullstack" "-DartifactId=libros-generado" "-Dversion=1.0.0-SNAPSHOT" "-Dpackage=cl.duoc.biblioteca" "-DoutputDirectory=target/generado" "-DinteractiveMode=false" "-DarchetypeCatalog=local"
.\mvnw.cmd -B -ntp -f target/generado/libros-generado/pom.xml verify
```

La generacion se ejecuta sobre un destino nuevo. Si ya existe, elegir otro artifactId o directorio. El paquete del ejemplo cambia a `cl.duoc.biblioteca`, demostrando que se usa la plantilla y no una copia con rutas fijas. El descriptor `archetype-metadata.xml` controla los archivos incluidos y el cambio de paquete.

El JAR que despliega GitHub Actions corresponde al **proyecto generado**. No se limita a compilar el proyecto base. Los archivos de Oracle privados no forman parte del arquetipo ni del JAR.

## Iniciar en local

Copiar la wallet a `Wallet_Biblioteca`, junto al pom.xml, y configurar `config/application-local.properties` usando el archivo de ejemplo. Ese archivo debe contener los accesos reales de la base y esta URL:

```properties
spring.datasource.url=jdbc:oracle:thin:@biblioteca_low?TNS_ADMIN=./Wallet_Biblioteca
```

Desde la raiz del proyecto base:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-27.0.1.0.3"
.\mvnw.cmd spring-boot:run
```

La ruta de Java se ajusta al equipo. API local: `http://localhost:8080/libros`. Si se ejecuta el proyecto generado, tambien necesita su propia carpeta de wallet y su archivo privado antes de iniciarlo. Se usa `ddl-auto=validate`; no se recrea la tabla.

## GitHub Actions

El workflow se ejecuta al hacer push a main, abrir un pull request o iniciarlo manualmente:

1. Prepara Java 21.
2. Compila y prueba el proyecto base con H2.
3. Instala el arquetipo propio en Maven.
4. Genera otro proyecto y ejecuta sus pruebas.
5. Guarda el JAR generado como artefacto de la ejecucion.
6. En main, si `AWS_DEPLOY_ENABLED` vale `true`, carga el JAR al bucket privado y actualiza EC2 mediante Systems Manager.
7. Comprueba `GET /libros` desde EC2. Si una nueva version falla, intenta restaurar la anterior.

Los pull request no despliegan ni reciben credenciales de AWS. Los tests de CI no usan Oracle ni necesitan su wallet.

Variables del repositorio: `AWS_DEPLOY_ENABLED`, `ARTIFACT_BUCKET` y `EC2_INSTANCE_ID`. Secretos: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN`.

En AWS Academy esos secretos son temporales. Cuando caduquen, iniciar el laboratorio y reemplazar los tres valores desde Settings > Secrets and variables > Actions. Despues volver a ejecutar el workflow. Nunca escribir los valores en el codigo, en un commit o en un log.

## Despliegue AWS Academy

Se usa una EC2 t3.micro con Amazon Linux y Java 21, utilizando el perfil existente `LabInstanceProfile`. No se crean usuarios IAM ni claves permanentes. Un bucket S3 privado con cifrado guarda las versiones y, por separado, la wallet y el archivo privado de Oracle. Se bloquea el acceso publico al bucket.

El grupo de seguridad permite el puerto 8080 solo desde la IP indicada en `AllowedCidr`. No se abre SSH. Systems Manager realiza la administracion. El microservicio se ejecuta como un usuario de sistema sin privilegios y se inicia mediante systemd.

La direccion de la API se obtiene de la salida `ApiUrl` del stack o de la IP publica actual de la instancia, agregando `:8080/libros`. El despliegue de laboratorio utiliza HTTP restringido por IP; no se publica la API para todo Internet.

Si cambia la red desde donde se hace la demostracion, actualizar `AllowedCidr` con la nueva IP /32. Si se detiene y vuelve a iniciar la instancia, comprobar su nueva IP publica. Los recursos consumen creditos del laboratorio mientras permanecen activos.

## Pruebas en Postman

Importar `postman/Libros_Semana4.postman_collection.json`. En la variable `baseUrl` colocar la direccion del servidor, sin `/libros`, por ejemplo `http://IP_PUBLICA:8080`. Ejecutar las ocho solicitudes en orden: listado, alta, consulta por ID, actualizacion, comprobacion del cambio, validacion 400, eliminacion del registro temporal y comprobacion 404.

| Metodo | Ruta | Resultado esperado |
| --- | --- | --- |
| GET | `/libros` | 200, lista de libros |
| GET | `/libros/{id}` | 200, libro consultado |
| POST | `/libros` | 201, libro con ID generado |
| PUT | `/libros/{id}` | 200, datos actualizados |
| DELETE | `/libros/{id}` | 204, sin cuerpo |

La coleccion guarda el ID del libro que crea y solo elimina ese registro. Se deben conservar los libros anteriores. El ID real no tiene por que comenzar en 1.

Para demostrar persistencia, consultar Oracle antes de eliminar el registro temporal:

```sql
SELECT id, titulo, autor, anio_publicacion, genero FROM libros ORDER BY id;
```

## Relacion con la pauta

| Criterio | Evidencia |
| --- | --- |
| Git | Historial del repositorio y commit Semana 4 |
| Patron en Spring Boot | Builder utilizado en LibroService.crear |
| Patron dentro del arquetipo | Libro y servicio incluidos en archetype-resources |
| Arquetipo propio implementado | Comandos de generacion, pruebas y JAR del proyecto generado |
| CRUD Oracle verificable | Coleccion Postman, API y consulta de persistencia |
| GitHub Actions y AWS, agregados a la actividad | Workflow y aplicacion desplegada en EC2 |

Las pruebas automatizadas se ejecutan con `.\mvnw.cmd verify`. Los resultados del pipeline y la URL real se revisan en GitHub Actions y en AWS; un archivo de workflow por si solo no demuestra que el despliegue haya finalizado.
