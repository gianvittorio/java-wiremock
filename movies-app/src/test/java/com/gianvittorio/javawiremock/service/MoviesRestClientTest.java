package com.gianvittorio.javawiremock.service;

import com.gianvittorio.javawiremock.exception.MovieErrorResponse;
import com.gianvittorio.javawiremock.service.impl.MoviesRestClientImpl;
import com.gianvittorio.javawiremock.web.dto.MovieDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoviesRestClientTest {
    private MoviesRestClient moviesRestClient;

    @BeforeEach
    public void setUp() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8081/")
                .defaultHeader("accept", "application/json")
                .build();

        moviesRestClient = new MoviesRestClientImpl(webClient);
    }

    @Test
    @DisplayName("Must return movies list.")
    public void retrieveAllMoviesTest() {

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveAllMovies();

        // Then
        assertThat(movieDTOS)
                .isNotNull();

        assertThat(movieDTOS)
                .isNotEmpty();
    }

    @Test
    @DisplayName("Retrieve movie by id.")
    public void retrieveMovieByIdTest() {
        // Given
        Long id = 1l;

        // When
        MovieDTO movie = moviesRestClient.retrieveMovieById(id);

        // Then
        assertThat(movie)
                .isNotNull();
        assertThat(movie.getName())
                .isEqualTo("Batman Begins");
    }

    @Test
    @DisplayName("Get 404 Not Found on providing invalid movie id.")
    public void retrieveMovieByIdInvalidTest() {
        // Given
        Long id = 100l;

        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(id));
    }

    @Test
    @DisplayName("Retrieve movie by name.")
    public void retrieveMovieByNameTest() {
        // Given
        String movieName = "Avengers";

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveMoviesByName(movieName);

        // Then
        assertThat(movieDTOS)
                .isNotNull();
        assertThat(movieDTOS.size())
                .isEqualTo(4);
    }

    @Test
    @DisplayName("Get 404 Not Found on providing invalid movie name.")
    public void retrieveMovieByNameInvalidTest() {
        // Given
        String movieName = "ABC";
        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByName(movieName));
    }

    @Test
    @DisplayName("Retrieve movie by year.")
    public void retrieveMovieByYearTest() {
        // Given
        Integer year = 2012;

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveMoviesByYear(year);

        // Then
        assertThat(movieDTOS)
                .isNotNull();
        assertThat(movieDTOS.size())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Get 404 Not Found on providing invalid movie year.")
    public void retrieveMovieByYearInvalidTest() {
        // Given
        Integer year = 1960;
        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByYear(year));
    }

    @Test
    @DisplayName("Must add new movie.")
    public void addMovieTest() {
        // Given
        MovieDTO movieDTO = MovieDTO.builder()
                .movieId(null)
                .name("Toy Story 4")
                .cast("Tom Hanks, Tim Allen")
                .year(2019)
                .releaseDate(LocalDate.of(2019, 06, 20))
                .build();

        // When
        MovieDTO addedMovie = moviesRestClient.addMovie(movieDTO);

        // Then
        assertThat(addedMovie.getMovieId())
                .isNotNull();
    }

    @Test
    @DisplayName("Must get 400 Bad Request, once not providing suitable movieDTO.")
    public void addMovieInvalidTest() {
        // Given
        MovieDTO movieDTO = MovieDTO.builder()
                .movieId(null)
                .name(null)
                .cast("Tom Hanks, Tim Allen")
                .year(2019)
                .releaseDate(LocalDate.of(2019, 06, 20))
                .build();

        // Then
        String expectedErrorMessage = "Please pass all the input fields : [name]";
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(movieDTO), expectedErrorMessage);
    }

    @Test
    @DisplayName("Must update new movie.")
    public void updateMovieTest() {
        // Given
        Long movieId = 3l;
        String cast = "ABC";
        MovieDTO movieDTO = MovieDTO.builder()
                .movieId(movieId)
                .cast(cast)
                .build();

        // When
        MovieDTO addedMovie = moviesRestClient.updateMovie(movieDTO.getMovieId(), movieDTO);

        // Then
        assertThat(addedMovie.getMovieId())
                .isNotNull()
                .isEqualTo(movieId);
        assertThat(addedMovie.getCast())
                .contains(cast);
    }

    @Test
    @DisplayName("Must get 404 Not Found, once not providing suitable movieDTO.")
    public void updateMovieInvalidTest() {
        // Given
        Long movieId = 100l;
        String cast = "ABC";
        MovieDTO movieDTO = MovieDTO.builder()
                .movieId(movieId)
                .cast(cast)
                .build();

        // Then
        String expectedErrorMessage = "Please pass all the input fields : [name]";
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(movieId, movieDTO), expectedErrorMessage);
    }

    @Test
    @DisplayName("Must delete movie by Id.")
    public void deleteMovieByIdTest() {
        // Given
        MovieDTO movieDTO = MovieDTO.builder()
                .movieId(null)
                .name("Toy Story 5")
                .cast("Tom Hanks, Tim Allen")
                .year(2019)
                .releaseDate(LocalDate.of(2019, 06, 20))
                .build();
        MovieDTO addedMovie = moviesRestClient.addMovie(movieDTO);

        // When
        String responseMessage = moviesRestClient.deleteMovieById(addedMovie.getMovieId());

        // Then
        String expectedMessage = "Movie Deleted Successfully";
        assertThat(responseMessage)
                .isNotNull()
                .isNotBlank()
                .isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Must get 404 Not Found whenever deleting non-existing movie.")
    public void deleteMovieByIdInvalidTest() {
        // When
        long movieId = 100l;

        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovieById(movieId));
    }
}
