# MaplePlanner 배포 스크립트 (로컬 PowerShell에서 실행)
# 사용법: .\deploy\deploy.ps1

$KEY = "$HOME\.ssh\mapleplanner"
$SERVER = "ubuntu@168.107.52.15"
$JAR = "build\libs\maple-planner-0.0.1-SNAPSHOT.jar"
$FRONTEND = "C:\Users\white\youngjun\workspace\maplestory-frontend"

Write-Host "=== 1. 백엔드 JAR 빌드 ===" -ForegroundColor Cyan
.\gradlew.bat bootJar
if ($LASTEXITCODE -ne 0) { Write-Host "빌드 실패" -ForegroundColor Red; exit 1 }

Write-Host "=== 2. 프론트엔드 빌드 ===" -ForegroundColor Cyan
Push-Location $FRONTEND
npm run build
if ($LASTEXITCODE -ne 0) { Write-Host "프론트 빌드 실패" -ForegroundColor Red; Pop-Location; exit 1 }
Pop-Location

Write-Host "=== 3. JAR 업로드 및 서비스 재시작 ===" -ForegroundColor Cyan
scp -i $KEY $JAR "${SERVER}:/home/ubuntu/app.jar"
ssh -i $KEY $SERVER "sudo systemctl restart mapleplanner"

Write-Host "=== 4. 프론트엔드 업로드 ===" -ForegroundColor Cyan
scp -i $KEY -r "$FRONTEND\dist\*" "${SERVER}:/var/www/mapleplanner/"
ssh -i $KEY $SERVER "sudo chown -R www-data:www-data /var/www/mapleplanner && sudo chmod -R 755 /var/www/mapleplanner"

Write-Host "=== 완료 — http://168.107.52.15 ===" -ForegroundColor Green
