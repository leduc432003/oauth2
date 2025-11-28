# 🚀 Hướng dẫn CI/CD với GitHub Actions

## 📋 Tổng quan

Dự án này sử dụng GitHub Actions để tự động hóa quy trình CI/CD, bao gồm:
- ✅ Build và test code tự động
- 🐳 Build và push Docker images
- 🔍 Kiểm tra code quality với SonarCloud
- 🔒 Scan bảo mật với Trivy
- 🚀 Deploy tự động (optional)

## 🔧 Cấu hình ban đầu

### 1. Tạo GitHub Repository Secrets

Truy cập: `Settings` → `Secrets and variables` → `Actions` → `New repository secret`

Thêm các secrets sau:

#### **Bắt buộc cho Docker:**
- `DOCKER_USERNAME`: Username Docker Hub của bạn
- `DOCKER_PASSWORD`: Password hoặc Access Token của Docker Hub

#### **Optional cho SonarCloud:**
- `SONAR_TOKEN`: Token từ SonarCloud (để phân tích code quality)

#### **Optional cho Deploy:**
- `PRODUCTION_HOST`: IP hoặc domain của server production
- `PRODUCTION_USERNAME`: Username SSH
- `PRODUCTION_SSH_KEY`: Private SSH key để kết nối server

### 2. Tạo Docker Hub Access Token

1. Đăng nhập vào [Docker Hub](https://hub.docker.com/)
2. Vào `Account Settings` → `Security` → `New Access Token`
3. Đặt tên token (ví dụ: `github-actions`)
4. Copy token và lưu vào GitHub Secret `DOCKER_PASSWORD`

### 3. Setup SonarCloud (Optional - để phân tích code quality)

1. Truy cập [SonarCloud](https://sonarcloud.io/)
2. Đăng nhập bằng GitHub account
3. Import repository của bạn
4. Lấy token từ `My Account` → `Security` → `Generate Token`
5. Lưu token vào GitHub Secret `SONAR_TOKEN`

## 📁 Cấu trúc Workflows

### 1. **ci-cd.yml** - Pipeline chính

Workflow này chạy khi có push hoặc PR vào branch `main` hoặc `develop`:

```yaml
Jobs:
├── build-and-test      # Build code và chạy tests
├── code-quality        # Phân tích code với SonarCloud
├── docker-build-push   # Build và push Docker image
├── security-scan       # Scan bảo mật với Trivy
└── deploy-production   # Deploy lên production (commented)
```

**Trigger:**
- Push vào `main` hoặc `develop`
- Pull request vào `main` hoặc `develop`

### 2. **build.yml** - Build đơn giản

Workflow nhẹ hơn, chạy cho mọi branch:

```yaml
Jobs:
└── build              # Build và test đơn giản
```

**Trigger:**
- Push vào bất kỳ branch nào
- Pull request vào bất kỳ branch nào

## 🔄 Quy trình làm việc

### Khi tạo Pull Request:
1. ✅ Code được build và test tự động
2. ✅ Kiểm tra code quality
3. ✅ Kết quả hiển thị trên PR

### Khi merge vào `main` hoặc `develop`:
1. ✅ Build và test
2. ✅ Phân tích code quality
3. 🐳 Build Docker image
4. 🐳 Push image lên Docker Hub và GitHub Container Registry
5. 🔒 Scan bảo mật
6. 🚀 Deploy (nếu được enable)

## 🐳 Docker Images

Sau khi build thành công, Docker images sẽ được push với các tags:

- `latest` - Từ branch main
- `develop` - Từ branch develop
- `main-<commit-sha>` - Từ commit cụ thể
- `v1.0.0` - Từ git tags (semantic versioning)

**Locations:**
- Docker Hub: `<your-username>/oauth2-jwt:latest`
- GitHub Container Registry: `ghcr.io/<your-username>/oauth2-jwt:latest`

## 🚀 Enable Deploy tự động

Để enable deploy tự động lên server:

1. **Uncomment phần deploy** trong `ci-cd.yml`:
   ```yaml
   # Bỏ comment từ dòng 134-155
   deploy-production:
     name: Deploy to Production
     ...
   ```

2. **Thêm GitHub Secrets:**
   - `PRODUCTION_HOST`: IP server của bạn
   - `PRODUCTION_USERNAME`: Username SSH
   - `PRODUCTION_SSH_KEY`: Private key SSH

3. **Chuẩn bị server:**
   ```bash
   # Trên server, cài đặt Docker và Docker Compose
   sudo apt update
   sudo apt install docker.io docker-compose -y
   
   # Clone repository hoặc tạo docker-compose.yml
   mkdir -p /opt/oauth2-jwt
   cd /opt/oauth2-jwt
   
   # Copy docker-compose.yml từ repo
   # Sửa image name trong docker-compose.yml thành:
   # image: <your-dockerhub-username>/oauth2-jwt:latest
   ```

4. **Test SSH connection:**
   ```bash
   ssh -i your-private-key.pem username@your-server-ip
   ```

## 📊 Monitoring và Logs

### Xem workflow runs:
1. Vào repository trên GitHub
2. Click tab `Actions`
3. Chọn workflow run để xem chi tiết

### Xem logs:
- Click vào job cụ thể
- Expand các steps để xem logs chi tiết

### Xem test reports:
- Test reports được tự động tạo và attach vào workflow run
- Download artifacts để xem chi tiết

## 🔍 Troubleshooting

### Build failed?
```bash
# Kiểm tra logs trong GitHub Actions
# Thường do:
- Dependencies không tải được
- Tests failed
- Code compilation errors
```

### Docker push failed?
```bash
# Kiểm tra:
- DOCKER_USERNAME và DOCKER_PASSWORD đã đúng chưa
- Docker Hub repository đã tồn tại chưa
- Token còn hiệu lực không
```

### Deploy failed?
```bash
# Kiểm tra:
- SSH key đã đúng format chưa
- Server có thể SSH được không
- Docker đã cài trên server chưa
- Port có bị firewall block không
```

## 🎯 Best Practices

1. **Branch Strategy:**
   - `main` - Production code
   - `develop` - Development code
   - `feature/*` - Feature branches
   - `hotfix/*` - Hotfix branches

2. **Commit Messages:**
   ```
   feat: Add new authentication feature
   fix: Fix token expiration bug
   docs: Update README
   test: Add unit tests for AuthService
   ```

3. **Pull Requests:**
   - Luôn tạo PR thay vì push trực tiếp vào main
   - Đợi CI pass trước khi merge
   - Request review từ team members

4. **Versioning:**
   ```bash
   # Tạo git tag để release version mới
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

## 📚 Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Hub](https://hub.docker.com/)
- [SonarCloud](https://sonarcloud.io/)
- [Trivy Security Scanner](https://github.com/aquasecurity/trivy)

## 🆘 Support

Nếu gặp vấn đề, hãy:
1. Kiểm tra logs trong GitHub Actions
2. Xem phần Troubleshooting ở trên
3. Tạo issue trong repository
4. Liên hệ team để được hỗ trợ

---

**Happy Coding! 🎉**
