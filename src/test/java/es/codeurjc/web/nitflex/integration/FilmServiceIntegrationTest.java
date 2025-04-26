package es.codeurjc.web.nitflex.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.web.nitflex.ImageTestUtils;
import es.codeurjc.web.nitflex.dto.film.CreateFilmRequest;
import es.codeurjc.web.nitflex.dto.film.FilmDTO;
import es.codeurjc.web.nitflex.dto.film.FilmSimpleDTO;
import es.codeurjc.web.nitflex.model.Film;
import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.FilmRepository;
import es.codeurjc.web.nitflex.repository.UserRepository;

import es.codeurjc.web.nitflex.service.FilmService;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) 
@Transactional
class FilmServiceIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    public void setup() {
        User user1 = new User("Juan", "juan@example.com");
        User user2 = new User("Pedro", "Pedro@example.com");
        userRepository.save(user1);
        userRepository.save(user2);
    }
    
    @AfterEach
    public void teardown() {
        userRepository.deleteAll();
        filmRepository.deleteAll();
    }



    @Test
    void whenSaveValidFilm_thenItIsSavedInDatabase() {
        // GIVEN
        CreateFilmRequest request = new CreateFilmRequest("Interstellar", "Space travel", 2014, "PG-13");

        // WHEN
        FilmDTO savedFilm = filmService.save(request);

        // THEN
        assertNotNull(savedFilm);
        assertNotNull(savedFilm.id()); 
        assertEquals("Interstellar", savedFilm.title());

        // Comprobamos que realmente está en la base de datos
        assertTrue(filmRepository.existsById(savedFilm.id()));
    }

    @Test
    void whenUpdateFilmTitleAndSynopsis_thenChangesAreSaved() { //Without image
        // GIVEN
        CreateFilmRequest createFilmRequest = new CreateFilmRequest("Title", "synopsis", 2000, "PG");
        User user = userRepository.findByName("Juan").get();

        // Guardar pelicula y añadir usuario a la lista de usuarios que dieron like
        FilmDTO savedFilm = filmService.save(createFilmRequest);
        Long filmId = savedFilm.id();
        Film film = filmRepository.findById(filmId).get();
        film.getUsersThatLiked().add(user);  
        filmRepository.save(film);
        assertNotNull(savedFilm);
        assertNotNull(filmId);
        
        // Verificamos que el usuario esta en la lista
        FilmDTO updatedFilm = filmService.findOne(filmId).get();
        assertEquals(1, updatedFilm.usersThatLiked().size()); 
        
        // WHEN
        // Actualizamos nombre y sinopsis
        FilmSimpleDTO updateRequest = new FilmSimpleDTO(filmId, "New Title", "New Synopsis", 2000, "PG");
        filmService.update(filmId, updateRequest);

        // Obtenemos el filmDTO con los datos actualizados
        Optional<FilmDTO> updatedFilmDTO = filmService.findOne(filmId);

        // THEN
        assertNotNull(updatedFilmDTO);
        assertEquals(filmId, updatedFilmDTO.get().id());
        assertEquals("New Title", updatedFilmDTO.get().title());
        assertEquals("New Synopsis", updatedFilmDTO.get().synopsis());
       
        // verificamos que la lista de usuarios se mantuvo
        assertEquals(1, updatedFilmDTO.get().usersThatLiked().size()); 
    }

    @Test
    void whenEditFilm_thenChangesAreSaved() throws IOException, SQLException{
        //GIVEN
        CreateFilmRequest createFilmRequest = new CreateFilmRequest("Napoleon", "A french guy", 2023, "PG");
        
        //Create a film with a image
        MultipartFile image = ImageTestUtils.createSampleImage();
        FilmDTO savedFilm = filmService.save(createFilmRequest,image);

        //WHEN
        FilmSimpleDTO updateRequest = new FilmSimpleDTO(savedFilm.id(), "Napoleon Bonaparte", "A mad french guy", 2023, "PG");
        filmService.update(savedFilm.id(), updateRequest);

        //THEN
        Optional<FilmDTO> updatedFilmDTO = filmService.findOne(savedFilm.id());
        assertNotNull(updatedFilmDTO);
        assertEquals(savedFilm.id(), updatedFilmDTO.get().id());
        assertEquals("Napoleon Bonaparte", updatedFilmDTO.get().title());
        assertEquals("A mad french guy", updatedFilmDTO.get().synopsis());
        InputStream inputStream = filmService.getPosterFile(savedFilm.id());
        assertNotNull(inputStream);
        //Create the blob from the InputStream
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        assertTrue(ImageTestUtils.areSameBlob(new SerialBlob(image.getBytes()),new SerialBlob(buffer.toByteArray())));
    }

    @Test
    void whenDeleteAFilm_ThenShouldRemoveFilmAndUpdateUserFavorites() { 
        // GIVEN
        CreateFilmRequest createFilmRequest = new CreateFilmRequest("Title", "synopsis", 2000, "PG");
        User user1 = userRepository.findByName("Juan").get();
        User user2 = userRepository.findByName("Pedro").get();

        // Guardar pelicula y añade la pelicula a la lista de faoritos de lso 2 usuarios
        FilmDTO savedFilm = filmService.save(createFilmRequest);
        Long filmId = savedFilm.id();
        Film film = filmRepository.findById(filmId).get();

        user1.getFavoriteFilms().add(film);
        user2.getFavoriteFilms().add(film);

        film.getUsersThatLiked().add(user1);
        film.getUsersThatLiked().add(user2);

        filmRepository.save(film);
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        // Verificamos que la pelicula esta en la lista de favoritos de cada usuario
        assertTrue(user1.getFavoriteFilms().contains(film));
        assertTrue(user2.getFavoriteFilms().contains(film));
        // Verificamos que los usuarios esten en la lista de usuario que han dado like a la pelicula
        assertTrue(film.getUsersThatLiked().contains(user1));
        assertTrue(film.getUsersThatLiked().contains(user2));
        
        // WHEN
        // Borramos la pelicula
        filmService.delete(filmId);


        // THEN
        // Verificamos que la pelicula se ha borrado correctamente
        assertFalse(filmRepository.existsById(filmId));

        // Recargamos los usuarios para ver los datos actualizados
        User updatedUser1 = userRepository.findById(user1.getId()).get();
        User updatedUser2 = userRepository.findById(user2.getId()).get();
       
        // Verificamos la pelicula se elimino de la lista de favoritos de cada usuario
        assertFalse(updatedUser1.getFavoriteFilms().contains(film));
        assertFalse(updatedUser2.getFavoriteFilms().contains(film));
    }

}
