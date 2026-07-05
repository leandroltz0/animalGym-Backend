# Plan: Fix conexión a Railway PostgreSQL

## Problema
`RailwayDataSourceConfig` tiene `@ConditionalOnProperty(name = "DATABASE_URL")` y solo soporta `DATABASE_URL`. Railway además provee variables individuales (`PGHOST`, `PGPORT`, etc.) que se setean automáticamente al linkear la DB. Si la referencia `${{Postgres.DATABASE_URL}}` no se resuelve, el backend cae al default de `application.properties` (localhost).

## Fix

### `RailwayDataSourceConfig.java` — quitar `@ConditionalOnProperty` y agregar soporte dual

```java
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
        // 1. Intentar con DATABASE_URL (formato: postgresql://user:pass@host:port/db)
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

        // 2. Intentar con variables individuales de Railway (PGHOST, PGPORT, etc.)
        String pgHost = env.getProperty("PGHOST");
        if (pgHost != null && !pgHost.isBlank()) {
            String pgPort = env.getProperty("PGPORT", "5432");
            String pgDatabase = env.getProperty("PGDATABASE", "railway");
            String pgUser = env.getProperty("PGUSER", "postgres");
            String pgPassword = env.getProperty("PGPASSWORD", "");

            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase)
                    .username(pgUser)
                    .password(pgPassword)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        // 3. Fallback: usar propiedades de application.properties (desarrollo local)
        return DataSourceBuilder.create()
                .url(env.getProperty("spring.datasource.url",
                        "jdbc:postgresql://localhost:5432/animalgym-db"))
                .username(env.getProperty("spring.datasource.username", "postgres"))
                .password(env.getProperty("spring.datasource.password", "postgres"))
                .driverClassName(env.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver"))
                .build();
    }
}
```

## Opcional: En Railway (sin tocar código)

Si preferís no modificar el código, borrá todas las variables de DB y agregá una sola:

| Variable | Valor |
|----------|-------|
| `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` (o el nombre exacto de tu servicio PostgreSQL) |

Para saber el nombre exacto, andá a tu Railway project → el servicio PostgreSQL tiene un nombre (ej: `Postgres`, `PostgreSQL`, `animal-gym-db`, etc.). Usá ese nombre en la referencia.

---

## Recomendación

Implementar el fix de código (opción 1). Es más robusto: funciona linkeando la DB sin necesitar referencias manuales.
