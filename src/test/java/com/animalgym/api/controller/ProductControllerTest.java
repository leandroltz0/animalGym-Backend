package com.animalgym.api.controller;

import com.animalgym.api.dto.response.ProductResponse;
import com.animalgym.api.service.JwtService;
import com.animalgym.api.service.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({com.animalgym.api.config.SecurityConfig.class, com.animalgym.api.config.JwtAuthFilter.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldReturnActiveProducts() throws Exception {
        List<ProductResponse> products = List.of(
                ProductResponse.builder()
                        .id(1L).name("Whey").price(new BigDecimal("29.99"))
                        .stock(10).description("Protein").imageUrl("url").build()
        );

        when(productService.getActiveProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Whey"));

        verify(productService).getActiveProducts();
    }

    @Test
    void shouldReturnEmptyListWhenNoProducts() throws Exception {
        when(productService.getActiveProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
