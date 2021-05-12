package com.gianvittorio.javawiremock.service;

import com.gianvittorio.javawiremock.exception.MovieErrorResponse;
import com.gianvittorio.javawiremock.service.impl.MoviesRestClientImpl;
import com.gianvittorio.javawiremock.web.dto.MovieDTO;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoviesRestClientServerFaultTest {
    private MoviesRestClient moviesRestClient;

    static int port;

    static WireMockServer wireMockServer;

    @BeforeAll
    public static void bootStrap() {
        WireMockConfiguration configuration = wireMockConfig()
                .port(8088)
                .notifier(new ConsoleNotifier((true)))
                .extensions(new ResponseTemplateTransformer(true));

        wireMockServer = new WireMockServer(configuration);

        wireMockServer.start();

        port = wireMockServer.port();
    }

    @AfterAll
    public static void shutDown() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void setUp() {
        wireMockServer.resetAll();

        WebClient webClient = WebClient.builder()
                .baseUrl(String.format("http://localhost:%d/", port))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        moviesRestClient = new MoviesRestClientImpl(webClient);
    }

    @Test
    @DisplayName("Must return 500 Internal Server Error.")
    public void retrieveAllMovies500InternalServerErrorTest() {
        // Given
        wireMockServer.stubFor(
                get(anyUrl())
                        .willReturn(
                                serverError()
                        )
        );

        // Then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
    }

    @Test
    @DisplayName("Must return 503 Service Unavailable.")
    public void retrieveAllMovies503ServiceUnavailableTest() {
        // Given
        String errorResponseMessage = "Service Unavailable";
        wireMockServer.stubFor(
                get(anyUrl())
                        .willReturn(
                                serverError()
                                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                                        .withBody(errorResponseMessage)
                        )
        );

        // When
        Throwable throwable = catchThrowable(() -> moviesRestClient.retrieveAllMovies());

        // Then
        assertThat(throwable)
                .isInstanceOf(MovieErrorResponse.class);
        assertThat(throwable.getLocalizedMessage())
                .isEqualTo(errorResponseMessage);
    }

    @Test
    @DisplayName("Must return 503 Service Unavailable.")
    public void retrieveAllMoviesFaultResponseTest() {
        // Given
        String errorResponseMessage = "org.springframework.web.reactive.function.client.WebClientRequestException: Connection prematurely closed BEFORE response; nested exception is reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        wireMockServer.stubFor(
                get(anyUrl())
                        .willReturn(
                                aResponse()
                                        .withFault(Fault.EMPTY_RESPONSE)
                        )
        );

        // When
        Throwable throwable = catchThrowable(() -> moviesRestClient.retrieveAllMovies());

        // Then
        assertThat(throwable)
                .isInstanceOf(MovieErrorResponse.class);
        assertThat(throwable.getLocalizedMessage())
                .isEqualTo(errorResponseMessage);
    }

    @Test
    @DisplayName("Must return 503 Service Unavailable.")
    public void retrieveAllMoviesRandomDataThenCloseTest() {
        // Given
        wireMockServer.stubFor(
                get(anyUrl())
                        .willReturn(
                                aResponse()
                                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
                        )
        );

        // When
        Throwable throwable = catchThrowable(() -> moviesRestClient.retrieveAllMovies());

        // Then
        assertThat(throwable)
                .isInstanceOf(MovieErrorResponse.class);
    }
}
