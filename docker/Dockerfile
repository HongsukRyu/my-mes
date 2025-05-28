# Dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="MES Core Team"
LABEL description="MES Core Application with OPC-UA Integration"

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 패키지 업데이트 및 필요한 도구 설치
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Gradle wrapper와 build.gradle 복사
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle gradle.properties ./

# 의존성 다운로드 (Docker 캐시 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN ./gradlew clean bootJar --no-daemon

# 빌드된 JAR 파일 복사
RUN cp target/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
