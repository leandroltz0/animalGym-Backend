# Plan: Filtro active en productos + soft delete

## Archivos a modificar

### 1. `src/main/java/com/animalgym/api/service/ProductService.java`

**a) `deleteProduct()` — hard delete → soft delete**

```java
// ANTES (hard delete):
productRepository.delete(product);

// DESPUÉS (soft delete):
product.setActive(false);
productRepository.save(product);
```

**b) `updateProduct()` — agregar manejo de active**

Agregar después del último `if` existente (el de `request.getImageUrl()`):

```java
if (request.getActive() != null) {
    product.setActive(request.getActive());
}
```

### 2. `src/main/java/com/animalgym/api/dto/request/ProductUpdateRequest.java`

Agregar campo (no lleva `@NotNull` porque es opcional en update):

```java
private Boolean active;
```

### 3. `src/test/java/com/animalgym/api/service/ProductServiceTest.java`

Agregar test para update con active (`shouldUpdateProductActiveField`):

```java
@Test
void shouldUpdateProductActiveField() {
    Product existing = Product.builder()
            .id(1L).name("Test").price(new BigDecimal("10"))
            .stock(5).description("Desc").imageUrl("url").active(false).build();

    ProductUpdateRequest request = new ProductUpdateRequest();
    request.setActive(true);

    when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

    ProductResponse response = productService.updateProduct(1L, request, null);

    assertTrue(response.getActive());
}
```

## Lo que NO se toca

| Archivo | Razón |
|---------|-------|
| `ProductController.java` | Ya llama a `getActiveProducts()` |
| `AdminProductController.java` | Ya llama a `getAllProducts()` |
| `ProductRepository.java` | Ya tiene `findByActiveTrue()` y `findAllByOrderByIdDesc()` |
| `Product.java` | Ya tiene `active` con `@Builder.Default = true` |
| `ProductRequest.java` | Ya se crea con `active = true` en el service |
| `SecurityConfig.java` | Ya protege `/api/admin/**` |

## Tests existentes que validan estos cambios

- `ProductServiceTest.shouldSoftDeleteProduct()` — ya espera soft delete (verifica `product.getActive() == false`)
- `ProductControllerTest.shouldReturnActiveProducts()` — ya verifica solo activos
- `AdminProductControllerTest.shouldReturnAllProductsForAdmin()` — ya verifica todos

## Verificación

```bash
./mvnw test
```
