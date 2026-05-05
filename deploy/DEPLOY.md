# MaplePlanner 배포 가이드

## 서버 정보

| 항목 | 값 |
|------|---|
| 플랫폼 | Oracle Cloud (VM.Standard.E2.1.Micro, Always Free) |
| OS | Ubuntu 22.04 |
| Public IP | 168.107.52.15 |
| 도메인 | mapleplanner.duckdns.org |
| 스펙 | 1 vCPU, 1GB RAM, 스왑 2GB |
| SSH 키 | `%USERPROFILE%\.ssh\mapleplanner` |

SSH 접속:
```powershell
ssh -i "$HOME\.ssh\mapleplanner" ubuntu@168.107.52.15
```

---

## 아키텍처

```
사용자 → https://mapleplanner.duckdns.org
              Nginx :443 (SSL)
              ├── /        → React 정적 파일 (/var/www/mapleplanner)
              └── /api/*   → Spring Boot :8080 (프록시)
```

---

## 1. 서버 초기 설정

### 스왑 설정 (RAM 부족 대비)
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### 패키지 업데이트
```bash
sudo apt update && sudo apt upgrade -y
```

---

## 2. Java 21 설치

```bash
sudo apt install -y openjdk-21-jdk-headless
java -version  # 확인
```

---

## 3. MySQL 8 설치 및 설정

### 설치
```bash
sudo apt install -y mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

### DB 및 유저 생성
```bash
sudo mysql
```

MySQL 프롬프트에서:
```sql
CREATE DATABASE maple_planner CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mapleplanner'@'localhost' IDENTIFIED BY '비밀번호';
GRANT ALL PRIVILEGES ON maple_planner.* TO 'mapleplanner'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

## 4. 환경변수 파일 생성

```bash
sudo mkdir -p /etc/mapleplanner
sudo nano /etc/mapleplanner/env
```

파일 내용:
```
DB_URL=jdbc:mysql://localhost:3306/maple_planner?characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
DB_USERNAME=mapleplanner
DB_PASSWORD=위에서설정한비밀번호
JWT_SECRET=64자이상랜덤문자열
```

JWT_SECRET 생성 (로컬 PowerShell):
```powershell
-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

권한 설정:
```bash
sudo chmod 600 /etc/mapleplanner/env
```

---

## 5. 백엔드 배포

### 로컬에서 JAR 빌드 및 업로드 (로컬 PowerShell)
```powershell
cd C:\Users\white\youngjun\workspace\maplestory-backend

# JAR 빌드
.\gradlew.bat bootJar

# 서버 업로드
scp -i "$HOME\.ssh\mapleplanner" "build\libs\maple-planner-0.0.1-SNAPSHOT.jar" ubuntu@168.107.52.15:/home/ubuntu/app.jar
scp -i "$HOME\.ssh\mapleplanner" "deploy\mapleplanner.service" ubuntu@168.107.52.15:/home/ubuntu/
```

### systemd 서비스 등록 (서버에서)
```bash
sudo mv /home/ubuntu/mapleplanner.service /etc/systemd/system/mapleplanner.service
sudo systemctl daemon-reload
sudo systemctl enable mapleplanner
sudo systemctl start mapleplanner
```

### 로그 확인
```bash
sudo journalctl -u mapleplanner -f
```

---

## 6. 보스 마스터 데이터 임포트

Spring Boot 첫 실행 후 테이블이 생성되면 마스터 데이터 임포트.

로컬에서 파일 전송:
```powershell
scp -i "$HOME\.ssh\mapleplanner" "src\main\resources\data.sql" ubuntu@168.107.52.15:/home/ubuntu/data.sql
```

서버에서 임포트:
```bash
mysql -u mapleplanner -p maple_planner < /home/ubuntu/data.sql
```

---

## 7. Nginx 설치 및 설정

### 설치
```bash
sudo apt install -y nginx
```

### 설정 파일 업로드 (로컬 PowerShell)
```powershell
scp -i "$HOME\.ssh\mapleplanner" "deploy\nginx-mapleplanner.conf" ubuntu@168.107.52.15:/home/ubuntu/
```

### 설정 적용 (서버에서)
```bash
sudo mv /home/ubuntu/nginx-mapleplanner.conf /etc/nginx/sites-available/mapleplanner
sudo ln -s /etc/nginx/sites-available/mapleplanner /etc/nginx/sites-enabled/mapleplanner
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

---

## 8. 방화벽 설정

### OS 방화벽 (iptables)
```bash
sudo apt install -y iptables-persistent
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save
```

### Oracle Cloud Security List
```
Oracle Cloud 콘솔
→ Networking → Virtual Cloud Networks → vcn-20260108-0006
→ Security Lists → Default Security List
→ Add Ingress Rules
```

추가할 규칙:
| Source CIDR | Protocol | Port |
|------------|---------|------|
| 0.0.0.0/0 | TCP | 80 |
| 0.0.0.0/0 | TCP | 443 |

---

## 9. 프론트엔드 배포

### 빌드 및 업로드 (로컬 PowerShell)
```powershell
# 프론트 프로젝트로 이동 후 빌드
cd C:\Users\white\youngjun\workspace\maplestory-frontend
npm run build

# 서버에 배포 폴더 생성
ssh -i "$HOME\.ssh\mapleplanner" ubuntu@168.107.52.15 "sudo mkdir -p /var/www/mapleplanner && sudo chown ubuntu:ubuntu /var/www/mapleplanner"

# 파일 업로드
scp -i "$HOME\.ssh\mapleplanner" -r dist/* ubuntu@168.107.52.15:/var/www/mapleplanner/

# Nginx 읽기 권한 설정
ssh -i "$HOME\.ssh\mapleplanner" ubuntu@168.107.52.15 "sudo chown -R www-data:www-data /var/www/mapleplanner && sudo chmod -R 755 /var/www/mapleplanner"
```

---

## 10. DuckDNS 도메인 설정

1. https://www.duckdns.org 접속 (GitHub 로그인)
2. `mapleplanner` 입력 → `mapleplanner.duckdns.org` 생성
3. IP: `168.107.52.15` 입력 후 **update ip** 클릭

---

## 11. HTTPS 설정 (Let's Encrypt)

### Nginx server_name 수정
```bash
sudo nano /etc/nginx/sites-available/mapleplanner
# server_name을 mapleplanner.duckdns.org 로 변경
sudo systemctl reload nginx
```

### Certbot SSL 인증서 발급
```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d mapleplanner.duckdns.org
```

이메일 입력 → 약관 동의(Y) → 자동 HTTPS 설정 완료

### 자동 갱신 확인 (인증서 90일 유효)
```bash
sudo certbot renew --dry-run
```

---

## 12. GitHub Actions 자동 배포 (백엔드)

`main` 브랜치에 push 시 자동으로 JAR 빌드 → 서버 업로드 → 서비스 재시작.

### GitHub Secrets 등록
`https://github.com/youngjun-kim92/maplestory-backend/settings/secrets/actions`

| Secret | 값 |
|--------|---|
| `SERVER_HOST` | `168.107.52.15` |
| `SERVER_USER` | `ubuntu` |
| `SSH_PRIVATE_KEY` | `Get-Content "$HOME\.ssh\mapleplanner"` 출력값 전체 |

---

## 13. 이후 배포 방법

### 백엔드만 수정한 경우
```bash
git push origin main
# GitHub Actions가 자동으로 빌드 + 배포
```

Actions 결과 확인:
```
https://github.com/youngjun-kim92/maplestory-backend/actions
```

### 프론트엔드도 함께 수정한 경우
```powershell
cd C:\Users\white\youngjun\workspace\maplestory-backend
.\deploy\deploy.ps1
```

자동으로 수행:
1. 백엔드 JAR 빌드
2. 프론트엔드 `npm run build`
3. JAR 업로드 + 서비스 재시작
4. 프론트 파일 업로드 + 권한 설정

---

## 유용한 서버 명령어

```bash
# 백엔드 상태 확인
sudo systemctl status mapleplanner

# 백엔드 로그 실시간
sudo journalctl -u mapleplanner -f

# 백엔드 재시작
sudo systemctl restart mapleplanner

# Nginx 상태
sudo systemctl status nginx

# Nginx 설정 테스트
sudo nginx -t

# Nginx 재시작
sudo systemctl restart nginx

# SSL 인증서 갱신
sudo certbot renew
```
