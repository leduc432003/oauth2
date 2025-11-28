# 🚀 Quick Start - Push lên GitHub và Enable CI/CD

## Bước 1: Tạo Repository trên GitHub

1. Truy cập [GitHub](https://github.com/)
2. Click nút **"New repository"** (hoặc dấu + ở góc trên bên phải)
3. Điền thông tin:
   - **Repository name**: `oauth2` (hoặc tên bạn muốn)
   - **Description**: "OAuth2 JWT Authentication with Redis"
   - **Visibility**: Public hoặc Private
   - **KHÔNG** chọn "Initialize this repository with a README"
4. Click **"Create repository"**

## Bước 2: Push Code lên GitHub

Mở terminal trong thư mục dự án và chạy các lệnh sau:

```bash
# Kiểm tra git đã được khởi tạo chưa
git status

# Nếu chưa có git, khởi tạo
git init

# Add tất cả files
git add .

# Commit
git commit -m "feat: Initial commit with CI/CD setup"

# Thêm remote repository (thay YOUR_USERNAME bằng username GitHub của bạn)
git remote add origin https://github.com/leduc432003/oauth2.git

# Đổi branch sang main (nếu đang ở master)
git branch -M main

# Push lên GitHub
git push -u origin main
```

## Bước 3: Cấu hình GitHub Secrets

### 3.1. Tạo Docker Hub Access Token

1. Đăng nhập vào [Docker Hub](https://hub.docker.com/)
2. Click vào **Account Settings** (góc trên bên phải)
3. Chọn **Security** → **New Access Token**
4. Đặt tên: `github-actions`
5. Chọn permissions: **Read, Write, Delete**
6. Click **Generate**
7. **COPY TOKEN NGAY** (chỉ hiển thị 1 lần!)

### 3.2. Thêm Secrets vào GitHub

1. Vào repository trên GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**
4. Thêm các secrets sau:

**DOCKER_USERNAME**
- Name: `DOCKER_USERNAME`
- Secret: `your-dockerhub-username` (ví dụ: `leduc432003`)

**DOCKER_PASSWORD**
- Name: `DOCKER_PASSWORD`
- Secret: `<paste-token-from-step-3.1>`

## Bước 4: Test CI/CD

### 4.1. Kiểm tra Workflows

1. Vào repository trên GitHub
2. Click tab **"Actions"**
3. Bạn sẽ thấy workflows đang chạy:
   - ✅ **Build and Test** - Chạy cho mọi push
   - ✅ **CI/CD Pipeline** - Chạy cho push vào main/develop

### 4.2. Xem Kết quả

- Click vào workflow run để xem chi tiết
- Nếu có lỗi, click vào job bị lỗi để xem logs
- Khi thành công, bạn sẽ thấy dấu ✅ màu xanh

### 4.3. Kiểm tra Docker Image

Sau khi CI/CD chạy thành công:

1. Vào [Docker Hub](https://hub.docker.com/)
2. Vào **Repositories**
3. Bạn sẽ thấy image mới: `your-username/oauth2-jwt:latest`

## Bước 5: Test với Pull Request

Tạo một branch mới và test workflow:

```bash
# Tạo branch mới
git checkout -b feature/test-ci

# Thay đổi một file bất kỳ (ví dụ README)
echo "Test CI/CD" >> README.md

# Commit và push
git add .
git commit -m "test: Test CI/CD workflow"
git push origin feature/test-ci
```

Trên GitHub:
1. Click **"Compare & pull request"**
2. Tạo Pull Request
3. Xem CI/CD tự động chạy tests
4. Nếu pass, merge vào main

## Bước 6: Tạo Release (Optional)

Để tạo release với tag:

```bash
# Tạo tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag lên GitHub
git push origin v1.0.0
```

Workflow **Create Release** sẽ tự động:
- Tạo GitHub Release
- Generate changelog
- Upload JAR file

## 🎯 Checklist

- [ ] Repository đã được tạo trên GitHub
- [ ] Code đã được push lên GitHub
- [ ] `DOCKER_USERNAME` secret đã được thêm
- [ ] `DOCKER_PASSWORD` secret đã được thêm
- [ ] Workflows đã chạy thành công (check tab Actions)
- [ ] Docker image đã được push lên Docker Hub
- [ ] Badges trên README hiển thị đúng

## 🔧 Troubleshooting

### Lỗi: "remote: Repository not found"
```bash
# Kiểm tra remote URL
git remote -v

# Sửa lại nếu sai
git remote set-url origin https://github.com/leduc432003/oauth2.git
```

### Lỗi: "failed to push some refs"
```bash
# Pull trước khi push
git pull origin main --rebase
git push origin main
```

### Workflow failed: "Error: Docker login failed"
- Kiểm tra `DOCKER_USERNAME` và `DOCKER_PASSWORD` đã đúng chưa
- Token Docker Hub còn hiệu lực không
- Username có chính xác không (không có khoảng trắng)

### Workflow failed: "Tests failed"
```bash
# Chạy tests local trước
mvn test

# Fix lỗi rồi commit lại
git add .
git commit -m "fix: Fix failing tests"
git push
```

## 📚 Next Steps

Sau khi setup xong CI/CD:

1. **Setup SonarCloud** (Optional):
   - Xem [CI-CD-GUIDE.md](./CI-CD-GUIDE.md) section 3
   - Thêm code quality analysis

2. **Setup Auto Deploy**:
   - Uncomment phần deploy trong `ci-cd.yml`
   - Thêm server credentials vào Secrets

3. **Customize Workflows**:
   - Thêm notifications (Slack, Discord)
   - Thêm performance tests
   - Thêm security scans

## 🆘 Cần Giúp?

Nếu gặp vấn đề:
1. Xem logs trong GitHub Actions
2. Đọc [CI-CD-GUIDE.md](./CI-CD-GUIDE.md) để biết chi tiết
3. Check [GitHub Actions Documentation](https://docs.github.com/en/actions)

---

**Chúc bạn thành công! 🎉**
