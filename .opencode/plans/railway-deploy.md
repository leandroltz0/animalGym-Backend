# Plan: Preparar backend para deploy en Railway

## Archivos a crear (2) y modificar (2)

### 1. (CREAR) `Procfile` — raíz del proyecto

Railway necesita saber cómo arrancar la app:

```
web: java -jar target/animal-gym-api-0.0.1-SNAPSHOT.jar
```

### 2. (CREAR) `src/main/java/com/animalgym/api/config/RailwayDataSourceConfig.java`

Railway expone la DB como `DATABASE_URL` en formato `postgresql://user:pass@host:5432/dbname`, pero Spring Boot necesita `jdbc:postgresql://host:5432/dbname`. Este config lo parsea automáticamente:

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

        // Fallback: usa las propiedades individuales (desarrollo local)
        return DataSourceBuilder.create()
                .url(env.getProperty("spring.datasource.url",
                        "jdbc:postgresql://localhost:5432/animalgym-db"))
                .username(env.getProperty("spring.datasource.username", "postgres"))
                .password(env.getProperty("spring.datasource.password", "postgres"))
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
```

### 3. `application.properties` — cambios

**Deshabilitar SQL log en prod** (seguridad, no filtrar queries en logs):

```properties
spring.jpa.show-sql=false
```

**Cambiar default de `app.frontend.url.prod`** a un placeholder genérico:

```properties
app.frontend.url.prod=${FRONTEND_URL_PROD:https://your-frontend.vercel.app}
```

**Agregar default de storage type** a `cloudinary` (Railway no tiene disco persistente):

```properties
app.storage.type=${STORAGE_TYPE:cloudinary}
```

### 4. `.env.example` — actualizar

- Sincronizar `JWT_EXPIRATION_MS` a `2592000000` (30 días)
- Agregar `STORAGE_TYPE=cloudinary`
- Agregar comentario de `DATABASE_URL`

---

## Config manual en Railway (dashboard)

Después del deploy, setear estas variables en Railway:

| Variable | Valor |
|----------|-------|
| `JWT_SECRET` | (tu clave secreta, larga) |
| `CLOUDINARY_CLOUD_NAME` | (tu cloud name) |
| `CLOUDINARY_API_KEY` | (tu api key) |
| `CLOUDINARY_API_SECRET` | (tu api secret) |
| `FRONTEND_URL_PROD` | `https://tudominio.vercel.app` |
| `STORAGE_TYPE` | `cloudinary` |

La DB de Railway se auto-configura vía `DATABASE_URL` (el config lo parsea).

---

## Build en Railway

Railway detecta Maven automáticamente. Comandos sugeridos:

- **Build:** `./mvnw clean package -DskipTests`
- **Start:** `java -jar target/animal-gym-api-0.0.1-SNAPSHOT.jar`

O simplemente crear el `Procfile` y Railway lo usa automáticamente.

---

## Verificación local

```bash
./mvnw clean package -DskipTests   # build exitoso
java -jar target/animal-gym-api-0.0.1-SNAPSHOT.jar  # arranca bien
```
