#!/usr/bin/env bash
# 파일명: build-and-run-dev.sh

# 🛠️ 에러 발생 시 즉시 종료
set -e

# 📂 경로 설정
projectDir="$(pwd)"
jarRelativePath="build/libs"
jarNamePattern="yummy-0.0.1-SNAPSHOT.jar"

# 🔧 1. Gradle 빌드 수행
echo "🔧 Building Gradle..."
./gradlew build --quiet

# 🔍 2. JAR 파일 경로 확인
jarFile=$(find "$projectDir/$jarRelativePath" -maxdepth 1 -type f -name "$jarNamePattern" ! -name "*plain*" | head -n 1)

if [ -z "$jarFile" ]; then
    echo "❌ JAR file not found. ($jarRelativePath/$jarNamePattern)"
    exit 1
fi

# 🚀 3. JAR 실행
echo "🚀 Running JAR in Spring profile 'dev': $(basename "$jarFile")"
exec java -jar "$jarFile" --spring.profiles.active=dev
