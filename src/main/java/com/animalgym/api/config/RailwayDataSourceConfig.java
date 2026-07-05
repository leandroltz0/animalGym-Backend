package com.animalgym.api.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class RailwayDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            URI uri = URI.create(databaseUrl);
            String userInfo = uri.getUserInfo();
            String username = userInfo != null ? userInfo.split(":")[0] : "";
            String password = userInfo != null && userInfo.contains(":") ? userInfo.split(":")[1] : "";
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        String pgHost = env.getProperty("PGHOST");
        if (pgHost != null && !pgHost.isBlank()) {
            String pgPort = env.getProperty("PGPORT", "5432");
            String pgDatabase = env.getProperty("PGDATABASE", "railway");
            String pgUser = env.getProperty("PGUSER", "postgres");
            String pgPassword = env.getProperty("PGPASSWORD", "");
            String jdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(pgUser)
                    .password(pgPassword)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        return DataSourceBuilder.create()
                .url(env.getProperty("spring.datasource.url",
                        "jdbc:postgresql://localhost:5432/animalgym-db"))
                .username(env.getProperty("spring.datasource.username", "postgres"))
                .password(env.getProperty("spring.datasource.password", "postgres"))
                .driverClassName(env.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver"))
                .build();
    }
}
