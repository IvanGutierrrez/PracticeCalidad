package es.codeurjc.web.nitflex.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.web.nitflex.dto.film.CreateFilmRequest;
import es.codeurjc.web.nitflex.dto.film.FilmDTO;
import es.codeurjc.web.nitflex.dto.film.FilmMapper;
import es.codeurjc.web.nitflex.model.Film;
import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.FilmRepository;
import es.codeurjc.web.nitflex.repository.UserRepository;
import es.codeurjc.web.nitflex.service.FavoriteFilmService;
import es.codeurjc.web.nitflex.service.FilmService;
import es.codeurjc.web.nitflex.service.UserComponent;
import es.codeurjc.web.nitflex.service.exceptions.FilmNotFoundException;
import es.codeurjc.web.nitflex.utils.ImageUtils;

class FilmServiceUnitTest {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FavoriteFilmService favoriteFilmService;

    @Mock
    private UserComponent userComponent;

    @Mock
    private ImageUtils imageUtils;

    @Mock
    private FilmMapper filmMapper;

    @InjectMocks
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenSaveValidFilm_thenFilmIsSaved() {
        // GIVEN
        CreateFilmRequest request = new CreateFilmRequest("Inception", "Great movie", 2010, "PG-13");
        Film film = new Film(); // Simula la entidad Film
        film.setId(1L);
        film.setTitle("Inception");

        FilmDTO filmDTO = new FilmDTO(1L, "Inception", "Great movie", 2010, "PG-13", null, null);

        // Configurar mocks
        when(filmMapper.toDomain(request)).thenReturn(film);
        when(filmRepository.save(any(Film.class))).thenReturn(film);
        when(filmMapper.toDTO(film)).thenReturn(filmDTO);

        // WHEN
        FilmDTO result = filmService.save(request);

        // THEN
        assertNotNull(result);
        assertEquals("Inception", result.title());
        verify(filmRepository, times(1)).save(any(Film.class));

        // CLEANUP
        filmRepository.deleteById(film.getId());
    }

    @ParameterizedTest(name = "Attempting to save film with invalid title: {0}")
    @NullAndEmptySource // Prueba con title = null y title = ""
    void whenSaveFilmWithInvalidTitle_thenThrowExceptionAndNotSaved(String invalidTitle) {
        // GIVEN
        CreateFilmRequest request = new CreateFilmRequest(invalidTitle, "Great movie", 2010, "PG-13");
        
        // WHEN
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> filmService.save(request));
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> filmService.save(request, (Blob) null));
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> filmService.save(request, (MultipartFile) null));
        // THEN
        assertEquals("The title is empty", exception1.getMessage());
        assertEquals("The title is empty", exception2.getMessage());
        assertEquals("The title is empty", exception3.getMessage());
        
        // Comprueba que el repositorio nunca guarda la película
        verify(filmRepository, never()).save(any(Film.class));

        // Comprueba que la pelicula nunca se convierte a DTO
        verify(filmMapper, never()).toDTO(any(Film.class));

        // Comprueba que la pelicula nunca se convierte a Entidad
        verify(filmMapper, never()).toDomain(any(CreateFilmRequest.class));
    }   
    

    @Test
    void whenDeleteFilm_thenFilmIsDeleted() {
        // GIVEN
        CreateFilmRequest request = new CreateFilmRequest("Inception", "Great movie", 2010, "PG-13");
        Film film = new Film(); // Simula la entidad Film
        film.setId(1L);
        film.setTitle("Inception");

        FilmDTO filmDTO = new FilmDTO(1L, "Inception", "Great movie", 2010, "PG-13", null, null);

        // Configurar mocks
        when(filmMapper.toDomain(request)).thenReturn(film);
        when(filmRepository.save(any(Film.class))).thenReturn(film);
        when(filmRepository.findById(1L)).thenReturn(Optional.of(film));  // Simula que el repositorio encuentra la película
        when(filmMapper.toDTO(film)).thenReturn(filmDTO);

        filmService.save(request);

        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setFavoriteFilms(new ArrayList<>()); // Lista vacía al inicio
        userRepository.save(user);

        when(userComponent.getUser()).thenReturn(user);
        favoriteFilmService.addToFavorites(1L);

        // WHEN
        filmService.delete(1L);

        // THEN
        assertTrue(filmService.findAll().isEmpty()); //Compruebas que se elimina del repostorio
        assertFalse(user.getFavoriteFilms().contains(film)); // Compruebas que se elimina de la lista de favoritos del usuario
    }

    @Test
    void testDelete_FilmNotFound_ShouldThrowException() {
        // GIVEN
        long filmId = 1L; //Creamos un ID que no existe
        Mockito.when(filmRepository.findById(filmId)).thenReturn(Optional.empty()); // Configuramos el filmRepository

        // WHEN
        //LLamamos al metodo con el  nuevo Id y capturamos la excepción.
        FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmService.delete(filmId));

        // THEN
        assertEquals("Film not found with id: " + filmId, exception.getMessage()); //Comparamos que el mensaje de la excepcion es igual al de la clase FilmNotFoundException.java
        Mockito.verify(filmRepository, Mockito.never()).deleteById(filmId); //Comprobamos que nunca se llama al metodo deleteById del filmRepository
    }
}
