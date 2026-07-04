package com.animalgym.api.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionChecker implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnectionChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
            System.out.println("========================================");
            System.out.println("DATABASE CONNECTED");
            System.out.println("========================================");
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("DATABASE CONNECTION FAILED: " + e.getMessage());
            System.err.println("========================================");
        }
    }
}
