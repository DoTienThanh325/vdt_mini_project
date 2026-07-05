# Hệ thống quản lý và liên thông văn bản

Ứng dụng hỗ trợ quản lý vòng đời văn bản trong tổ chức và trao đổi văn bản giữa
các hệ thống liên thông. Repository gồm backend Spring Boot, frontend Angular và
hạ tầng Docker cho cơ sở dữ liệu, cache, lưu trữ log tập trung.

## Chức năng chính

- Đăng nhập và xác thực bằng JWT.
- Quản lý người dùng, vai trò và đơn vị.
- Quản lý các hệ thống liên thông và API key.
- Tạo văn bản, tải tệp đính kèm, phê duyệt hoặc từ chối văn bản.
- Ký số, kiểm tra tính hợp lệ của chữ ký trước khi gửi.
- Gửi, nhận và phản hồi văn bản giữa các đơn vị.
- Mã hóa dữ liệu liên thông bằng AES-256-GCM.
- Thông báo thời gian thực qua WebSocket/STOMP.
- Tìm kiếm, lọc và phân trang danh sách văn bản.
- Thu thập và quan sát log qua Filebeat, Logstash, Elasticsearch và Kibana.

## Vai trò người dùng

| Vai trò | Trách nhiệm chính |
| --- | --- |
| `ADMIN` | Quản trị toàn bộ hệ thống |
| `ORGADMIN` | Quản trị người dùng và thông tin đơn vị |
| `STAFF` | Tạo và xử lý văn bản |
| `MANAGER` | Phê duyệt hoặc từ chối văn bản |
| `LEADER` | Ký và quản lý văn bản |
| `CLERK` | Gửi, tiếp nhận và phản hồi văn bản |

Luồng xử lý chính:

```text
Tạo văn bản -> Phê duyệt -> Ký số -> Xác minh chữ ký
             -> Gửi liên thông -> Tiếp nhận -> Phản hồi
```

## Công nghệ sử dụng

### Backend

- Java 21, Spring Boot 3.5
- Spring Web, Spring Security, Spring Data JPA
- JWT, Bean Validation
- MariaDB
- Redis Cache
- WebSocket/STOMP
- Maven

### Frontend

- Angular 21, TypeScript
- Bootstrap 5
- RxJS
- Vitest
- Nginx khi triển khai bằng Docker

### Hạ tầng

- Docker Compose
- Elasticsearch, Logstash, Kibana
- Filebeat

## Kiến trúc repository

```text
.
├── documentmanagement/                    # Backend Spring Boot
│   ├── .mvn/
│   │   └── wrapper/                       # Maven Wrapper
│   ├── logs/                              # Log khi chạy local
│   ├── uploads/                           # Tệp tải lên khi chạy local
│   └── src/
│   │   ├── main/
│   │   │   ├── java/com/vdt/documenttransfer/
│   │   │   │   ├── common/               # Thành phần dùng chung
│   │   │   │   │   ├── config/           # Cấu hình Spring, Security, Redis, WebSocket
│   │   │   │   │   ├── exception/        # Xử lý exception toàn cục
│   │   │   │   │   ├── logging/          # Logging và MDC
│   │   │   │   │   ├── response/         # Response dùng chung
│   │   │   │   │   ├── security/         # JWT và xác thực người dùng
│   │   │   │   │   └── util/             # Tiện ích mã hóa và xử lý dữ liệu
│   │   │   │   ├── infrastructure/        # Tích hợp hạ tầng
│   │   │   │   │   ├── client/           # Client gọi hệ thống ngoài
│   │   │   │   │   ├── crypto/           # Thành phần mật mã
│   │   │   │   │   ├── mail/             # Hạ tầng gửi email
│   │   │   │   │   └── storage/          # Hạ tầng lưu trữ tệp
│   │   │   │   ├── modules/               # Các module nghiệp vụ
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── document/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── mapper/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── documentfile/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── interconnectedsystem/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── mapper/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── notification/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── organization/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── mapper/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── role/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── signature/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   ├── system/           # API mô phỏng hệ thống liên thông
│   │   │   │   │   │   ├── DN/
│   │   │   │   │   │   │   └── controller/
│   │   │   │   │   │   ├── HCM/
│   │   │   │   │   │   ├── HN/
│   │   │   │   │   │   └── HP/
│   │   │   │   │   │       ├── controller/
│   │   │   │   │   │       └── service/
│   │   │   │   │   │           └── impl/
│   │   │   │   │   ├── transfer/
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       ├── components/
│   │   │   │   │   │       └── impl/
│   │   │   │   │   └── user/
│   │   │   │   │       ├── controller/
│   │   │   │   │       ├── dto/
│   │   │   │   │       ├── entity/
│   │   │   │   │       ├── repository/
│   │   │   │   │       ├── service/
│   │   │   │   │       │   └── impl/
│   │   │   │   │       └── utils/
│   │   │   └── resources/
│   │   │       ├── db/
│   │   │       │   └── migration/         # Script tạo bảng và dữ liệu mẫu
│   │   │       ├── static/
│   │   │       └── templates/
│   │   └── test/
│   │       └── java/com/vdt/documenttransfer/
│   │           ├── common/
│   │           │   └── logging/
│   │           ├── infrastructure/
│   │           └── modules/
│   │               ├── auth/
│   │               ├── document/
│   │               ├── notification/
│   │               │   └── service/
│   │               │       └── impl/
│   │               ├── signature/
│   │               ├── transfer/
│   │               └── user/
├── documentmanagement-fe/    # Frontend Angular
│   └── src/app/
│       ├── pages/            # Các màn hình nghiệp vụ
│       ├── services/         # Gọi REST API và WebSocket
│       ├── models/           # Kiểu dữ liệu
│       └── utils/            # Tiện ích dùng chung
├── elk/                      # Cấu hình Filebeat và Logstash
├── uploads/                  # Volume lưu tệp khi chạy Docker
└── logs/                     # Volume lưu log
```

Backend được tổ chức theo các module nghiệp vụ: `auth`, `user`, `role`,
`organization`, `interconnectedsystem`, `document`, `documentfile`, `signature`,
`transfer` và `notification`.

## Khởi chạy bằng Docker

### Yêu cầu

- Docker Engine
- Docker Compose

Tạo file `.env` tại thư mục gốc:

```env
DB_PASSWORD=replace-with-a-strong-database-password
JWT_SECRET=replace-with-a-secret-of-at-least-32-characters
AES_SECRET_KEY=replace-with-a-base64-encoded-256-bit-aes-key
```

`AES_SECRET_KEY` phải được tạo một lần và giữ cố định. Nếu thay hoặc làm mất key,
dữ liệu đã mã hóa bằng key cũ sẽ không thể giải mã.

Khởi chạy:

```bash
docker compose up -d --build
```

Các địa chỉ mặc định:

| Thành phần | Địa chỉ |
| --- | --- |
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8081 |
| MariaDB | localhost:3307 |
| Redis | localhost:6379 |
| Elasticsearch | http://localhost:9200 |
| Kibana | http://localhost:5601 |

Dừng hệ thống:

```bash
docker compose down
```

## Khởi chạy trong môi trường phát triển

### Backend

Yêu cầu Java 21, MariaDB và Redis. Cấu hình local nằm trong
`documentmanagement/src/main/resources/application-dev.properties`.

```bash
cd documentmanagement
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Trên Windows:

```powershell
cd documentmanagement
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### Frontend

Yêu cầu Node.js 22 và npm.

```bash
cd documentmanagement-fe
npm install
npm start
```

Frontend hiện gọi backend tại `http://localhost:8081`.

## Dữ liệu mẫu

Các script SQL nằm trong:

- `documentmanagement/src/main/resources/db/migration/V1__create_tables.sql`
- `documentmanagement/src/main/resources/db/migration/V2__insert_seed_data.sql`

`V2__insert_seed_data.sql` cung cấp các hệ thống, đơn vị, vai trò và tài khoản
mẫu. Mật khẩu chung của dữ liệu mẫu là `123456`, chỉ dùng cho môi trường phát
triển. Ví dụ:

- `admin`
- `staff_hn`
- `manager_hn`
- `leader_hn`
- `clerk_hn`

Nếu môi trường không tự chạy migration, cần thực thi lần lượt `V1` và `V2` bằng
MariaDB client.

## API chính

| Nhóm | Base path |
| --- | --- |
| Xác thực | `/api/auth` |
| Người dùng | `/api/users` |
| Vai trò | `/api/roles` |
| Đơn vị | `/api/organizations` |
| Hệ thống liên thông | `/api/interconnected-systems` |
| Văn bản và tệp | `/api/documents` |
| Thông báo | `/api/notifications` |
| WebSocket | `/ws` |

Các API được bảo vệ sử dụng header:

```http
Authorization: Bearer <access-token>
```

API nhận văn bản liên thông sử dụng thêm:

```http
X-API-KEY: <system-api-key>
```

## Kiểm thử

Backend:

```bash
cd documentmanagement
./mvnw test
```

Frontend:

```bash
cd documentmanagement-fe
npm test -- --watch=false
```

Build frontend:

```bash
cd documentmanagement-fe
npm run build
```

## Lưu ý bảo mật

- Không commit `.env`, JWT secret, AES key, API key hoặc mật khẩu thật.
- Không gửi AES key xuống frontend hoặc truyền key trong request liên thông.
- Chỉ các service backend cần giải mã mới được cấp cùng AES key qua secret
  management.
- Thay toàn bộ tài khoản, mật khẩu và API key mẫu trước khi triển khai.
- Production cần sử dụng HTTPS và giới hạn CORS/WebSocket origin phù hợp.
