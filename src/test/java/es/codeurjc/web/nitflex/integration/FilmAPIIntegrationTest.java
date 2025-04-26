package es.codeurjc.web.nitflex.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

import org.json.JSONException;
import org.json.JSONObject;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import es.codeurjc.web.nitflex.dto.film.CreateFilmRequest;
import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmAPIIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
	private UserRepository userRepository;

    @BeforeEach
	public void setup() {
        RestAssured.port = port;
		User user = new User("User", "ejemplo@gmail.com");
		userRepository.save(user);
	}

	@AfterEach
	public void teardown() {
		userRepository.deleteAll();
	}
    


    @Test
    void whenCreateFilmWithoutImage_thenFilmCanBeRetrievedById() {
        // GIVEN
        String filmTitle = "Dune New Film";
        String filmDescription = "Sci-fi epic";
        int filmYear = 2021;
        String filmAgeRating = "PG-13";

        CreateFilmRequest request = new CreateFilmRequest(filmTitle, filmDescription, filmYear, filmAgeRating);

        // WHEN 
        long filmId = 
            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/films/")
            .then()
                .statusCode(CREATED.value()) // Verify HTTP 201 Created
                .extract()
                .jsonPath()
                .getLong("id");

        // THEN 
        given()
            .when()
                .get("/api/films/{id}", filmId)
            .then()
                .statusCode(OK.value()) // Verify HTTP 200 OK
                .contentType(ContentType.JSON)
                .body("id", equalTo((int) filmId))
                .body("title", equalTo(filmTitle))
                .body("synopsis", equalTo(filmDescription))
                .body("releaseYear", equalTo(filmYear))
                .body("ageRating", equalTo(filmAgeRating));

        // CLEANUP 
        given()
        .when()
            .delete("/api/films/{id}", filmId)
        .then()
            .statusCode(NO_CONTENT.value()); // Verify HTTP 204 No Content
        
    }

    @Test
    public void createFilmWithoutTitle_shouldReturnError() throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("title", ""); 
        requestBody.put("synopsis", "A film without a title");
        requestBody.put("year", 2021);
        requestBody.put("rating", "PG");    
        given()
            .contentType("application/json")
            .body(requestBody.toString())
        .when()
            .post("/api/films/")
        .then()
            .statusCode(400)
            .contentType("text/plain") 
            .body(equalTo("The title is empty"));
    }

    @Test
    public void whenEditTitle_thenTheFilmHasChanged(){
        // GIVEN
        String filmTitle = "Dune New Film";
        String filmDescription = "Sci-fi epic";
        int filmYear = 2021;
        String filmAgeRating = "PG-13";

        CreateFilmRequest request = new CreateFilmRequest(filmTitle, filmDescription, filmYear, filmAgeRating);

        long filmId = 
            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/films/")
            .then()
                .statusCode(CREATED.value()) // Verify HTTP 201 Created
                .extract()
                .jsonPath()
                .getLong("id");

        // WHEN 
        String newTitle = filmTitle + "- parte 2";
        CreateFilmRequest updateRequest = new CreateFilmRequest(newTitle, filmDescription, filmYear, filmAgeRating);

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/films/{id}", filmId)
        .then()
            .statusCode(OK.value()); // Verifica HTTP 200 OK

        // THEN 
        given()
            .when()
                .get("/api/films/{id}", filmId)
            .then()
                .statusCode(OK.value()) // Verify HTTP 200 OK
                .contentType(ContentType.JSON)
                .body("id", equalTo((int) filmId))
                .body("title", equalTo(newTitle)) //New part
                .body("synopsis", equalTo(filmDescription))
                .body("releaseYear", equalTo(filmYear))
                .body("ageRating", equalTo(filmAgeRating));

        // CLEANUP 
        given()
        .when()
            .delete("/api/films/{id}", filmId)
        .then()
            .statusCode(NO_CONTENT.value()); // Verify HTTP 204 No Content

    }

    @Test
    public void whenCreateAndDeleteFilm_thenNotAvailableAnymore(){
        // GIVEN
        String filmTitle = "Dune New Film";
        String filmDescription = "Sci-fi epic";
        int filmYear = 2021;
        String filmAgeRating = "PG-13";

        CreateFilmRequest request = new CreateFilmRequest(filmTitle, filmDescription, filmYear, filmAgeRating);

        long filmId = 
            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/films/")
            .then()
                .statusCode(CREATED.value()) // Verify HTTP 201 Created
                .extract()
                .jsonPath()
                .getLong("id");

        // WHEN 

        given()
        .when()
            .delete("/api/films/{id}", filmId)
        .then()
            .statusCode(NO_CONTENT.value()); // Verifica HTTP 204 No Content

        // THEN 
        given()
            .when()
                .get("/api/films/{id}", filmId)
            .then()
                .statusCode(NOT_FOUND.value()); // Verify HTTP 404 Not Found
                
                

    }

}
