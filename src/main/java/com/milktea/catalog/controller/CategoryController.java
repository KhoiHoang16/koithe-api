package com.milktea.catalog.controller;
import com.milktea.catalog.dto.*;import com.milktea.catalog.service.CategoryService;import com.milktea.common.response.ApiResponse;import jakarta.validation.Valid;import java.util.List;import org.springframework.http.HttpStatus;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;public CategoryController(CategoryService service){this.service=service;}
    @GetMapping public ApiResponse<List<CategoryResponse>> list(){return ApiResponse.success(service.list());}
    @GetMapping("/{id}") public ApiResponse<CategoryResponse> get(@PathVariable Long id){return ApiResponse.success(service.get(id));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request){return ApiResponse.success(service.create(request));}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ApiResponse<CategoryResponse> update(@PathVariable Long id,@Valid @RequestBody CategoryRequest request){return ApiResponse.success(service.update(id,request));}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable Long id){service.delete(id);}
}
