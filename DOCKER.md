# Docker Deployment Guide

## Tổng quan

Dự án này sử dụng Docker và Docker Compose để triển khai ứng dụng OAuth2 JWT Spring Boot với MySQL và Redis.

## Yêu cầu

- Docker (phiên bản 20.10 trở lên)
- Docker Compose (phiên bản 2.0 trở lên)

## Cấu trúc Docker

### Services

1. **app** - Ứng dụng Spring Boot OAuth2 JWT
   - Port: 8080
   - Build từ Dockerfile multi-stage
   
2. **db** - MySQL 8.0
   - Port: 3307 (host) -> 3306 (container)
   - Database: oauth2
   
3. **redis** - Redis 7 Alpine
   - Port: 6379
   - Persistent storage với volume

## Hướng dẫn sử dụng

### 1. Cấu hình môi trường

Tạo file `.env` từ `.env.example`:

```bash
cp .env.example .env
```

Cập nhật các biến môi trường trong file `.env`:

```env
# Google OAuth2 Configuration (BẮT BUỘC)
GOOGLE_CLIENT_ID=your-actual-google-client-id
GOOGLE_CLIENT_SECRET=your-actual-google-client-secret

# JWT Secret (Khuyến nghị thay đổi cho production)
JWT_SECRET=your-secure-jwt-secret-key-base64-encoded

# OAuth2 Redirect URIs
OAUTH2_SUCCESS_REDIRECT_URI=http://localhost:3000/oauth2/success
OAUTH2_FAILURE_REDIRECT_URI=http://localhost:3000/oauth2/failure
```

### 2. Build và chạy ứng dụng

#### Chạy tất cả services:

```bash
docker-compose up -d
```

#### Build lại image khi có thay đổi code:

```bash
docker-compose up -d --build
```

#### Chỉ build app service:

```bash
docker-compose build app
```

### 3. Kiểm tra trạng thái

#### Xem logs của tất cả services:

```bash
docker-compose logs -f
```

#### Xem logs của một service cụ thể:

```bash
docker-compose logs -f app
docker-compose logs -f db
docker-compose logs -f redis
```

#### Kiểm tra trạng thái services:

```bash
docker-compose ps
```

### 4. Truy cập ứng dụng

- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/api-docs
- **Health Check**: http://localhost:8080/api/actuator/health

### 5. Dừng và xóa containers

#### Dừng services:

```bash
docker-compose stop
```

#### Dừng và xóa containers:

```bash
docker-compose down
```

#### Dừng, xóa containers và volumes (XÓA DỮ LIỆU):

```bash
docker-compose down -v
```

## Dockerfile Features

### Multi-stage Build

Dockerfile sử dụng multi-stage build để tối ưu kích thước image:

1. **Build Stage**: Sử dụng Maven để build ứng dụng
2. **Runtime Stage**: Chỉ chứa JRE và JAR file

### Security Best Practices

- Chạy ứng dụng với non-root user
- Sử dụng Alpine Linux để giảm attack surface
- Health check tự động

### Optimizations

- Layer caching cho dependencies
- JVM options tối ưu cho container
- Health checks cho tất cả services

## Troubleshooting

### Lỗi kết nối database

Nếu app không kết nối được với database, kiểm tra:

```bash
# Kiểm tra MySQL đã ready chưa
docker-compose logs db

# Kiểm tra health check
docker-compose ps
```

### Lỗi build

Nếu build thất bại:

```bash
# Xóa cache và build lại
docker-compose build --no-cache app
```

### Lỗi port đã được sử dụng

Nếu port 8080, 3307, hoặc 6379 đã được sử dụng:

1. Dừng service đang sử dụng port đó
2. Hoặc thay đổi port mapping trong `docker-compose.yml`

### Xem logs chi tiết

```bash
# Logs của app service
docker-compose logs -f --tail=100 app

# Exec vào container để debug
docker-compose exec app sh
```

## Production Deployment

### Khuyến nghị cho Production

1. **Thay đổi JWT Secret**: Tạo secret key mạnh và unique
2. **Cấu hình HTTPS**: Sử dụng reverse proxy (Nginx, Traefik)
3. **Database**: Sử dụng managed database service
4. **Redis**: Sử dụng Redis Cluster hoặc managed service
5. **Monitoring**: Thêm Prometheus, Grafana
6. **Logging**: Centralized logging với ELK stack
7. **Backup**: Tự động backup database

### Environment Variables cho Production

```env
# Production database
SPRING_DATASOURCE_URL=jdbc:mysql://production-db-host:3306/oauth2
SPRING_DATASOURCE_USERNAME=production_user
SPRING_DATASOURCE_PASSWORD=strong_password

# Production Redis
REDIS_HOST=production-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

# Production JWT
JWT_SECRET=very-long-and-secure-base64-encoded-secret

# Production OAuth2
GOOGLE_CLIENT_ID=production-client-id
GOOGLE_CLIENT_SECRET=production-client-secret
OAUTH2_SUCCESS_REDIRECT_URI=https://yourdomain.com/oauth2/success
OAUTH2_FAILURE_REDIRECT_URI=https://yourdomain.com/oauth2/failure
```

## Volumes

- `db-data`: Lưu trữ dữ liệu MySQL
- `redis-data`: Lưu trữ dữ liệu Redis

Dữ liệu sẽ được giữ lại ngay cả khi containers bị xóa (trừ khi dùng `docker-compose down -v`).

## Network

Tất cả services chạy trong cùng một Docker network, cho phép chúng giao tiếp với nhau thông qua service names:

- `app` có thể kết nối tới `db:3306`
- `app` có thể kết nối tới `redis:6379`

## Maintenance

### Backup Database

```bash
# Backup
docker-compose exec db mysqldump -u root -p123456 oauth2 > backup.sql

# Restore
docker-compose exec -T db mysql -u root -p123456 oauth2 < backup.sql
```

### Update Dependencies

```bash
# Rebuild với dependencies mới
docker-compose build --no-cache app
docker-compose up -d app
```

## Support

Nếu gặp vấn đề, vui lòng:

1. Kiểm tra logs: `docker-compose logs -f`
2. Kiểm tra health checks: `docker-compose ps`
3. Xem documentation trong README.md chính
