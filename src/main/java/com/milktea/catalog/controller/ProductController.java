package com.milktea.catalog.controller;
import com.milktea.catalog.dto.*;import com.milktea.catalog.service.*;import com.milktea.common.response.*;import jakarta.validation.Valid;import java.util.List;import org.springframework.data.domain.*;import org.springframework.data.web.PageableDefault;import org.springframework.http.HttpStatus;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/products")
public class ProductController {
    private final ProductService products;private final VariantService variants;public ProductController(ProductService products,VariantService variants){this.products=products;this.variants=variants;}
    @GetMapping public ApiResponse<PageResponse<ProductResponse>> list(@RequestParam(required=false) Long categoryId,@RequestParam(required=false) String keyword,@PageableDefault(size=20,sort="id",direction=Sort.Direction.ASC) Pageable pageable){return ApiResponse.success(products.list(categoryId,keyword,pageable));}
    @GetMapping("/{id}") public ApiResponse<ProductResponse> get(@PathVariable Long id){return ApiResponse.success(products.get(id));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request){return ApiResponse.success(products.create(request));}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ApiResponse<ProductResponse> update(@PathVariable Long id,@Valid @RequestBody ProductRequest request){return ApiResponse.success(products.update(id,request));}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable Long id){products.delete(id);}
    @GetMapping("/{id}/variants") public ApiResponse<List<VariantResponse>> variants(@PathVariable Long id){return ApiResponse.success(variants.list(id));}
    @PostMapping("/{id}/variants") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ApiResponse<VariantResponse> createVariant(@PathVariable Long id,@Valid @RequestBody VariantRequest request){return ApiResponse.success(variants.create(id,request));}
}
