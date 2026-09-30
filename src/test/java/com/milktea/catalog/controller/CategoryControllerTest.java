package com.milktea.catalog.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.milktea.catalog.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CategoryControllerTest {
    private CategoryService service;
    private MockMvc mvc;

    @BeforeEach void setUp() {
        service = mock(CategoryService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CategoryController(service)).build();
    }

    @Test void categoryListReturnsOkAndDoesNotExposeEntities() throws Exception {
        when(service.list()).thenReturn(List.of());
        mvc.perform(get("/api/categories")).andExpect(status().isOk());
    }
}
