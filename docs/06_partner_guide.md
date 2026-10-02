# Hướng dẫn làm việc dành cho partner

Tài liệu này bổ sung cho Swagger và [hướng dẫn chạy local](huong-dan-chay-local-va-deploy.md). Hãy kiểm tra DTO/controller hiện tại trước khi gửi request hoặc viết test, vì contract cụ thể có thể khác ví dụ minh họa.

## 1. Trace request từ Swagger xuống database

1. Chạy backend và mở Swagger UI (`/swagger-ui.html`). Chọn endpoint, xem HTTP method, path, schema request/response và thử bằng token phù hợp.
2. Đặt breakpoint tại method trong `controller`. Chạy `MilkTeaApplication` ở chế độ **Debug** trong IntelliJ IDEA/Eclipse rồi gửi lại request từ Swagger. Kiểm tra path variable, query parameter, body đã bind/validate ra sao và principal/role hiện tại.
3. Step Into lời gọi service interface để vào implementation trong `service/impl`. Theo dõi các validation, mapper và transaction. Service scaffold còn `TODO`/`UnsupportedOperationException`; endpoint tương ứng chưa có nghiệp vụ cho tới khi partner triển khai.
4. Step Into repository để xem query JPA/SQL, sau đó kiểm tra entity và quan hệ tại `entity`. Với thao tác ghi, xác nhận transaction và các FK/unique constraint.
5. Đối chiếu schema bằng migration trong `src/main/resources/db/migration/`; không suy luận schema chỉ từ DTO. Kiểm tra dữ liệu trực tiếp trong PostgreSQL/DBeaver nếu cần.

Luồng tổng quát:

```text
Swagger / HTTP -> Controller -> Service interface -> ServiceImpl -> Repository -> Entity / PostgreSQL
                                      |                    |
                                      +---- Mapper / DTO --+
```

Đặt breakpoint ở cả controller, service và repository giúp phân biệt lỗi binding/validation với lỗi nghiệp vụ hoặc persistence. Với request có `@PreAuthorize`, kiểm tra token và role; response 401/403 thường xảy ra trước khi vào controller.

## 2. Đọc ERD bằng DBeaver

1. Khởi động PostgreSQL bằng Docker Compose và xác nhận container healthy: `docker compose ps`.
2. Trong DBeaver tạo kết nối PostgreSQL: host `localhost`, port `5433`, database `milktea`, user `milktea`; lấy password từ `.env` (`POSTGRES_PASSWORD`). Các cổng này là cổng host mặc định, có thể đã đổi trong `.env`.
3. Mở schema `public`, chọn các bảng cần xem rồi dùng **ER Diagram** (hoặc ER Diagram của schema) để hiển thị PK/FK và quan hệ. Nếu sơ đồ thiếu bảng, refresh metadata và kiểm tra Flyway đã chạy.
4. Nếu timestamp hiển thị lệch giờ, trước hết xem timezone của session:

   ```sql
   SHOW TIME ZONE;
   SET TIME ZONE 'UTC';
   SELECT now();
   ```

   Có thể đặt timezone hiển thị của Data Editor/driver về UTC trong cấu hình DBeaver. Đây là cấu hình phiên xem dữ liệu; không thay đổi timezone máy chủ hay sửa dữ liệu để “bù giờ”. Cân nhắc lại timezone sau khi reconnect vì `SET TIME ZONE` chỉ áp dụng session hiện tại.

## 3. Unit test cho ServiceImpl

Chọn test theo mục tiêu:

- **Unit test nhanh**: Mockito mock repository/gateway/facade, chỉ khởi tạo service cần test. Kiểm tra kết quả, validation, lời gọi collaborator và exception. Dùng `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`; không cần khởi động Spring hoặc database.
- **Integration test**: dùng `@SpringBootTest` và `@Transactional` khi cần xác nhận wiring Spring, mapper, JPA mapping/constraint hoặc transaction. Transaction thường rollback khi test kết thúc. Dùng database test cô lập (ví dụ Testcontainers nếu môi trường CI hỗ trợ), không chạy test ghi lên database dùng chung.

Khung unit test minh họa (đổi tên service, method và collaborator theo module đang làm):

```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceImplTest {
    @Mock
    private ExampleRepository repository;

    @InjectMocks
    private ExampleServiceImpl service;

    @Test
    void create_shouldRejectInvalidRequest() {
        var request = new ExampleRequest(/* dữ liệu không hợp lệ */);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verifyNoInteractions(repository);
    }
}
```

Đây là mẫu cấu trúc, không giả định mọi module dùng `IllegalArgumentException` hay constructor giống nhau. Hãy assert exception/kiểu lỗi đúng contract dự án, và bổ sung case thành công, not-found, dữ liệu biên, xung đột đồng thời. Với integration test, có thể dùng:

```java
@SpringBootTest
@Transactional
class ExampleServiceIntegrationTest {
    @Autowired
    private ExampleService service;

    // Arrange dữ liệu test -> gọi service -> assert kết quả/persistence.
}
```

## 4. Lỗi thường gặp

| Lỗi | Nguyên nhân thường gặp | Cách xử lý |
| --- | --- | --- |
| `LazyInitializationException` | Truy cập quan hệ `LAZY` sau khi persistence context/transaction đã đóng. | Đọc quan hệ trong transaction; dùng query `JOIN FETCH` hoặc `@EntityGraph` cho đúng use case. Tránh bật eager toàn cục chỉ để che lỗi. |
| `OptimisticLockException` / lỗi optimistic locking | Một bản ghi bị cập nhật đồng thời hoặc version đã cũ. | Reload trạng thái mới, quyết định retry có giới hạn nếu thao tác an toàn/idempotent; nếu không thì trả lỗi xung đột dễ hiểu để người dùng thử lại. Không retry mù thao tác cộng/trừ tồn kho. |
| Flyway checksum mismatch | Nội dung migration đã chạy bị sửa, nên checksum hiện tại khác lịch sử. | Không sửa migration đã áp dụng. Khôi phục nội dung migration cũ nếu bị thay đổi ngoài ý muốn; tạo migration mới cho thay đổi schema. Không `repair` để che sai lệch khi chưa hiểu tác động. |
| Port conflict `5432` / `5433` | PostgreSQL cục bộ hoặc container khác đã chiếm cổng host. Compose mặc định publish PostgreSQL ở `5433:5432`. | Xem `docker-compose.yml`, `.env` (`POSTGRES_PORT`) và `docker compose ps`; đổi cổng host nếu cần rồi cập nhật `DB_URL`/cấu hình DBeaver. `5432` là cổng bên trong container, còn app trên host dùng cổng publish (mặc định `5433`). |

## 5. Postman collection

1. Trong Postman chọn **Import**, mở `docs/milktea-api.postman_collection.json` và import collection.
2. Tạo/chọn Environment có `baseUrl = http://localhost:8081` và `accessToken` để trống lúc đầu. Nếu dùng các request giỏ hàng guest, có thể khai báo thêm `cartToken`.
3. Gửi Auth → Login. Collection test script lưu access token từ response nếu API trả `data.accessToken`; nếu response khác, copy access token vào biến `accessToken` thủ công.
4. Collection pre-request script tự gắn `Authorization: Bearer {{accessToken}}` cho request cần xác thực khi biến token có giá trị. Các endpoint public và login/register/refresh không cần bearer token. Request cart có thể kèm `X-Cart-Token: {{cartToken}}` theo contract guest-cart.
5. Chạy từng request hoặc cả folder; kiểm tra response/status với Swagger và DTO hiện hành. Thay ID ví dụ bằng ID có thật trong database local.

Không lưu JWT, mật khẩu thật hoặc secret môi trường vào collection đã commit. Collection chứa ví dụ request, không chứa credential cá nhân.
