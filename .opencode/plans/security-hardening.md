# Plan: Hardening de seguridad y bugs críticos

## Issue 1 [CRITICAL - NPE en CloudinaryService]

**Archivo:** `src/main/java/com/animalgym/api/service/CloudinaryService.java`

**Problema:** `uploadResult.get("secure_url").toString()` lanza NPE si Cloudinary no devuelve la key.

**Fix:** Agregar null-guard:
```java
Object secureUrl = uploadResult.get("secure_url");
if (secureUrl == null) {
    throw new RuntimeException("Cloudinary upload failed: no secure_url in response");
}
return secureUrl.toString();
```

---

## Issue 2 [HIGH - extractRole() sin manejo de error]

**Archivo:** `src/main/java/com/animalgym/api/config/JwtAuthFilter.java`

**Problema:** Si el JWT no tiene claim `role`, `extractRole()` lanza `MissingClaimException`. Además si devuelve null se genera `ROLE_null`.

**Fix:** Envolver en try-catch + null-check:
```java
if (token != null && jwtService.isTokenValid(token)) {
    String email = jwtService.extractEmail(token);
    String role;
    try {
        role = jwtService.extractRole(token);
    } catch (JwtException e) {
        filterChain.doFilter(request, response);
        return;
    }
    if (role == null) {
        filterChain.doFilter(request, response);
        return;
    }
    // resto igual...
}
```

Agregar import de `io.jsonwebtoken.JwtException` si no está.

---

## Issue 3 [MEDIUM - SecurityConfig sin verificación de rol]

**Archivo:** `src/main/java/com/animalgym/api/config/SecurityConfig.java` línea 50

**Problema:** Solo valida `.authenticated()`, no verifica el rol. Cualquier JWT válido (incluso sin rol ADMIN) accede al admin.

**Fix:** Cambiar:
```java
.requestMatchers("/api/admin/**").authenticated()
```
a:
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

---

## Issue 4 [MEDIUM - app.storage.type inválido deja sin bean]

**Archivo:** `src/main/java/com/animalgym/api/service/CloudinaryService.java` y `LocalImageStorageServiceImpl.java`

**Problema:** Si alguien setea `app.storage.type=s3` (un typo), no se crea ningún `ImageStorageService` y Spring no arranca.

**Fix:** Agregar una implementación default con `@ConditionalOnMissingBean`:
```java
@Service
@ConditionalOnMissingBean(ImageStorageService.class)
public class DefaultImageStorageService implements ImageStorageService {
    @Override
    public String uploadImage(MultipartFile file) {
        throw new IllegalStateException("No storage implementation configured. Set app.storage.type=local or cloudinary");
    }
    @Override
    public void deleteImage(String imageUrl) { }
}
```

---

## Issue 5 [LOW - Unused import en AdminProductController]

**Archivo:** `src/main/java/com/animalgym/api/controller/AdminProductController.java` línea 13

**Fix:** Eliminar `import java.io.IOException;`

---

## Issue 6 [LOW - DatabaseConnectionChecker usa query PostgreSQL]

**Archivo:** `src/main/java/com/animalgym/api/config/DatabaseConnectionChecker.java` línea 20

**Problema:** `current_database()` es PostgreSQL-only. Falla en H2 (tests).

**Fix:** Cambiar a `SELECT 1` (database-agnostic).

---

## Resumen de archivos a modificar

| Archivo | Cambios |
|---------|---------|
| `CloudinaryService.java` | Null-guard en `secure_url` |
| `JwtAuthFilter.java` | Try-catch + null-check en `extractRole` |
| `SecurityConfig.java` | `.authenticated()` → `.hasRole("ADMIN")` |
| **(nuevo)** `DefaultImageStorageService.java` | Fallback si no hay storage configurado |
| `AdminProductController.java` | Eliminar import basura |
| `DatabaseConnectionChecker.java` | `current_database()` → `SELECT 1` |

Ningún cambio toca lógica de negocio ni endpoints.
