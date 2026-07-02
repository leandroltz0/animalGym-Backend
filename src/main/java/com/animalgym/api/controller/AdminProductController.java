package com.animalgym.api.controller;

import com.animalgym.api.dto.request.ProductRequest;
import com.animalgym.api.dto.request.ProductUpdateRequest;
import com.animalgym.api.dto.response.ProductResponse;
import com.animalgym.api.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

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
            @RequestPart("data") String dataJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        ProductRequest request = objectMapper.readValue(dataJson, ProductRequest.class);
        ProductResponse response = productService.createProduct(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProductMultipart(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        ProductUpdateRequest request = dataJson != null ? objectMapper.readValue(dataJson, ProductUpdateRequest.class) : new ProductUpdateRequest();
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
