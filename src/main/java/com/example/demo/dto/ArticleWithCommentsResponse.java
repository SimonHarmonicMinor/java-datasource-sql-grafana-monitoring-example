package com.example.demo.dto;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ArticleWithCommentsResponse {
    @NotNull
    private final UUID id;
    @NotNull
    private final String name;
    @NotNull
    private final long likes;
    @NotNull
    private final List<String> tags;
    private final List<Comment> comments;

    @Data
    public static class Comment {
        @NotNull
        private final UUID id;
        @NotNull
        private final String content;
    }
}
