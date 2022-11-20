package com.example.demo;

import com.example.demo.dto.ArticleCreateRequest;
import com.example.demo.dto.ArticleWithCommentsResponse;
import com.example.demo.jooq.generated.tables.daos.ArticleDao;
import com.example.demo.jooq.generated.tables.daos.CommentDao;
import com.example.demo.jooq.generated.tables.pojos.Article;
import com.example.demo.jooq.generated.tables.pojos.Comment;

import org.jooq.DSLContext;
import org.jooq.Records;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import static com.example.demo.jooq.generated.Tables.ARTICLE;
import static com.example.demo.jooq.generated.Tables.COMMENT;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final ArticleDao articleDao;
    private final CommentDao commentDao;
    private final DSLContext dsl;

    @GetMapping("/articles")
    public List<ArticleWithCommentsResponse> getArticles() {
        return dsl.select(
                ARTICLE.ID,
                ARTICLE.NAME,
                ARTICLE.LIKES,
                ARTICLE.TAGS.convertFrom(arr -> Arrays.stream(arr).toList()),
                multiset(
                    select(COMMENT.ID, COMMENT.CONTENT)
                        .from(COMMENT)
                        .where(COMMENT.ARTICLE_ID.eq(ARTICLE.ID))
                ).as("comments").convertFrom(r -> r.map(Records.mapping(ArticleWithCommentsResponse.Comment::new)))
            )
            .from(ARTICLE)
            .fetch(Records.mapping(ArticleWithCommentsResponse::new));
    }

    @PostMapping("/articles")
    public void createArticle(@NotNull @RequestBody ArticleCreateRequest request) {
        articleDao.insert(new Article(
            UUID.randomUUID(),
            request.getName(),
            request.getContent(),
            request.getTags().toArray(new String[0]),
            0L
        ));
    }

    @PostMapping("/articles/{articleId}/likes")
    public void likeTheArticle(@NotNull @PathVariable UUID articleId) {
        dsl.update(ARTICLE)
            .set(ARTICLE.LIKES, ARTICLE.LIKES.plus(1))
            .where(ARTICLE.ID.eq(articleId))
            .execute();
    }

    @PostMapping("/articles/{articleId}/comments")
    public void addComment(@NotNull @PathVariable UUID articleId,
                           @NotNull @RequestParam String content) {
        commentDao.insert(
            new Comment(
                UUID.randomUUID(),
                content,
                articleId
            )
        );
    }
}
