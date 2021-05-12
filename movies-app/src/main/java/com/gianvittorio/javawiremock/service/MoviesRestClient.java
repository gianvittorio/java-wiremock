package com.gianvittorio.javawiremock.service;

import com.gianvittorio.javawiremock.web.dto.MovieDTO;

import java.util.List;

public interface MoviesRestClient {

    List<MovieDTO> retrieveAllMovies();

    MovieDTO retrieveMovieById(Long id);

    List<MovieDTO> retrieveMoviesByName(String movieName);

    List<MovieDTO> retrieveMoviesByYear(Integer movieYear);

    MovieDTO addMovie(MovieDTO movieDTO);

    MovieDTO updateMovie(Long movieId, MovieDTO movieDTO);

    String deleteMovieById(Long movieId);

    String deleteMovieByName(String name);
}
