package ${package}.service;

import ${package}.model.Libro;
import ${package}.repository.LibroRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class LibroService {

    private final LibroRepository repository;

    public LibroService(LibroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Libro> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Libro obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
    }

    public Libro crear(Libro libro) {
        Libro nuevo = Libro.builder()
                .titulo(libro.getTitulo())
                .autor(libro.getAutor())
                .anioPublicacion(libro.getAnioPublicacion())
                .genero(libro.getGenero())
                .build();
        return repository.save(nuevo);
    }

    public Libro actualizar(Long id, Libro datos) {
        Libro libro = obtener(id);
        libro.setTitulo(datos.getTitulo());
        libro.setAutor(datos.getAutor());
        libro.setAnioPublicacion(datos.getAnioPublicacion());
        libro.setGenero(datos.getGenero());
        return repository.save(libro);
    }

    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }
}
