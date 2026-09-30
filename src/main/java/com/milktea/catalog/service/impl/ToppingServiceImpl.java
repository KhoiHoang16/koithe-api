package com.milktea.catalog.service.impl;
import com.milktea.catalog.dto.*;import com.milktea.catalog.entity.Topping;import com.milktea.catalog.mapper.ToppingMapper;import com.milktea.catalog.repository.ToppingRepository;import com.milktea.catalog.service.ToppingService;import com.milktea.common.exception.BusinessException;import java.math.BigDecimal;import java.util.List;import org.springframework.http.HttpStatus;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class ToppingServiceImpl implements ToppingService {
    private final ToppingRepository toppings;private final ToppingMapper mapper;
    public ToppingServiceImpl(ToppingRepository toppings,ToppingMapper mapper){this.toppings=toppings;this.mapper=mapper;}
    public List<ToppingResponse> list(){return toppings.findAllByDeletedAtIsNullOrderByTenToppingAsc().stream().map(mapper::toResponse).toList();}
    @Transactional public ToppingResponse create(ToppingRequest r){Topping t=new Topping();t.setTenTopping(r.tenTopping().trim());t.setGiaBan(r.giaBan());if(r.soLuongTon()!=null){t.setSoLuongTon(r.soLuongTon());t.setConHang(r.soLuongTon().compareTo(BigDecimal.ZERO)>0);}return mapper.toResponse(toppings.save(t));}
    @Transactional public ToppingResponse update(Long id,ToppingRequest r){Topping t=topping(id);t.setTenTopping(r.tenTopping().trim());t.setGiaBan(r.giaBan());if(r.soLuongTon()!=null){t.setSoLuongTon(r.soLuongTon());t.setConHang(r.soLuongTon().compareTo(BigDecimal.ZERO)>0);}return mapper.toResponse(t);}
    @Transactional public void delete(Long id){topping(id).markDeleted();}
    private Topping topping(Long id){return toppings.findById(id).filter(t->t.getDeletedAt()==null).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Không tìm thấy topping"));}
}
