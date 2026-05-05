# MaplePlanner 배포 스크립트 (로컬 PowerShell에서 실행)
# 사용법: .\deploy\deploy.ps1

$KEY = "$HOME\.ssh\mapleplanner"
$HOST = "ubuntu@168.107.52.15"
$JAR = "build\libs\maple-planner-0.0.1-SNAPSHOT.jar"

Write-Host "=== 1. JAR 빌드 ===" -ForegroundColor Cyan
.\gradlew.bat bootJar
if ($LASTEXITCODE -ne 0) { Write-Host "빌드 실패" -ForegroundColor Red; exit 1 }

Write-Host "=== 2. JAR + 설정 파일 업로드 ===" -ForegroundColor Cyan
scp -i $KEY $JAR "${HOST}:/home/ubuntu/app.jar"
scp -i $KEY "src\main\resources\application-prod.yml" "${HOST}:/home/ubuntu/application-prod.yml"

Write-Host "=== 3. systemd 서비스 파일 업로드 ===" -ForegroundColor Cyan
scp -i $KEY "deploy\mapleplanner.service" "${HOST}:/home/ubuntu/mapleplanner.service"
ssh -i $KEY $HOST "sudo mv /home/ubuntu/mapleplanner.service /etc/systemd/system/mapleplanner.service && sudo systemctl daemon-reload"

Write-Host "=== 4. 서비스 재시작 ===" -ForegroundColor Cyan
ssh -i $KEY $HOST "sudo systemctl restart mapleplanner && sudo systemctl status mapleplanner --no-pager"

Write-Host "=== 완료 ===" -ForegroundColor Green
