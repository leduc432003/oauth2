# Quick Start Guide - Docker

## Bước 1: Chuẩn bị môi trường

1. Đảm bảo đã cài đặt Docker và Docker Compose
2. Copy file `.env.example` thành `.env`:
   ```bash
   cp .env.example .env
   ```
3. Cập nhật Google OAuth2 credentials trong file `.env`

## Bước 2: Chạy ứng dụng

### Sử dụng script tiện ích (Khuyến nghị)

**Windows:**
```bash
docker.bat start
```

**Linux/Mac:**
```bash
chmod +x docker.sh
./docker.sh start
```

### Hoặc sử dụng Docker Compose trực tiếp

```bash
docker-compose up -d
```

## Bước 3: Kiểm tra

Xem logs:
```bash
# Windows
docker.bat logs

# Linux/Mac
./docker.sh logs

# Hoặc
docker-compose logs -f
```

Kiểm tra health:
```bash
# Windows
docker.bat health

# Linux/Mac
./docker.sh health
```

## Truy cập ứng dụng

- API: http://localhost:8080/api
- Swagger: http://localhost:8080/api/api-docs
- Health: http://localhost:8080/api/actuator/health

## Các lệnh hữu ích

| Lệnh | Windows | Linux/Mac | Mô tả |
|------|---------|-----------|-------|
| Start | `docker.bat start` | `./docker.sh start` | Khởi động services |
| Stop | `docker.bat stop` | `./docker.sh stop` | Dừng services |
| Restart | `docker.bat restart` | `./docker.sh restart` | Khởi động lại |
| Build | `docker.bat build` | `./docker.sh build` | Build và start |
| Logs | `docker.bat logs` | `./docker.sh logs` | Xem logs |
| Status | `docker.bat status` | `./docker.sh status` | Kiểm tra status |
| Health | `docker.bat health` | `./docker.sh health` | Kiểm tra health |
| Backup | `docker.bat backup` | `./docker.sh backup` | Backup database |
| Clean | `docker.bat clean` | `./docker.sh clean` | Xóa tất cả |

## Troubleshooting

### Port đã được sử dụng
Thay đổi port mapping trong `docker-compose.yml`

### Build lỗi
```bash
docker-compose build --no-cache app
```

### Xem logs chi tiết
```bash
docker-compose logs -f app
```

Xem thêm chi tiết trong [DOCKER.md](DOCKER.md)
