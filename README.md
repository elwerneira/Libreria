# Libros

En este proyecto desarrollé un microservicio para gestionar libros de una biblioteca, como parte de la actividad de la Semana 1 de Desarrollo Full Stack III. Utilicé Java con Spring Boot, JPA para guardar y consultar los datos, y una base de datos Oracle en la nube.

Cada libro tiene un ID, título, autor, año de publicación y género. Mediante la API puedo crear un libro, buscarlo por su ID, actualizar sus datos y eliminarlo.

## Para ejecutar el proyecto

Necesito un JDK compatible de versión 21 o superior, conexión a Internet y los accesos a la base de datos Oracle Cloud. El proyecto incluye Maven Wrapper, por lo que no es necesario instalar Maven por separado.

La conexión utiliza una wallet. La wallet y el archivo con las credenciales se entregan por separado y están fuera de Git.

## Configurar la conexión a Oracle

Primero debo descomprimir la wallet en una carpeta del equipo, conservando todos sus archivos.

Dentro de la carpeta del proyecto debo tener el archivo `config/application-local.properties`, con esta estructura:

```properties
spring.datasource.username=USUARIO
spring.datasource.password=CLAVE
spring.datasource.url=jdbc:oracle:thin:@biblioteca_low?TNS_ADMIN="C:/ruta/Wallet_Biblioteca"
```

Debo reemplazar USUARIO y CLAVE por los accesos reales de Oracle, y ajustar TNS_ADMIN a la carpeta donde descomprimí la wallet. Las comillas permiten usar una ruta que contenga espacios.

El archivo principal `src/main/resources/application.properties` carga esta configuración local. Al clonar el repositorio debo crear el archivo local, porque no se descarga desde GitHub.

Al usar otro equipo, debo ajustar la ruta de la wallet y JAVA_HOME al JDK instalado. También debe tener acceso de red a Oracle Cloud por el puerto 1522; si la base tiene restricciones por IP, debo autorizar la IP pública de esa conexión.

Al conectarme a la misma base de datos tendré los mismos libros. La tabla LIBROS ya está creada y no necesito volver a ejecutar el script SQL. La opción `spring.jpa.hibernate.ddl-auto=update` permite que JPA cree o actualice la estructura de la tabla al iniciar.

## Iniciar servidor

```powershell
cd "C:\Users\Elwer\Documents\Duoc\FullStack lll\Libros"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-27.0.1.0.3"
.\mvnw.cmd spring-boot:run
```

Las rutas anteriores corresponden a mi equipo; en otro computador debo cambiarlas. La primera ejecución puede tardar mientras Maven descarga las dependencias.

Espero a que aparezca el mensaje `Started LibrosApplication`. La API queda disponible en `http://localhost:8080` y debo mantener la terminal abierta mientras realizo las pruebas.

La aplicación es una API REST y no tiene una página de inicio. Abrir solamente `http://localhost:8080/` devuelve 404; para consultar un libro debo usar `/libros/{id}` con un ID existente.

## Probar el CRUD en Postman

Uso `http://localhost:8080` como dirección base.

| Método | Ruta | Resultado esperado |
| --- | --- | --- |
| POST | `/libros` | 201 Created y el libro con su ID generado |
| GET | `/libros/{id}` | 200 OK y los datos del libro |
| PUT | `/libros/{id}` | 200 OK y los datos actualizados |
| DELETE | `/libros/{id}` | 204 No Content |

Para crear un libro selecciono **POST**, escribo `http://localhost:8080/libros` y en **Body → raw → JSON** ingreso:

```json
{
  "titulo": "Papelucho",
  "autor": "Marcela Paz",
  "anioPublicacion": 1947,
  "genero": "Infantil"
}
```

Oracle genera el ID automáticamente. Para consultar, actualizar o eliminar el libro debo usar el ID recibido en la respuesta del POST, sin asumir que comienza en 1.

Para actualizar uso PUT con el ID en la URL y envío los cuatro atributos del libro en el JSON. El ID se mantiene. PUT actualiza un libro existente; si el ID fue eliminado o no existe, responde 404. GET y DELETE también responden 404 cuando el libro no existe. Los datos incompletos o inválidos en POST y PUT responden 400.

GET a `/libros` sin indicar un ID devuelve 405, porque no se implementó una consulta para listar todos los libros.

Puedo comprobar los datos guardados directamente en Oracle con:

```sql
SELECT * FROM libros;
```

## Repositorio

El código está disponible en [GitHub](https://github.com/elwerneira/Libreria). La wallet y el archivo `config/application-local.properties` se comparten por un canal privado y no se suben al repositorio.
