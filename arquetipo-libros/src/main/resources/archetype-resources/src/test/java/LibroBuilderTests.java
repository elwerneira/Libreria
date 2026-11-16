package ${package};

import ${package}.model.Libro;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LibroBuilderTests {
    @Test void builderConstruyeLosDatosSinAsignarUnaIdentidadDeBase() {
        Libro libro = Libro.builder().titulo("Papelucho").autor("Marcela Paz")
                .anioPublicacion(1947).genero("Infantil").build();
        assertThat(libro.getId()).isNull();
        assertThat(libro.getTitulo()).isEqualTo("Papelucho");
        assertThat(libro.getAutor()).isEqualTo("Marcela Paz");
        assertThat(libro.getAnioPublicacion()).isEqualTo(1947);
        assertThat(libro.getGenero()).isEqualTo("Infantil");
        assertThat(new Libro().getId()).isNull(); // JPA y JSON conservan el constructor sin argumentos.
    }
}
