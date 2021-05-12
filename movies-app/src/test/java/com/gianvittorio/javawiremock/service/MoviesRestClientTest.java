package com.gianvittorio.javawiremock.service;

import com.gianvittorio.javawiremock.constants.MoviesConstants;
import com.gianvittorio.javawiremock.exception.MovieErrorResponse;
import com.gianvittorio.javawiremock.service.impl.MoviesRestClientImpl;
import com.gianvittorio.javawiremock.web.dto.MovieDTO;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoviesRestClientTest {
    private MoviesRestClient moviesRestClient;

    static WireMockServer wireMockServer;

    static int port;

    @BeforeAll
    public static void bootstrap() {
        WireMockConfiguration wireMockConfiguration = wireMockConfig()
                .port(8088)
                .notifier(new ConsoleNotifier(true))
                .extensions(new ResponseTemplateTransformer(true));
        wireMockServer = new WireMockServer(wireMockConfiguration);

        wireMockServer.start();

        port = wireMockServer.port();
    }

    @AfterAll
    public static void tearDownAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void setUp() {
        WebClient webClient = WebClient.builder()
                .baseUrl(String.format("http://localhost:%d/", port))
                .defaultHeader("accept", "application/json")
                .build();

        moviesRestClient = new MoviesRestClientImpl(webClient);

        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Must return movies list.")
    public void retrieveAllMoviesTest() {
        // Given
        wireMockServer.stubFor(
                get(anyUrl())
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("all-movies.json")
                        )
        );

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveAllMovies();

        // Then
        assertThat(movieDTOS)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @DisplayName("Must return movies list.")
    public void retrieveAllMoviesMatchesURLTest() {
        // Given
        wireMockServer.stubFor(
                get(urlPathEqualTo(MoviesConstants.GET_ALL_MOVIES_V1))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("all-movies.json")
                        )
        );

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveAllMovies();

        // Then
        assertThat(movieDTOS)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @DisplayName("Retrieve movie by id.")
    public void retrieveMovieByIdTest() {
        // Given
        wireMockServer.stubFor(
                get(urlPathMatching("/movieservice/v1/movie/\\d"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("movie.json")
                        )
        );

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
    @DisplayName("Retrieve movie by id.")
    public void retrieveMovieByIdWithResponseTemplatingTest() {
        // Given
        wireMockServer.stubFor(
                get(urlPathMatching("/movieservice/v1/movie/\\d"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("movie-template.json")
                        )
        );

        Long movieId = 9l;

        // When
        MovieDTO movie = moviesRestClient.retrieveMovieById(movieId);

        // Then
        assertThat(movie)
                .isNotNull();
        assertThat(movie.getName())
                .isEqualTo("Batman Begins");
        assertThat(movie.getMovieId())
                .isEqualTo(movieId);
    }

    @Test
    @DisplayName("Get 404 Not Found on providing invalid movie id.")
    public void retrieveMovieByIdInvalidTest() {
        // Given
        wireMockServer.stubFor(
                get(urlPathMatching("/movieservice/v1/movie/\\d+"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.NOT_FOUND.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("404-movieid.json")
                        )
        );

        Long id = 100l;

        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(id));
    }

    @Test
    @DisplayName("Retrieve movie by name.")
    public void retrieveMovieByNameTest() {
        // Given
        String movieName = "Avengers";

        wireMockServer.stubFor(
                get(urlEqualTo(MoviesConstants.GET_MOVIE_BY_NAME_V1
                                .concat("?movie_name=")
                                .concat(movieName)
                        )
                )
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("avengers.json")
                        )
        );

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveMoviesByName(movieName);

        // Then
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertThat(movieDTOS)
                .isNotNull();
        assertThat(movieDTOS.size())
                .isEqualTo(4);
        assertThat(movieDTOS.get(0).getCast())
                .isEqualTo(castExpected);
    }

    @Test
    @DisplayName("Retrieve movie by name.")
    public void retrieveMovieByNameApproach2Test() {
        // Given
        String movieName = "Avengers";

        wireMockServer.stubFor(
                get(urlPathEqualTo(MoviesConstants.GET_MOVIE_BY_NAME_V1))
                        .withQueryParam("movie_name", equalTo(movieName))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("avengers.json")
                        )
        );

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveMoviesByName(movieName);

        // Then
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertThat(movieDTOS)
                .isNotNull();
        assertThat(movieDTOS.size())
                .isEqualTo(4);
        assertThat(movieDTOS.get(0).getCast())
                .isEqualTo(castExpected);
    }

    @Test
    @DisplayName("Retrieve movie by name.")
    public void retrieveMovieByNameResponseTemplatingTest() {
        // Given
        String movieName = "Avengers";

        wireMockServer.stubFor(
                get(urlEqualTo(MoviesConstants.GET_MOVIE_BY_NAME_V1
                                .concat("?movie_name=")
                                .concat(movieName)
                        )
                )
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("movie-ByName-template.json")
                        )
        );

        // When
        List<MovieDTO> movieDTOS = moviesRestClient.retrieveMoviesByName(movieName);

        // Then
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertThat(movieDTOS)
                .isNotNull();
        assertThat(movieDTOS.size())
                .isEqualTo(4);
        assertThat(movieDTOS.get(0).getCast())
                .isEqualTo(castExpected);
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

        wireMockServer.stubFor(
                get(urlEqualTo(MoviesConstants.GET_MOVIE_BY_YEAR_V1.concat("?year=").concat(Integer.toString(year))))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("movie-ByYear.json")
                        )
        );

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

        wireMockServer.stubFor(
                get(urlEqualTo(MoviesConstants.GET_MOVIE_BY_YEAR_V1.concat("?year=").concat(Integer.toString(year))))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.BAD_REQUEST.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("404-movieYear.json")
                        )
        );

        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByYear(year));
    }

    @Test
    @DisplayName("Must add new movie.")
    public void addMovieTest() {
        // Given
        wireMockServer.stubFor(
                post(urlPathEqualTo(MoviesConstants.ADD_MOVIE_V1))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
                        .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("add-movie.json")
                        )
        );

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
    @DisplayName("Must add new movie.")
    public void addMovieResponseTemplatingTest() {
        // Given
        wireMockServer.stubFor(
                post(urlPathEqualTo(MoviesConstants.ADD_MOVIE_V1))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
                        .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("add-movie-template.json")
                        )
        );

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
        wireMockServer.stubFor(
                post(urlPathEqualTo(MoviesConstants.ADD_MOVIE_V1))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.BAD_REQUEST.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("400-invalid-input.json")
                        )
        );

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

        wireMockServer.stubFor(
                put(urlPathMatching("/movieservice/v1/movie/\\d+"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("update-movie-template.json")
                        )
        );

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

        wireMockServer.stubFor(
                put(urlPathMatching("/movieservice/v1/movie/\\d+"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.NOT_FOUND.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        )
        );

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

        String expectedErrorMessage = "Movie Deleted Successfully";

        wireMockServer.stubFor(
                post(urlPathEqualTo(MoviesConstants.ADD_MOVIE_V1))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 5")))
                        .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("add-movie.json")
                        )
        );

        MovieDTO addedMovie = moviesRestClient.addMovie(movieDTO);

        wireMockServer.stubFor(
                delete(urlPathMatching("/movieservice/v1/movie/\\d+"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(expectedErrorMessage)
                        )
        );

        // When
        String responseMessage = moviesRestClient.deleteMovieById(addedMovie.getMovieId());

        // Then

        assertThat(responseMessage)
                .isNotNull()
                .isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Must get 404 Not Found whenever deleting non-existing movie.")
    public void deleteMovieByIdInvalidTest() {
        // Given
        wireMockServer.stubFor(
                delete(urlPathMatching("/movieservice/v1/movie/\\d+"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.NOT_FOUND.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        )
        );

        // When
        long movieId = 100l;

        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovieById(movieId));
    }

    @Test
    @DisplayName("Must delete movie by name.")
    public void deleteMovieByNameTest() {
        // Given
        MovieDTO movieDTO = MovieDTO.builder()
                .movieId(null)
                .name("Toy Story 5")
                .cast("Tom Hanks, Tim Allen")
                .year(2019)
                .releaseDate(LocalDate.of(2019, 06, 20))
                .build();

        String expectedErrorMessage = "Movie Deleted Successfully";

        wireMockServer.stubFor(
                post(urlPathEqualTo(MoviesConstants.ADD_MOVIE_V1))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 5")))
                        .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("add-movie.json")
                        )
        );

        MovieDTO addedMovie = moviesRestClient.addMovie(movieDTO);

        wireMockServer.stubFor(
                delete(urlEqualTo(MoviesConstants.GET_MOVIE_BY_NAME_V1.concat("?movie_name=Toy%20Story%204")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        )
        );

        // When
        String responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());

        // Then
        assertThat(responseMessage)
                .isNotNull()
                .isEqualTo(expectedErrorMessage);

        wireMockServer.verify(exactly(1), postRequestedFor(urlPathEqualTo(MoviesConstants.ADD_MOVIE_V1))
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 5")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
        );

        wireMockServer.verify(exactly(1), deleteRequestedFor(urlEqualTo(MoviesConstants.GET_MOVIE_BY_NAME_V1.concat("?movie_name=Toy%20Story%204"))));
    }
}
