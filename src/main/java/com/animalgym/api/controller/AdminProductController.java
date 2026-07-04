package com.animalgym.api.controller;

import com.animalgym.api.dto.request.ProductRequest;
import com.animalgym.api.dto.request.ProductUpdateRequest;
import com.animalgym.api.dto.response.ProductResponse;
import com.animalgym.api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("data") @Valid ProductRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        ProductResponse response = productService.createProduct(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProductMultipart(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) @Valid ProductUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        if (request == null) {
            request = new ProductUpdateRequest();
        }
        ProductResponse response = productService.updateProduct(id, request, file);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> updateProductJson(
            @PathVariable Long id,
            @RequestBody @Valid ProductUpdateRequest request
    ) {
        ProductResponse response = productService.updateProduct(id, request, null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
