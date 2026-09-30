package com.milktea.catalog.service.impl;
import com.milktea.catalog.dto.*; import com.milktea.catalog.entity.LoaiSanPham; import com.milktea.catalog.mapper.CategoryMapper; import com.milktea.catalog.repository.*; import com.milktea.catalog.service.CategoryService; import com.milktea.common.exception.BusinessException; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final LoaiSanPhamRepository categories; private final SanPhamRepository products; private final CategoryMapper mapper;
    public CategoryServiceImpl(LoaiSanPhamRepository categories, SanPhamRepository products, CategoryMapper mapper){this.categories=categories;this.products=products;this.mapper=mapper;}
    public List<CategoryResponse> list(){return categories.findAllByDeletedAtIsNullOrderByTenDanhMucAsc().stream().map(mapper::toResponse).toList();}
    public CategoryResponse get(Long id){return mapper.toResponse(category(id));}
    @Transactional public CategoryResponse create(CategoryRequest request){LoaiSanPham c=new LoaiSanPham();c.setTenDanhMuc(request.tenDanhMuc().trim());return mapper.toResponse(categories.save(c));}
    @Transactional public CategoryResponse update(Long id,CategoryRequest request){LoaiSanPham c=category(id);mapper.update(request,c);c.setTenDanhMuc(request.tenDanhMuc().trim());return mapper.toResponse(c);}
    @Transactional public void delete(Long id){LoaiSanPham c=category(id);if(products.existsByDanhMucIdAndDeletedAtIsNull(id))throw new BusinessException(HttpStatus.CONFLICT,"Không thể xóa danh mục đang có sản phẩm");c.markDeleted();}
    private LoaiSanPham category(Long id){return categories.findById(id).filter(c->c.getDeletedAt()==null).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Không tìm thấy danh mục"));}
}
