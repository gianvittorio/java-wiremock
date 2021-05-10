package com.gianvittorio.javawiremock.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private String cast;

    private Long movie_id;

    private String name;

    private String releaseDate;

    private Integer year;
}
