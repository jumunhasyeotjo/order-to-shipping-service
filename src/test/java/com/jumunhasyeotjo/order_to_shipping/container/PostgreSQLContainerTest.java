package com.jumunhasyeotjo.order_to_shipping.container;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class PostgreSQLContainerTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Test
    void checkContainerRunning() {
        System.out.println("JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("Username: " + postgres.getUsername());
        System.out.println("Password: " + postgres.getPassword());
    }
}