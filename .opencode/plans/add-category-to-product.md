# Plan: Agregar categoría a Product

## Archivos a modificar (6) + tests (1)

### 1. `src/main/java/com/animalgym/api/entity/Product.java`

Agregar después de `active`:
```java
@Column(nullable = false, length = 100)
@Builder.Default
private String category = "General";
```

### 2. `src/main/java/com/animalgym/api/dto/request/ProductRequest.java`

Agregar:
```java
@NotBlank(message = "Category is required")
private String category;
```

### 3. `src/main/java/com/animalgym/api/dto/request/ProductUpdateRequest.java`

Agregar (opcional, null = no cambiar):
```java
private String category;
```

### 4. `src/main/java/com/animalgym/api/dto/response/ProductResponse.java`

Agregar:
```java
private String category;
```

### 5. `src/main/java/com/animalgym/api/service/ProductService.java`

**`createProduct()`** — agregar en el builder:
```java
.category(request.getCategory() != null ? request.getCategory() : "General")
```

**`updateProduct()`** — agregar después del if de imageUrl:
```java
if (request.getCategory() != null) {
    product.setCategory(request.getCategory());
}
```

**`toResponse()`** — agregar:
```java
.category(product.getCategory())
```

### 6. `src/test/java/com/animalgym/api/service/ProductServiceTest.java`

Agregar `.category("General")` en todos los `Product.builder()` y `ProductResponse.builder()` existentes para que compilen.

---

## Lo que NO se toca

- **Controllers** — no cambian, reciben los DTOs actualizados
- **Repository** — no necesita cambios (JPA maneja el nuevo campo automáticamente)
- **Security** — no cambia
- **application.properties** — no cambia (JPA `ddl-auto=update` crea la columna sola)

---

## Verificación

```bash
./mvnw test
```

La columna `category` se crea automáticamente con `ddl-auto=update`. Los productos existentes sin categoría quedan con `NULL`, el default `"General"` aplica solo a nuevos productos vía Java.
