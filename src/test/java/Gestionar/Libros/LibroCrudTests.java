package Gestionar.Libros;

import Gestionar.Libros.repository.LibroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibroCrudTests {

    @Autowired private MockMvc mvc;
    @Autowired private LibroRepository repository;

    private static final String LIBRO = """
            {"titulo":"Papelucho","autor":"Marcela Paz","anioPublicacion":1947,"genero":"Infantil"}
            """;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void crearConsultarActualizarYEliminarLibro() throws Exception {
        String location = mvc.perform(post("/libros").contentType("application/json").content(LIBRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Papelucho"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor").value("Marcela Paz"))
                .andExpect(jsonPath("$.anioPublicacion").value(1947))
                .andExpect(jsonPath("$.genero").value("Infantil"));

        mvc.perform(put(location).contentType("application/json")
                        .content(LIBRO.replace("Papelucho", "Papelucho detective")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Papelucho detective"));
        mvc.perform(get(location)).andExpect(jsonPath("$.titulo").value("Papelucho detective"));

        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void idInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/libros/999999")).andExpect(status().isNotFound());
        mvc.perform(put("/libros/999999").contentType("application/json").content(LIBRO))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/libros/999999")).andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void datosInvalidosNoSeGuardan() throws Exception {
        mvc.perform(post("/libros").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/libros").contentType("application/json")
                        .content(LIBRO.replace("1947", "-1")))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    void postConIdExistenteCreaOtroLibro() throws Exception {
        mvc.perform(post("/libros").contentType("application/json").content(LIBRO))
                .andExpect(status().isCreated());
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(post("/libros").contentType("application/json")
                        .content(LIBRO.replace("{", "{\"id\":" + id + ",")))
                .andExpect(status().isCreated());
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findById(id).orElseThrow().getTitulo()).isEqualTo("Papelucho");
    }
}
