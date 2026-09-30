package com.milktea.catalog.service.impl;
import com.milktea.catalog.dto.*; import com.milktea.catalog.entity.*; import com.milktea.catalog.mapper.ProductMapper; import com.milktea.catalog.repository.*; import com.milktea.catalog.service.ProductService; import com.milktea.common.exception.BusinessException; import com.milktea.common.response.PageResponse; import java.util.ArrayList; import java.util.List; import org.springframework.data.domain.*; import org.springframework.data.jpa.domain.Specification; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class ProductServiceImpl implements ProductService {
    private final SanPhamRepository products; private final LoaiSanPhamRepository categories; private final ProductMapper mapper;
    public ProductServiceImpl(SanPhamRepository products,LoaiSanPhamRepository categories,ProductMapper mapper){this.products=products;this.categories=categories;this.mapper=mapper;}
    public PageResponse<ProductResponse> list(Long categoryId,String keyword,Pageable pageable){Specification<SanPham> spec=(root,q,cb)->{List<jakarta.persistence.criteria.Predicate> ps=new ArrayList<>();ps.add(cb.isNull(root.get("deletedAt")));if(categoryId!=null)ps.add(cb.equal(root.get("danhMuc").get("id"),categoryId));if(keyword!=null&&!keyword.isBlank())ps.add(cb.like(cb.lower(root.get("tenSanPham")),"%"+keyword.trim().toLowerCase()+"%"));return cb.and(ps.toArray(jakarta.persistence.criteria.Predicate[]::new));};Page<ProductResponse> page=products.findAll(spec,pageable).map(mapper::toResponse);return PageResponse.from(page);}
    public ProductResponse get(Long id){return mapper.toResponse(product(id));}
    @Transactional public ProductResponse create(ProductRequest r){SanPham p=new SanPham();p.setDanhMuc(category(r.maDanhMuc()));p.setTenSanPham(r.tenSanPham().trim());p.setMoTa(r.moTa());return mapper.toResponse(products.save(p));}
    @Transactional public ProductResponse update(Long id,ProductRequest r){SanPham p=product(id);p.setDanhMuc(category(r.maDanhMuc()));p.setTenSanPham(r.tenSanPham().trim());p.setMoTa(r.moTa());return mapper.toResponse(p);}
    @Transactional public void delete(Long id){product(id).markDeleted();}
    private SanPham product(Long id){return products.findById(id).filter(p->p.getDeletedAt()==null).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Không tìm thấy sản phẩm"));}
    private LoaiSanPham category(Long id){return categories.findById(id).filter(c->c.getDeletedAt()==null).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Không tìm thấy danh mục"));}
}
