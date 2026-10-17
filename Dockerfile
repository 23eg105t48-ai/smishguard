# ── Stage 1: build ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .
# Fix execute permission (Windows doesn't preserve Linux permissions)
RUN chmod +x mvnw
# Download dependencies first (cached layer)
RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: run ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S smish && adduser -S smish -G smish
USER smish

COPY --from=build /workspace/target/smishguard-*.jar app.jar

# PORT env var lets Render/Railway inject the port
EXPOSE ${PORT:-8080}

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
