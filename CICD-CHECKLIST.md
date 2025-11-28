# ✅ CI/CD Setup Checklist

## 📋 Pre-requisites

- [ ] Dự án đã có Git repository local
- [ ] Đã có tài khoản GitHub
- [ ] Đã có tài khoản Docker Hub
- [ ] Code đã được test và chạy thành công local

## 🚀 Phase 1: GitHub Repository Setup

### 1.1 Tạo GitHub Repository
- [ ] Đăng nhập vào GitHub
- [ ] Tạo repository mới
- [ ] Copy repository URL

### 1.2 Push Code lên GitHub
```bash
# Kiểm tra git status
git status

# Add tất cả files
git add .

# Commit với message rõ ràng
git commit -m "feat: Add CI/CD with GitHub Actions"

# Add remote repository
git remote add origin https://github.com/leduc432003/oauth2.git

# Push lên GitHub
git push -u origin main
```

- [ ] Code đã được push lên GitHub
- [ ] Kiểm tra files trên GitHub web interface
- [ ] Workflows folder đã có trên GitHub (`.github/workflows/`)

## 🐳 Phase 2: Docker Hub Setup

### 2.1 Tạo Docker Hub Account
- [ ] Đăng ký tài khoản tại [hub.docker.com](https://hub.docker.com/)
- [ ] Verify email
- [ ] Đăng nhập thành công

### 2.2 Tạo Access Token
- [ ] Vào Account Settings → Security
- [ ] Click "New Access Token"
- [ ] Đặt tên: `github-actions`
- [ ] Chọn permissions: Read, Write, Delete
- [ ] Copy token (chỉ hiển thị 1 lần!)
- [ ] Lưu token vào notepad tạm thời

### 2.3 (Optional) Tạo Repository
- [ ] Vào Repositories → Create Repository
- [ ] Tên: `oauth2-jwt`
- [ ] Visibility: Public hoặc Private
- [ ] Create

## 🔐 Phase 3: GitHub Secrets Configuration

### 3.1 Thêm Docker Secrets
Vào: Repository → Settings → Secrets and variables → Actions → New repository secret

**Secret 1: DOCKER_USERNAME**
- [ ] Click "New repository secret"
- [ ] Name: `DOCKER_USERNAME`
- [ ] Secret: `<your-dockerhub-username>`
- [ ] Click "Add secret"

**Secret 2: DOCKER_PASSWORD**
- [ ] Click "New repository secret"
- [ ] Name: `DOCKER_PASSWORD`
- [ ] Secret: `<paste-docker-hub-token>`
- [ ] Click "Add secret"

### 3.2 Verify Secrets
- [ ] Kiểm tra 2 secrets đã được thêm
- [ ] Không có typo trong tên secrets
- [ ] Values đã được lưu đúng

## 🧪 Phase 4: Test Workflows

### 4.1 Trigger First Workflow
- [ ] Vào tab "Actions" trên GitHub
- [ ] Xem workflows đang chạy
- [ ] Chờ workflows hoàn thành

### 4.2 Check Build Workflow
- [ ] Workflow "Build and Test" đã chạy
- [ ] Status: ✅ Success (hoặc ❌ Failed)
- [ ] Nếu failed, xem logs để debug

### 4.3 Check CI/CD Workflow
- [ ] Workflow "CI/CD Pipeline" đã chạy
- [ ] Job "Build and Test" ✅
- [ ] Job "Code Quality" ✅ (hoặc skipped nếu không có SONAR_TOKEN)
- [ ] Job "Docker Build Push" ✅
- [ ] Job "Security Scan" ✅

### 4.4 Verify Docker Image
- [ ] Đăng nhập vào Docker Hub
- [ ] Vào Repositories
- [ ] Tìm repository `oauth2-jwt`
- [ ] Kiểm tra tags: `latest`, `main-<sha>`
- [ ] Xem image size và upload time

## 🔍 Phase 5: Code Quality (Optional)

### 5.1 Setup SonarCloud
- [ ] Truy cập [sonarcloud.io](https://sonarcloud.io/)
- [ ] Đăng nhập bằng GitHub
- [ ] Import repository
- [ ] Tạo organization (nếu chưa có)

### 5.2 Generate Token
- [ ] Vào My Account → Security
- [ ] Generate Token
- [ ] Copy token

### 5.3 Add to GitHub Secrets
- [ ] Vào GitHub repository
- [ ] Settings → Secrets → New secret
- [ ] Name: `SONAR_TOKEN`
- [ ] Secret: `<paste-sonarcloud-token>`
- [ ] Add secret

### 5.4 Test SonarCloud
- [ ] Push code mới hoặc re-run workflow
- [ ] Kiểm tra job "Code Quality" chạy thành công
- [ ] Xem results trên SonarCloud dashboard

## 📝 Phase 6: Update Documentation

### 6.1 Update README Badges
- [ ] Mở `README.md`
- [ ] Thay `your-dockerhub-username` bằng username thật
- [ ] Commit và push changes
- [ ] Kiểm tra badges hiển thị đúng trên GitHub

### 6.2 Review Documentation
- [ ] Đọc `CI-CD-GUIDE.md`
- [ ] Đọc `QUICKSTART-CICD.md`
- [ ] Đọc `WORKFLOW-DIAGRAM.md`
- [ ] Hiểu rõ quy trình CI/CD

## 🧪 Phase 7: Test với Pull Request

### 7.1 Tạo Feature Branch
```bash
git checkout -b feature/test-ci-cd
```
- [ ] Branch mới đã được tạo

### 7.2 Make Changes
```bash
echo "# Test CI/CD" >> TEST.md
git add TEST.md
git commit -m "test: Test CI/CD workflow"
git push origin feature/test-ci-cd
```
- [ ] Changes đã được push

### 7.3 Create Pull Request
- [ ] Vào GitHub repository
- [ ] Click "Compare & pull request"
- [ ] Tạo PR với title rõ ràng
- [ ] Xem CI/CD tự động chạy

### 7.4 Review PR Checks
- [ ] Build workflow đã chạy
- [ ] CI/CD workflow đã chạy
- [ ] Tất cả checks đều pass ✅
- [ ] Review changes
- [ ] Merge PR

## 🎁 Phase 8: Create Release (Optional)

### 8.1 Prepare Release
- [ ] Đảm bảo code trên main đã stable
- [ ] Update version trong `pom.xml` nếu cần
- [ ] Commit changes

### 8.2 Create Git Tag
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```
- [ ] Tag đã được tạo
- [ ] Tag đã được push

### 8.3 Verify Release
- [ ] Vào tab "Actions"
- [ ] Workflow "Create Release" đã chạy
- [ ] Vào tab "Releases"
- [ ] Release v1.0.0 đã được tạo
- [ ] Changelog đã được generate
- [ ] JAR file đã được upload

## 🚀 Phase 9: Enable Auto Deploy (Optional)

### 9.1 Prepare Server
- [ ] Có server/VPS để deploy
- [ ] Server đã cài Docker và Docker Compose
- [ ] SSH access đã được setup

### 9.2 Generate SSH Key
```bash
ssh-keygen -t rsa -b 4096 -C "github-actions"
```
- [ ] SSH key đã được tạo
- [ ] Public key đã được add vào server
- [ ] Test SSH connection thành công

### 9.3 Add Server Secrets
- [ ] `PRODUCTION_HOST`: Server IP/domain
- [ ] `PRODUCTION_USERNAME`: SSH username
- [ ] `PRODUCTION_SSH_KEY`: Private SSH key

### 9.4 Enable Deploy Job
- [ ] Mở `.github/workflows/ci-cd.yml`
- [ ] Uncomment phần `deploy-production`
- [ ] Update script paths nếu cần
- [ ] Commit và push

### 9.5 Test Deploy
- [ ] Push code mới vào main
- [ ] Xem workflow chạy
- [ ] Deploy job chạy thành công
- [ ] Kiểm tra app trên server

## 📊 Phase 10: Monitoring & Maintenance

### 10.1 Setup Monitoring
- [ ] Enable GitHub Actions notifications
- [ ] Setup email notifications
- [ ] (Optional) Setup Slack/Discord webhooks

### 10.2 Regular Checks
- [ ] Kiểm tra workflow runs hàng tuần
- [ ] Review Dependabot PRs
- [ ] Update dependencies khi cần
- [ ] Monitor Docker image sizes

### 10.3 Security
- [ ] Review security alerts
- [ ] Update secrets khi expired
- [ ] Rotate access tokens định kỳ
- [ ] Review Trivy scan results

## ✨ Bonus: Advanced Features

### Dependabot
- [ ] Dependabot đã được enable
- [ ] Kiểm tra PRs tự động
- [ ] Merge dependency updates

### Branch Protection
- [ ] Enable branch protection cho `main`
- [ ] Require PR reviews
- [ ] Require status checks to pass
- [ ] Require branches to be up to date

### Notifications
- [ ] Setup Slack integration
- [ ] Setup Discord webhooks
- [ ] Setup email notifications

### Performance
- [ ] Monitor workflow execution time
- [ ] Optimize cache usage
- [ ] Review artifact retention

## 🎯 Final Checklist

- [ ] ✅ Code đã được push lên GitHub
- [ ] ✅ Workflows đang chạy tự động
- [ ] ✅ Docker images được build và push
- [ ] ✅ Tests đang chạy tự động
- [ ] ✅ Security scans đang hoạt động
- [ ] ✅ Documentation đã được cập nhật
- [ ] ✅ Team members đã được train
- [ ] ✅ Monitoring đã được setup

## 📈 Success Metrics

Sau khi hoàn thành setup, bạn nên thấy:

- ✅ Build time: < 5 phút
- ✅ Test success rate: > 95%
- ✅ Docker image size: < 500MB
- ✅ Security vulnerabilities: 0 critical
- ✅ Code coverage: > 70% (nếu có)
- ✅ Deployment frequency: Tăng lên
- ✅ Time to deploy: Giảm xuống

## 🎉 Congratulations!

Nếu bạn đã hoàn thành tất cả các bước trên, chúc mừng! 🎊

Bạn đã có một CI/CD pipeline hoàn chỉnh và professional!

### Next Steps:
1. Tiếp tục develop features mới
2. Tạo PRs và xem CI/CD tự động chạy
3. Monitor và optimize workflows
4. Share với team members
5. Enjoy automated deployments! 🚀

---

**Created by: Antigravity AI Assistant**
**Date: 2025-11-28**

---

## 📞 Need Help?

Nếu gặp vấn đề:
- [ ] Xem logs trong GitHub Actions
- [ ] Đọc troubleshooting trong `CI-CD-GUIDE.md`
- [ ] Check GitHub Actions documentation
- [ ] Ask team members
- [ ] Create issue trong repository
