package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ArticleCreateRequest {
    @NotNull
    private final String name;
    @NotNull
    private final String content;
    @NotNull
    private final List<String> tags;

    @JsonCreator
    public ArticleCreateRequest(String name, String content, List<String> tags) {
        this.name = name;
        this.content = content;
        this.tags = tags;
    }
}
