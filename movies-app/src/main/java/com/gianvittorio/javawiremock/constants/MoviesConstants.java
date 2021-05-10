package com.gianvittorio.javawiremock.constants;

import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

public interface MoviesConstants {
    static final String GET_ALL_MOVIES_V1 = "/movieservice/v1/allMovies";
    static final String GET_MOVIE_BY_ID_V1 = "/movieservice/v1/movie/{id}";
    static final String GET_MOVIE_BY_NAME_V1 = "/movieservice/v1/movieName";
    static final String GET_MOVIE_BY_YEAR_V1 = "/movieservice/v1/movieYear";
    static final String ADD_MOVIE_V1 = "/movieservice/v1/movie";
}
