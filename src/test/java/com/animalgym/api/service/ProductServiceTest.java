package com.animalgym.api.service;

import com.animalgym.api.dto.request.ProductRequest;
import com.animalgym.api.dto.request.ProductUpdateRequest;
import com.animalgym.api.dto.response.ProductResponse;
import com.animalgym.api.entity.Product;
import com.animalgym.api.exception.ResourceNotFoundException;
import com.animalgym.api.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, cloudinaryService);
    }

    @Test
    void shouldReturnOnlyActiveProducts() {
        Product activeProduct = Product.builder()
                .id(1L).name("Whey Protein").price(new BigDecimal("29.99"))
                .stock(10).description("Desc").imageUrl("url1").active(true).build();
        Product inactiveProduct = Product.builder()
                .id(2L).name("Creatine").price(new BigDecimal("19.99"))
                .stock(5).description("Desc").imageUrl("url2").active(false).build();

        when(productRepository.findByActiveTrue()).thenReturn(List.of(activeProduct));

        List<ProductResponse> result = productService.getActiveProducts();

        assertEquals(1, result.size());
        assertEquals("Whey Protein", result.get(0).getName());
    }

    @Test
    void shouldCreateProductWithImage() {
        ProductRequest request = new ProductRequest();
        request.setName("Protein Bar");
        request.setPrice(new BigDecimal("4.99"));
        request.setStock(50);
        request.setDescription("Delicious protein bar");

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadImage(image)).thenReturn("https://cloudinary.com/bar.jpg");

        Product savedProduct = Product.builder()
                .id(1L).name("Protein Bar").price(new BigDecimal("4.99"))
                .stock(50).description("Delicious protein bar")
                .imageUrl("https://cloudinary.com/bar.jpg").active(true).build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request, image);

        assertEquals("Protein Bar", response.getName());
        assertEquals("https://cloudinary.com/bar.jpg", response.getImageUrl());
        verify(cloudinaryService).uploadImage(image);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(99L));
    }

    @Test
    void shouldSoftDeleteProduct() {
        Product product = Product.builder()
                .id(1L).name("Test").price(new BigDecimal("10"))
                .stock(5).description("Test").imageUrl("url").active(true).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        productService.deleteProduct(1L);

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }

    @Test
    void shouldUpdateProduct() {
        Product existing = Product.builder()
                .id(1L).name("Old Name").price(new BigDecimal("10"))
                .stock(5).description("Old desc").imageUrl("old-url").active(true).build();

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("New Name");
        request.setPrice(new BigDecimal("15"));
        request.setStock(10);
        request.setDescription("New desc");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request, null);

        assertEquals("New Name", response.getName());
        assertEquals("old-url", response.getImageUrl());
    }

    @Test
    void shouldReturnAllProductsForAdmin() {
        Product p1 = Product.builder().id(1L).name("P1").price(new BigDecimal("10"))
                .stock(1).imageUrl("u1").active(true).build();
        Product p2 = Product.builder().id(2L).name("P2").price(new BigDecimal("20"))
                .stock(2).imageUrl("u2").active(false).build();

        when(productRepository.findAllByOrderByIdDesc()).thenReturn(List.of(p2, p1));

        List<ProductResponse> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("P2", result.get(0).getName());
        assertEquals("P1", result.get(1).getName());
    }
}
