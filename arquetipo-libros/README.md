# Arquetipo Maven de Libros

Plantilla propia de Semana 4. Incluye controller, service, repository, entidad Libro, patron Builder, validaciones, scripts SQL, Maven Wrapper y pruebas H2. No contiene wallets ni accesos reales de Oracle.

Desde la raiz del repositorio, instalar en el catalogo local:

```powershell
.\mvnw.cmd -B -ntp -f arquetipo-libros/pom.xml install
```

Generar otro proyecto con sus propias coordenadas y paquete:

```powershell
.\mvnw.cmd -B -ntp org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate "-DarchetypeGroupId=cl.duoc.fullstack" "-DarchetypeArtifactId=libros-arquetipo" "-DarchetypeVersion=1.0.0" "-DgroupId=cl.duoc.fullstack" "-DartifactId=libros-generado" "-Dversion=1.0.0-SNAPSHOT" "-Dpackage=cl.duoc.biblioteca" "-DoutputDirectory=target/generado" "-DinteractiveMode=false" "-DarchetypeCatalog=local"
.\mvnw.cmd -B -ntp -f target/generado/libros-generado/pom.xml verify
```

No repetir la generacion en una carpeta que ya tenga ese proyecto. Para otra prueba, cambiar artifactId o outputDirectory.

El archivo `src/main/resources/META-INF/maven/archetype-metadata.xml` define que se copia y que archivos Java se adaptan al nuevo paquete. Las clases de la plantilla estan en `src/main/resources/archetype-resources`. El patron Builder se usa en `LibroService.crear`: construye un nuevo libro sin aceptar un ID proporcionado por el cliente.

Para actualizar la plantilla despues de modificar el proyecto base, ejecutar `python scripts/actualizar-arquetipo.py` desde la raiz con Python 3 instalado. El script copia una lista explicita de archivos y excluye configuracion privada, wallets y archivos generados. Despues se debe instalar y generar otra vez para comprobar el resultado.
