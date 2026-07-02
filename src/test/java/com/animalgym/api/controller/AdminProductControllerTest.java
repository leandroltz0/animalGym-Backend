package com.animalgym.api.controller;

import com.animalgym.api.dto.request.ProductRequest;
import com.animalgym.api.dto.response.ProductResponse;
import com.animalgym.api.service.JwtService;
import com.animalgym.api.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProductController.class)
@Import({com.animalgym.api.config.SecurityConfig.class, com.animalgym.api.config.JwtAuthFilter.class})
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer eyJhbGciOiJIUzI1NiJ9.valid-token";

        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractEmail(anyString())).thenReturn("admin@animalgym.com");
        when(jwtService.extractRole(anyString())).thenReturn("ADMIN");
    }

    @Test
    void shouldReturnAllProductsForAdmin() throws Exception {
        List<ProductResponse> products = List.of(
                ProductResponse.builder()
                        .id(1L).name("Whey").price(new BigDecimal("29.99"))
                        .stock(10).description("Protein").imageUrl("url1").build(),
                ProductResponse.builder()
                        .id(2L).name("Creatine").price(new BigDecimal("19.99"))
                        .stock(0).description("Supplement").imageUrl("url2").build()
        );

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenInvalidToken() throws Exception {
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setPrice(new BigDecimal("15.99"));
        request.setStock(10);

        ProductResponse response = ProductResponse.builder()
                .id(1L).name("New Product").price(new BigDecimal("15.99"))
                .stock(10).imageUrl("https://cloudinary.com/img.jpg").build();

        when(productService.createProduct(any(ProductRequest.class), any()))
                .thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "image-content".getBytes()
        );
        MockMultipartFile data = new MockMultipartFile(
                "data", "data.json", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/admin/products")
                        .file(file)
                        .file(data)
                        .header("Authorization", validToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/admin/products/1")
                        .header("Authorization", validToken))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void shouldReturnProductById() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L).name("Product").price(new BigDecimal("10"))
                .stock(5).imageUrl("url").build();

        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/products/1")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
