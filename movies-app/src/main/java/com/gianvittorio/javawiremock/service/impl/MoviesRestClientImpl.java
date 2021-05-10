package com.gianvittorio.javawiremock.service.impl;

import com.gianvittorio.javawiremock.constants.MoviesConstants;
import com.gianvittorio.javawiremock.exception.MovieErrorResponse;
import com.gianvittorio.javawiremock.service.MoviesRestClient;
import com.gianvittorio.javawiremock.web.dto.MovieDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
public class MoviesRestClientImpl implements MoviesRestClient {

    private final WebClient webClient;

    public MoviesRestClientImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<MovieDTO> retrieveAllMovies() {
        return webClient.get()
                .uri(MoviesConstants.GET_ALL_MOVIES_V1)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(MovieDTO.class)
                .collectList()
                .block();
    }

    @Override
    public MovieDTO retrieveMovieById(Long movieId) {
        try {
            return webClient.get()
                    .uri(MoviesConstants.GET_MOVIE_BY_ID_V1, movieId)
                    .retrieve()
                    .bodyToMono(MovieDTO.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMoviedById. Status code is {} and the message is {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception ex) {
            log.error("Exception in retrieveMoviedById and the message is {}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    @Override
    public List<MovieDTO> retrieveMoviesByName(String movieName) {
        String uri = UriComponentsBuilder.fromUriString(MoviesConstants.GET_MOVIE_BY_NAME_V1)
                .queryParam("movie_name", movieName)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(MovieDTO.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMoviedByName. Status code is {} and the message is {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception ex) {
            log.error("Exception in retrieveMoviedByName and the message is {}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    @Override
    public List<MovieDTO> retrieveMoviesByYear(Integer movieYear) {
        String uri = UriComponentsBuilder.fromUriString(MoviesConstants.GET_MOVIE_BY_YEAR_V1)
                .queryParam("year", movieYear)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(MovieDTO.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMovieByYear. Status code is {} and the message is {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception ex) {
            log.error("Exception in retrieveMovieByYear and the message is {}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    @Override
    public MovieDTO addMovie(MovieDTO movieDTO) {
        try {
            return webClient.post()
                    .uri(MoviesConstants.ADD_MOVIE_V1)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(movieDTO), MovieDTO.class)
                    .retrieve()
                    .bodyToMono(MovieDTO.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in addMovie. Status code is {} and the message is {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception ex) {
            log.error("Exception in addMovie and the message is {}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    @Override
    public MovieDTO updateMovie(Long movieId, MovieDTO movieDTO) {
        try {
            return webClient.put()
                    .uri(MoviesConstants.GET_MOVIE_BY_ID_V1, movieId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(movieDTO), MovieDTO.class)
                    .retrieve()
                    .bodyToMono(MovieDTO.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in updateMovie. Status code is {} and the message is {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception ex) {
            log.error("Exception in updateMovie and the message is {}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    @Override
    public String deleteMovieById(Long movieId) {
        try {
            return webClient.delete()
                    .uri(MoviesConstants.GET_MOVIE_BY_ID_V1, movieId)
                    .accept(MediaType.TEXT_PLAIN)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in deleteMovieById. Status code is {} and the message is {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception ex) {
            log.error("Exception in deleteMovieById and the message is {}", ex);
            throw new MovieErrorResponse(ex);
        }
    }
}
