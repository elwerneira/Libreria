package ${package};

import ${package}.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibroApiTests {
    @Autowired MockMvc mvc;
    @Autowired LibroRepository repository;
    @Test void listadoIncluyeLosCincoAtributosYReflejaElCrud() throws Exception {
        repository.deleteAll(); // Solo H2, en el perfil de pruebas.
        mvc.perform(get("/libros")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(post("/libros").contentType("application/json").content(
                "{\"titulo\":\"Libro de prueba\",\"autor\":\"Autora\",\"anioPublicacion\":2026,\"genero\":\"Novela\"}"))
                .andExpect(status().isCreated());
        long id = repository.findAll().getFirst().getId();
        mvc.perform(get("/libros"))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].titulo").value("Libro de prueba"))
                .andExpect(jsonPath("$[0].autor").value("Autora"))
                .andExpect(jsonPath("$[0].anioPublicacion").value(2026))
                .andExpect(jsonPath("$[0].genero").value("Novela"));
        mvc.perform(put("/libros/" + id).contentType("application/json").content(
                "{\"titulo\":\"Actualizado\",\"autor\":\"Autora\",\"anioPublicacion\":2026,\"genero\":\"Novela\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/libros/" + id)).andExpect(jsonPath("$.titulo").value("Actualizado"));
        mvc.perform(delete("/libros/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/libros/" + id)).andExpect(status().isNotFound());
    }
}
