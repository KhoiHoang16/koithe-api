# Hướng dẫn chạy local và deploy MilkTea API

Tài liệu này dành cho partner cần khởi chạy hoặc triển khai backend `milktea-backend`.

## 1. Tổng quan

- Backend: Spring Boot 3.3.12 / Java 21.
- Database: PostgreSQL 16.
- Cache/lưu refresh token: Redis 7.
- Migration database: Flyway, tự chạy khi ứng dụng khởi động.
- Cổng bên trong ứng dụng: `8080`.
- API docs: `/swagger-ui.html`.

Các biến môi trường mẫu nằm trong [`.env.example`](../.env.example). Không commit file `.env` vì file này có mật khẩu và JWT secret.

## 2. Yêu cầu

Chọn một trong hai cách chạy:

| Cách chạy | Cần cài |
| --- | --- |
| Chạy mã nguồn trên máy (khuyến nghị khi phát triển) | JDK 21, Maven 3.9+, Docker Desktop/Compose |
| Chạy toàn bộ bằng Docker | Docker Desktop/Compose |

Kiểm tra nhanh:

```powershell
java -version
mvn -version
docker compose version
```

> Nếu dùng IntelliJ IDEA, đặt Project SDK là Java 21 và bật annotation processing (Lombok/MapStruct).

## 3. Chuẩn bị cấu hình

Tại thư mục gốc của dự án, tạo `.env` từ file mẫu:

```powershell
Copy-Item .env.example .env
```

Khi deploy, thay tối thiểu các giá trị sau trong `.env`:

```dotenv
POSTGRES_PASSWORD=<mat-khau-postgres-manh>
JWT_SECRET=<chuoi-ngau-nhien-it-nhat-32-ky-tu>
POSTGRES_PORT=5433
REDIS_PORT=6380
BACKEND_PORT=8081
```

`POSTGRES_PORT`, `REDIS_PORT`, và `BACKEND_PORT` là cổng **trên máy host**. Có thể đổi nếu bị trùng. Docker vẫn dùng lần lượt cổng `5432`, `6379`, và `8080` bên trong network của Compose.

## 4. Chạy local từ mã nguồn

### Bước 1: chỉ khởi động PostgreSQL và Redis

```powershell
docker compose up -d postgres redis
docker compose ps
```

Sau khi trạng thái hai service là `healthy`, backend chạy trên máy cần kết nối vào các cổng đã map ra host. Trong PowerShell, chạy:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DB_URL = 'jdbc:postgresql://localhost:5433/milktea'
$env:DB_USERNAME = 'milktea'
$env:DB_PASSWORD = '<gia-tri-POSTGRES_PASSWORD-trong-.env>'
$env:REDIS_HOST = 'localhost'
$env:REDIS_PORT = '6380'
$env:JWT_SECRET = '<jwt-secret-it-nhat-32-ky-tu>'
mvn spring-boot:run
```

Hoặc chạy qua IDE với các environment variables tương tự. Không dùng giá trị mặc định `postgres`/`redis` khi backend chạy trực tiếp trên máy, vì các hostname đó chỉ có trong Docker network.

### Bước 2: kiểm tra

Mở các URL sau:

- Health API: `http://localhost:8080/api/health`
- Actuator health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Ví dụ PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Lần chạy đầu, Flyway sẽ tạo schema và seed role/menu cơ bản. Database đã có user `admin`, nhưng tài liệu hiện không công bố mật khẩu seed; không dùng tài khoản này cho môi trường production trước khi đặt lại mật khẩu an toàn.

### Chạy test và build file JAR

```powershell
mvn test
mvn clean package
java -jar target/milktea-backend-0.0.1-SNAPSHOT.jar
```

Lệnh `java -jar` cũng cần các environment variables database, Redis và JWT như ở bước 1.

### Dừng và xóa dữ liệu local (chỉ khi cần làm mới)

```powershell
docker compose down
```

Lệnh trên giữ PostgreSQL volume. Để xóa toàn bộ dữ liệu local và để Flyway tạo lại từ đầu:

```powershell
docker compose down -v
```

> `down -v` xóa dữ liệu PostgreSQL của môi trường local; không dùng lệnh này trên server production.

## 5. Deploy bằng Docker Compose

Docker Compose build backend từ `Dockerfile`, chạy application với profile `prod`, và tự kết nối đến PostgreSQL/Redis trong cùng network.

### Bước 1: chuẩn bị server

- Cài Docker Engine và Docker Compose plugin.
- Clone/copy source code lên server.
- Tạo `.env` từ `.env.example`, sau đó thay `POSTGRES_PASSWORD` và `JWT_SECRET` bằng secret riêng của môi trường deploy.
- Chỉ mở `BACKEND_PORT` nếu API cần được truy cập trực tiếp. Nếu dùng Nginx/reverse proxy, chỉ expose proxy ra Internet.

### Bước 2: build và khởi động

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

Khi `backend` healthy, kiểm tra:

```bash
curl http://localhost:8081/api/health
curl http://localhost:8081/actuator/health
```

Nếu đổi `BACKEND_PORT` trong `.env`, thay `8081` bằng giá trị mới.

### Cập nhật phiên bản mới

Từ thư mục source đã cập nhật:

```bash
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 backend
```

Flyway chỉ áp dụng các migration mới chưa từng chạy. Không sửa nội dung migration đã được áp dụng trên production; cần thay đổi schema thì tạo file migration mới trong `src/main/resources/db/migration/`.

## 6. Dùng reverse proxy (khuyến nghị production)

Đặt Nginx hoặc proxy tương đương trước backend để xử lý HTTPS. Ví dụ location tối thiểu, khi backend đang map ra host tại `127.0.0.1:8081`:

```nginx
location / {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Sau đó xác nhận qua domain:

```bash
curl https://api.example.com/api/health
```

## 7. Xử lý sự cố thường gặp

| Hiện tượng | Cách kiểm tra/khắc phục |
| --- | --- |
| Backend không kết nối DB khi chạy từ mã nguồn | Đặt `DB_URL` thành `jdbc:postgresql://localhost:5433/milktea`; kiểm tra `docker compose ps` và mật khẩu trong `.env`. |
| Lỗi kết nối Redis khi chạy từ mã nguồn | Đặt `REDIS_HOST=localhost`, `REDIS_PORT=6380`. |
| Port đã được dùng | Đổi `POSTGRES_PORT`, `REDIS_PORT`, hoặc `BACKEND_PORT` trong `.env`; khi chạy source, cập nhật lại `DB_URL`/`REDIS_PORT` tương ứng. |
| Container backend không lên | Xem `docker compose logs backend`; thường do `JWT_SECRET` thiếu/không hợp lệ, DB chưa healthy, hoặc migration lỗi. |
| Schema/migration lỗi sau khi cập nhật | Kiểm tra `flyway_schema_history` và log backend. Không xóa volume production để bỏ qua migration. |
| Swagger không mở | Đảm bảo backend healthy và truy cập đúng `/swagger-ui.html`. |

## 8. Thông tin bàn giao cho partner

- Source/configuration: `pom.xml`, `Dockerfile`, `docker-compose.yml`, `.env.example`.
- API contract: Swagger UI tại `http://<host>:<BACKEND_PORT>/swagger-ui.html`.
- Health check cho load balancer/monitoring: `GET /actuator/health` (hoặc `GET /api/health`).
- Tài liệu kiến trúc/nghiệp vụ: [Kiến trúc phase 1–5](kien-truc-phase-1-5.md).

