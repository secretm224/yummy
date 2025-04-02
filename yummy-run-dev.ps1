# 파일명: build-and-run-dev.ps1

# 경로 설정
$projectDir = Get-Location
$jarRelativePath = "build\libs"
$jarNamePattern = "yummy-0.0.1-SNAPSHOT.jar"

# 🔧 1. Gradle 빌드 수행
Write-Host "Building Gradle..."
& ./gradlew.bat build --quiet

if ($LASTEXITCODE -ne 0) {
    Write-Host "Gradle build failed. Exits script."
    exit 1
}

# 🔍 2. JAR 파일 경로 확인
$jarFile = Get-ChildItem -Path "$projectDir\$jarRelativePath" -Filter $jarNamePattern | Where-Object { $_.Name -notlike "*plain*" } | Select-Object -First 1

if (-not $jarFile) {
    Write-Host "JAR file not found. ($jarRelativePath\$jarNamePattern)"
    exit 1
}

# 🚀 3. JAR 실행
Write-Host "Run in Spring profile (dev): $($jarFile.Name)"
& java -jar "$($jarFile.FullName)" --spring.profiles.active=dev
