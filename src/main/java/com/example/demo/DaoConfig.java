package com.example.demo;

import com.example.demo.jooq.generated.tables.daos.ArticleDao;
import com.example.demo.jooq.generated.tables.daos.CommentDao;

import org.jooq.impl.DefaultConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DaoConfig {
    private final DefaultConfiguration defaultConfiguration;

    @Bean
    public ArticleDao articleDao() {
        return new ArticleDao(defaultConfiguration);
    }

    @Bean
    public CommentDao commentDao() {
        return new CommentDao(defaultConfiguration);
    }
}
