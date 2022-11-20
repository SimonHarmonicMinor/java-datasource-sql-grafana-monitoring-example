CREATE TABLE article
(
    id      UUID PRIMARY KEY,
    name    VARCHAR(200)  NOT NULL,
    content TEXT          NOT NULL,
    tags    VARCHAR(20)[] NOT NULL,
    likes   BIGINT        NOT NULL
);

CREATE TABLE comment
(
    id         UUID PRIMARY KEY,
    content    TEXT,
    article_id UUID NOT NULL REFERENCES article (id)
);