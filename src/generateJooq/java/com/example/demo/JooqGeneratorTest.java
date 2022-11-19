package com.example.demo;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.SneakyThrows;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@JooqTest
@Transactional(propagation = NOT_SUPPORTED)
@Testcontainers
class JooqGeneratorTest {
    @Autowired
    private Environment environment;

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl());
        registry.add("spring.datasource.username", () -> POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> POSTGRES.getPassword());
    }

    @Test
    @SneakyThrows
    void generateJooq() {
        new GenerationTool().run(
            new Configuration()

                .withJdbc(new Jdbc()
                    .withDriver(environment.getProperty("spring.datasource.driver-class-name", String.class))
                    .withUrl(environment.getProperty("spring.datasource.url", String.class))
                    .withUser(environment.getProperty("spring.datasource.username", String.class))
                    .withPassword(environment.getProperty("spring.datasource.password", String.class)))
                .withGenerator(
                    new Generator()
                        .withGenerate(
                            new Generate()
                                .withPojos(true)
                                .withDaos(true)
                        )
                        .withDatabase(
                            new Database()
                                .withIncludes("^profile.*")
                        )
                        .withTarget(
                            new Target()
                                .withPackageName("com.example.demo.jooq.generated")
                                .withDirectory("build/generated/sources/annotationProcessor/java/main")
                        )
                )
        );
    }
}
