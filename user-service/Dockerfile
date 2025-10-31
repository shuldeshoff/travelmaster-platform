# Multi-stage build для уменьшения размера образа

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Копируем parent pom и зависимости
COPY pom.xml ../pom.xml
COPY common-lib ../common-lib

# Копируем user-service
COPY user-service/pom.xml ./pom.xml
COPY user-service/src ./src

# Сборка
RUN mvn clean package -DskipTests -f ../pom.xml -pl user-service -am

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создание пользователя для безопасности
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# Копирование jar файла
COPY --from=build /app/target/*.jar app.jar

# Смена владельца
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

