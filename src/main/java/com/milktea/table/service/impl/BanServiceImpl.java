package com.milktea.table.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.table.dto.*;
import com.milktea.table.entity.Ban;
import com.milktea.table.mapper.BanMapper;
import com.milktea.table.repository.BanRepository;
import com.milktea.table.service.BanService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BanServiceImpl implements BanService {

    private static final String TRONG = "TRONG";
    private static final Set<String> VALID_STATUSES = Set.of("TRONG", "DANG_CO_KHACH", "DA_DAT_TRUOC");

    private final BanRepository repository;
    private final BanMapper mapper;

    public BanServiceImpl(BanRepository repository, BanMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<BanResponse> findAll() {
        return repository.findAllByDeletedAtIsNullOrderBySoBanAsc()
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public BanResponse findById(Long id) {
        return mapper.toResponse(table(id));
    }

    @Override
    public BanResponse findByQrToken(String token) {
        if (token == null || token.isBlank()) {
            throw error(HttpStatus.BAD_REQUEST, "Mã QR token không được để trống");
        }
        return repository.findByMaQrTokenAndDeletedAtIsNull(token.trim())
                .map(mapper::toResponse)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy bàn tương ứng với mã QR"));
    }

    @Override
    @Transactional
    public BanResponse create(BanRequest r) {
        if (r.soBan() == null || r.soBan().isBlank()) {
            throw error(HttpStatus.BAD_REQUEST, "Số bàn không được để trống");
        }

        String soBan = r.soBan().trim();

        if (repository.existsBySoBanAndDeletedAtIsNull(soBan)) {
            throw error(HttpStatus.CONFLICT, "Số bàn đã tồn tại");
        }

        Ban ban = new Ban();
        ban.setSoBan(soBan);
        ban.setMaQrToken(UUID.randomUUID().toString());
        ban.setTrangThai(TRONG);

        return mapper.toResponse(repository.save(ban));
    }

    @Override
    @Transactional
    public BanResponse update(Long id, BanRequest r) {
        Ban ban = table(id);

        if (r.soBan() != null && !r.soBan().isBlank()) {
            String soBan = r.soBan().trim();
            if (repository.existsBySoBanAndIdNotAndDeletedAtIsNull(soBan, id)) {
                throw error(HttpStatus.CONFLICT, "Số bàn đã tồn tại");
            }
            ban.setSoBan(soBan);
        }

        if (r.trangThai() != null) {
            validateStatusTransition(ban.getTrangThai(), r.trangThai());
            ban.setTrangThai(r.trangThai());
        }

        if (r.maQrToken() != null) {
            ban.setMaQrToken(UUID.randomUUID().toString());
        }

        return mapper.toResponse(ban);
    }

    @Override
    @Transactional
    public BanResponse updateStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw error(HttpStatus.BAD_REQUEST, "Trạng thái không được để trống");
        }
        Ban ban = table(id);
        validateStatusTransition(ban.getTrangThai(), status.trim());
        ban.setTrangThai(status.trim());
        return mapper.toResponse(ban);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Ban ban = table(id);

        if (!TRONG.equals(ban.getTrangThai())) {
            throw error(HttpStatus.CONFLICT, "Không thể xóa bàn đang được sử dụng");
        }

        ban.markDeleted();
    }

    private Ban table(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy bàn"));
    }

    private void validateStatusTransition(String from, String to) {
        if (!VALID_STATUSES.contains(to)) {
            throw error(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + to);
        }
        if (from.equals(to)) {
            return;
        }
        if ("DANG_CO_KHACH".equals(from) && !"TRONG".equals(to)) {
            throw error(HttpStatus.BAD_REQUEST, "Bàn đang có khách chỉ có thể chuyển về trống");
        }
        if ("DA_DAT_TRUOC".equals(from) && !("DANG_CO_KHACH".equals(to) || "TRONG".equals(to))) {
            throw error(HttpStatus.BAD_REQUEST, "Bàn đã đặt trước chỉ có thể chuyển sang có khách hoặc trống");
        }
    }

    private BusinessException error(HttpStatus status, String message) {
        return new BusinessException(status, message);
    }
}
