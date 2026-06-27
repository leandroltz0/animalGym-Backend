package com.animalgym.api.service;

import com.animalgym.api.dto.request.ProductRequest;
import com.animalgym.api.dto.response.ProductResponse;
import com.animalgym.api.entity.Product;
import com.animalgym.api.exception.ResourceNotFoundException;
import com.animalgym.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = findProduct(id);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile image) {
        String imageUrl = (image != null && !image.isEmpty())
                ? cloudinaryService.uploadImage(image)
                : (request.getImageUrl() != null ? request.getImageUrl() : "");

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .description(request.getDescription())
                .imageUrl(imageUrl)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, MultipartFile image) {
        Product product = findProduct(id);

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setDescription(request.getDescription());

        if (image != null && !image.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(image);
            product.setImageUrl(newImageUrl);
        } else if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
