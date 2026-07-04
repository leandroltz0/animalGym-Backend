# Plan: Agregar active toggle al PATCH + confirmar hard delete

## 1. `src/main/java/com/animalgym/api/dto/request/ProductUpdateRequest.java`

Agregar campo:
```java
private Boolean active;
```

## 2. `src/main/java/com/animalgym/api/service/ProductService.java`

En `updateProduct()`, agregar después del bloque de category (línea 88):
```java
if (request.getActive() != null) {
    product.setActive(request.getActive());
}
```

## 3. Confirmación: DELETE ya es hard delete

```java
// ProductService.java:94-97 — ya está implementado
@Transactional
public void deleteProduct(Long id) {
    Product product = findProduct(id);
    productRepository.delete(product);  // ← ya es hard delete
}
```

## Resumen de endpoints

| Endpoint | Comportamiento |
|----------|---------------|
| `GET /products` | Solo `active = true` (público) |
| `GET /admin/products` | Todos (active true/false) |
| `POST /admin/products` | Crea con category obligatorio, active=true default |
| `PATCH /admin/products/{id}` | Update parcial, incluye toggle `active` |
| `DELETE /admin/products/{id}` | **Hard delete** — borra físico, no vuelve |

## Verificación

```bash
./mvnw test
```
