# Libros Semana 4

Proyecto de Desarrollo Full Stack III. Se crea un arquetipo Maven propio con el patron Builder, se genera un microservicio de Libros y se prepara su despliegue en AWS Academy mediante GitHub Actions. Se mantiene la misma base Oracle Biblioteca.

## Estructura

- `src`: microservicio base y pruebas.
- `arquetipo-libros`: plantilla Maven reutilizable con Builder y CRUD.
- `target/generado/libros-generado`: proyecto generado durante las pruebas y el pipeline; no se sube a Git.
- `.github/workflows/semana4.yml`: pruebas, generacion y despliegue del JAR generado.
- `infra/academy.json` y `deploy`: EC2, Systems Manager y actualizacion de versiones.
- `postman`: coleccion de ocho solicitudes para comprobar el CRUD.

## Empezar

Se necesita un JDK compatible con Java 21. Desde esta carpeta:

```powershell
.\mvnw.cmd verify
.\mvnw.cmd -B -ntp -f arquetipo-libros/pom.xml install
```

Los comandos completos para generar el proyecto estan en la [guia de Semana 4](README_SEMANA4.md) y en el [README del arquetipo](arquetipo-libros/README.md).

Para iniciar en local se necesita la carpeta `Wallet_Biblioteca` junto al pom.xml y `config/application-local.properties` con los accesos de Oracle. Usar el archivo de ejemplo como modelo. Despues ejecutar `.\mvnw.cmd spring-boot:run` y consultar `http://localhost:8080/libros`.

La wallet, los accesos y las credenciales de AWS no se suben al repositorio. Las pruebas automaticas usan H2; el servicio desplegado usa Oracle. No repetir los scripts SQL en la base existente.

## AWS y GitHub Actions

El workflow verifica el proyecto base, instala el arquetipo, genera otro proyecto y ejecuta sus pruebas. El despliegue utiliza el JAR de ese proyecto generado. AWS Academy requiere renovar los tres secretos temporales cuando caducan.

La infraestructura utiliza EC2 t3.micro, un bucket privado cifrado y el perfil existente LabInstanceProfile. El puerto 8080 se restringe a la IP de demostracion. El despliegue es de laboratorio y consume creditos mientras los recursos esten activos.

Consultar los resultados reales en la pestana Actions. La [guia detallada](README_SEMANA4.md) explica configuracion, Postman, credenciales temporales y correspondencia con la pauta.
