# Verificacion de Semana 4

Se completo el desarrollo del arquetipo Maven con Builder y el despliegue del microservicio generado en AWS Academy. El codigo se publico con el mensaje de commit `Semana 4`.

## Evidencia de generacion y pruebas

- El proyecto base paso sus 7 pruebas con H2.
- El arquetipo `cl.duoc.fullstack:libros-arquetipo:1.0.0` se instalo y genero un proyecto con paquete `cl.duoc.biblioteca`.
- El proyecto generado paso sus 7 pruebas y produjo el JAR que se despliega.
- GitHub Actions completo las etapas `verificar` y `desplegar`. La primera ejecucion de despliegue fallo porque Biblioteca estaba detenida; despues de iniciar la base, se repitio la etapa fallida y finalizo correctamente.
- Ejecucion: https://github.com/elwerneira/Libreria/actions/runs/43785632025

## Infraestructura utilizada

- Region AWS: us-east-1.
- Stack: `fullstack3-libros-s4-ec2`.
- EC2 t3.micro con LabInstanceProfile, administrada por Systems Manager.
- Bucket privado cifrado para el JAR, la wallet y la configuracion de Oracle.
- API: http://98.82.122.241:8080/libros
- Acceso al puerto 8080 restringido a la IP de demostracion.

No se guardaron wallets ni valores de credenciales en el repositorio. Los tres secretos temporales de AWS Academy se configuraron en GitHub Actions y deben renovarse cuando caduquen.

## CRUD real contra AWS y Oracle

La comprobacion HTTP creo un libro temporal, verifico el cambio de genero, lo elimino y comparo la lista final con la inicial.

| Solicitud | Resultado |
| --- | --- |
| GET /libros | 200, tres libros existentes |
| POST /libros | 201, ID asignado por Oracle |
| GET /libros/{id temporal} | 200, los datos enviados |
| PUT /libros/{id temporal} | 200, genero actualizado |
| GET /libros/{id temporal} | 200, cambio persistido |
| POST /libros con datos vacios | 400 |
| DELETE /libros/{id temporal} | 204 |
| GET del ID eliminado | 404 |
| GET /libros final | 200, los mismos tres libros originales |

Se verificaron 9 solicitudes HTTP. La coleccion de Postman contiene 8 solicitudes para repetir el recorrido; el noveno GET corresponde a la comprobacion adicional de conservacion de datos. Esta evidencia se obtuvo mediante un script HTTP y no equivale a una grabacion ni a una ejecucion en la interfaz de Postman.

## Antes de demostrar

1. Confirmar que el Learner Lab esta activo y que la EC2 esta iniciada.
2. Confirmar que Oracle muestra la base visible como Libros, con nombre interno Biblioteca, iniciada. Durante el diagnostico estaba detenida y devolvia ORA-12514; la wallet existente funciono al iniciar la base.
3. Usar la IP publica actual y comprobar que la IP del equipo este permitida en el grupo de seguridad.
4. Importar `postman/Libros_Semana4.postman_collection.json`. Su baseUrl apunta a esta instancia y se debe actualizar si cambia la IP.
5. Mantener los registros originales y usar un libro temporal para la prueba.

El despliegue utiliza HTTP restringido por IP para la demostracion del laboratorio. Los recursos de EC2 siguen consumiendo creditos mientras permanecen activos.
