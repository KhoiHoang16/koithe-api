package com.milktea.customer.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.customer.dto.*;
import com.milktea.customer.entity.KhachHang;
import com.milktea.customer.mapper.KhachHangMapper;
import com.milktea.customer.repository.KhachHangRepository;
import com.milktea.customer.service.KhachHangService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository repo;
    private final KhachHangMapper mapper;
    private final PasswordEncoder encoder;

    public KhachHangServiceImpl(KhachHangRepository repo, KhachHangMapper mapper, PasswordEncoder encoder) {
        this.repo = repo;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    @Override
    public List<KhachHangResponse> findAll(String phone) {
        if (phone != null && !phone.isBlank()) {
            return repo.findBySoDienThoaiAndDeletedAtIsNull(phone.trim())
                    .map(mapper::toResponse)
                    .map(List::of)
                    .orElse(List.of());
        }
        return repo.findAllByDeletedAtIsNull().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public KhachHangResponse findById(Long id) {
        return mapper.toResponse(requireCustomer(id));
    }

    @Override
    public KhachHangResponse findByPhone(String phone) {
        KhachHang customer = repo.findBySoDienThoaiAndDeletedAtIsNull(phone)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng với số điện thoại: " + phone));
        return mapper.toResponse(customer);
    }

    @Override
    @Transactional
    public KhachHangResponse create(KhachHangCreateRequest request) {
        if (repo.existsBySoDienThoaiAndDeletedAtIsNull(request.soDienThoai())) {
            throw error(HttpStatus.CONFLICT, "Số điện thoại đã được đăng ký");
        }

        KhachHang customer = mapper.toEntity(request);
        if (request.matKhau() != null && !request.matKhau().isBlank()) {
            customer.setMatKhauMaHoa(encoder.encode(request.matKhau()));
        }
        return mapper.toResponse(repo.save(customer));
    }

    @Override
    @Transactional
    public KhachHangResponse update(Long id, KhachHangUpdateRequest request) {
        KhachHang customer = requireCustomer(id);
        mapper.updateEntity(request, customer);
        return mapper.toResponse(repo.save(customer));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        KhachHang customer = requireCustomer(id);
        customer.markDeleted();
    }

    private KhachHang requireCustomer(Long id) {
        return repo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
    }

    private BusinessException error(HttpStatus status, String message) {
        return new BusinessException(status, message);
    }
}
