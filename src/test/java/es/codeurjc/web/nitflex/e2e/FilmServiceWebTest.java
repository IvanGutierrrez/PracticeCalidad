package es.codeurjc.web.nitflex.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import es.codeurjc.web.nitflex.Application;
import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.UserRepository;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmServiceWebTest {

	@LocalServerPort
	int port;

	@Autowired
	private UserRepository userRepository;

	private WebDriver driver;
	private WebDriverWait wait;

	@BeforeEach
	public void setup() {
		this.driver = new ChromeDriver();
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		User user = new User("User", "ejemplo@gmail.com");
		userRepository.save(user);
	}

	@AfterEach
	public void teardown() {
		if (driver != null) {
			driver.quit();
		}
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("when create a film, it redirects to the film's detail page")
	public void whenCreateFilm_thenRedirectToFilmDetailPage() {
		// GIVEN
		driver.get("http://localhost:" + this.port + "/");

		// WHEN
		String filmTitle = "Inception";
		createFilmAux(filmTitle);

		// THEN
		this.wait.until(ExpectedConditions.presenceOfElementLocated(By.id("film-title")));

		String displayedTitle = driver.findElement(By.id("film-title")).getText();
		assertEquals(filmTitle, displayedTitle, "The film detail page should display the correct film title");

		//Clean up
		driver.findElement(By.id("remove-film")).click();
    	this.wait.until(ExpectedConditions.textToBe(By.id("message"), "Film '" + filmTitle + "' deleted"));
	}

	@Test
	@DisplayName("when creating a film without a title, an error message appears and the film is not listed")
	public void whenCreateFilmWithNoTitle_thenShowErrorAndFilmNotListedInLanding() {
		// GIVEN
		driver.get("http://localhost:" + this.port + "/");

		// WHEN
		driver.findElement(By.id("create-film")).click();
		driver.findElement(By.id("Save")).click();

		// THEN
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-list")));
		String errorMessage = driver.findElement(By.id("error-list")).getText();
		assertEquals("The title is empty", errorMessage, "Error message should appear when user tries to make a film without a name");

		// Back to landing page
		driver.get("http://localhost:" + this.port + "/");
		List<WebElement> films = driver.findElements(By.cssSelector(".four.wide.column.film .header.film-title"));

		for (WebElement film : films) {
			String filmTitle = film.getText().trim();
			assertFalse(filmTitle.isEmpty(), "There should not be a film with an empty name");
		}
	}


	@Test
	@DisplayName("when create a film and delete it then the film disappear in the film's list")
	public void whenCreateAndDeleteFilm_thenFilmDontInFilmList() {
		// GIVEN
		driver.get("http://localhost:" + this.port + "/");

		// when
		createFilmAux("Napoleon");
		wait.until(ExpectedConditions.elementToBeClickable(By.id("remove-film"))).click();

		// THEN
		this.wait.until(ExpectedConditions.textToBe(By.id("message"), "Film 'Napoleon' deleted"));
		driver.findElement(By.id("all-films")).click();
		assertThrows(NoSuchElementException.class, () -> {
			driver.findElement(By.linkText("Napoleon"));
		});
	}

	@Test
	@DisplayName("when creating a film, editing it, and verifying the changes, the film's details are updated correctly")
	public void whenCreateAndEditFilmTitle_thenChangeIsApplied() {
		// GIVEN
		driver.get("http://localhost:" + this.port + "/");

		// WHEN
		String title = "Jurassic World";
		createFilmAux(title);
		wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-film"))).click();

		// Añadimos '- parte 2' al titulo de la pelicula y guardamos los cambios
		String newTitle = title + "- parte 2";
		driver.findElement(By.name("title")).sendKeys("- parte 2");

		driver.findElement(By.id("Save")).click();


		// THEN
		this.wait.until(ExpectedConditions.presenceOfElementLocated(By.id("film-title")));
		String updatedFilmTitle = driver.findElement(By.id("film-title")).getText();
		assertEquals(newTitle, updatedFilmTitle);
	}


	private void createFilmAux(String title) {
		driver.findElement(By.id("create-film")).click();

		driver.findElement(By.name("title")).sendKeys(title);

		driver.findElement(By.id("Save")).click();
	}
}
