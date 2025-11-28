# OAuth2 JWT Authentication with Redis

[![CI/CD Pipeline](https://github.com/leduc432003/oauth2/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/leduc432003/oauth2/actions/workflows/ci-cd.yml)
[![Build and Test](https://github.com/leduc432003/oauth2/actions/workflows/build.yml/badge.svg)](https://github.com/leduc432003/oauth2/actions/workflows/build.yml)
[![Docker Image](https://img.shields.io/docker/v/your-dockerhub-username/oauth2-jwt?label=docker&logo=docker)](https://hub.docker.com/r/your-dockerhub-username/oauth2-jwt)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Spring Boot application với OAuth2 (Google) và JWT authentication, sử dụng Redis để lưu trữ refresh tokens.

> 📖 **[Xem hướng dẫn CI/CD chi tiết](./CI-CD-GUIDE.md)**

## 🚀 Tính năng

- ✅ **Local Authentication**: Đăng ký và đăng nhập bằng email/password
- ✅ **Google OAuth2**: Đăng nhập bằng tài khoản Google
- ✅ **JWT Tokens**: Access token và refresh token
- ✅ **Redis Storage**: Lưu trữ refresh tokens trong Redis với TTL tự động
- ✅ **Role-based Authorization**: Phân quyền ADMIN/USER
- ✅ **Swagger UI**: API documentation

## 📋 Yêu cầu

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Google Cloud Console account (để lấy OAuth2 credentials)

## 🛠️ Cài đặt

### 1. Clone repository

```bash
git clone <your-repo-url>
cd oauth2-jwt
```

### 2. Cấu hình Google OAuth2

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Vào **APIs & Services** > **Credentials**
4. Tạo **OAuth 2.0 Client ID**:
   - Application type: **Web application**
   - Authorized redirect URIs: `http://localhost:8080/api/login/oauth2/code/google`
5. Copy **Client ID** và **Client Secret**

### 3. Cấu hình Environment Variables

Tạo file `.env` từ `.env.example`:

```bash
cp .env.example .env
```

Cập nhật các giá trị trong `.env`:

```env
GOOGLE_CLIENT_ID=your-actual-client-id
GOOGLE_CLIENT_SECRET=your-actual-client-secret
```

### 4. Khởi động MySQL và Redis

```bash
docker-compose up -d
```

Kiểm tra services đang chạy:

```bash
docker-compose ps
```

### 5. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc build và chạy:

```bash
mvn clean package
java -jar target/oauth2-jwt-0.0.1-SNAPSHOT.jar
```

## 📚 API Documentation

Sau khi chạy ứng dụng, truy cập Swagger UI:

```
http://localhost:8080/api/api-docs
```

## 🔑 API Endpoints

### Authentication

#### 1. Đăng ký (Local)
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### 2. Đăng nhập (Local)
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### 3. Đăng nhập Google
```
GET /api/oauth2/authorization/google
```

#### 4. Refresh Token
```http
POST /api/auth/refresh?refreshToken=<your-refresh-token>
```

#### 5. Logout
```http
POST /api/auth/logout?refreshToken=<your-refresh-token>
```

### Protected Endpoints

#### User Endpoint
```http
GET /api/user/me
Authorization: Bearer <access-token>
```

#### Admin Endpoint
```http
GET /api/admin/users
Authorization: Bearer <access-token>
```

## 🗄️ Redis Data Structure

Refresh tokens được lưu trong Redis với cấu trúc:

```
Key: refresh_tokens:<token-id>
Value: {
  "id": "uuid",
  "token": "refresh-token-string",
  "userId": 1,
  "userEmail": "user@example.com",
  "expiryDate": "2024-12-01T00:00:00Z",
  "timeToLive": 604800
}
TTL: 7 days (tự động xóa khi hết hạn)
```

## 🔒 Security Features

1. **Password Encryption**: BCrypt hashing
2. **JWT Signing**: HS512 algorithm
3. **Refresh Token**: Stored in Redis with TTL
4. **CORS**: Configured for frontend origin
5. **Stateless Sessions**: No server-side session storage

## 🏗️ Kiến trúc

```
oauth2-jwt/
├── config/
│   ├── SecurityConfig.java      # Spring Security configuration
│   └── RedisConfig.java          # Redis configuration
├── controller/
│   ├── AuthController.java       # Authentication endpoints
│   ├── UserController.java       # User endpoints
│   └── AdminController.java      # Admin endpoints
├── model/
│   ├── User.java                 # User entity (MySQL)
│   ├── Role.java                 # Role entity (MySQL)
│   └── RefreshToken.java         # RefreshToken (Redis)
├── repository/
│   ├── UserRepository.java       # JPA repository
│   ├── RoleRepository.java       # JPA repository
│   └── RefreshTokenRepository.java # Redis repository
├── security/
│   ├── JwtTokenProvider.java     # JWT utilities
│   ├── JwtAuthenticationFilter.java
│   ├── OAuth2UserService.java    # OAuth2 user handler
│   ├── OAuth2AuthenticationSuccessHandler.java
│   └── OAuth2AuthenticationFailureHandler.java
└── service/
    ├── AuthService.java          # Authentication logic
    ├── RefreshTokenService.java  # Refresh token management
    └── UserService.java          # User management
```

## 🧪 Testing

### Test Redis Connection

```bash
docker exec -it oauth2-jwt-redis-1 redis-cli
> PING
PONG
> KEYS *
(empty array)
```

### Test MySQL Connection

```bash
docker exec -it oauth2-jwt-db-1 mysql -uroot -p123456 oauth2
mysql> SHOW TABLES;
```

## 🐛 Troubleshooting

### Redis Connection Failed

```bash
# Kiểm tra Redis đang chạy
docker-compose ps redis

# Xem logs
docker-compose logs redis

# Restart Redis
docker-compose restart redis
```

### Google OAuth2 Error

1. Kiểm tra **Authorized redirect URIs** trong Google Console
2. Đảm bảo `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` đúng
3. Xem logs để biết chi tiết lỗi

### JWT Token Invalid

1. Kiểm tra `JWT_SECRET` có đúng không
2. Đảm bảo token chưa hết hạn (60 phút)
3. Kiểm tra format: `Authorization: Bearer <token>`

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `REDIS_PASSWORD` | Redis password | (empty) |
| `JWT_SECRET` | JWT signing key | (base64 encoded) |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | - |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | - |
| `OAUTH2_SUCCESS_REDIRECT_URI` | Success redirect URL | http://localhost:3000/oauth2/success |
| `OAUTH2_FAILURE_REDIRECT_URI` | Failure redirect URL | http://localhost:3000/oauth2/failure |

## 📄 License

MIT License

## 👨‍💻 Author

Your Name
