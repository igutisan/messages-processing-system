# Stage 1: Build (Compilación)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiamos primero solo el pom.xml para aprovechar la caché de Docker
# Si el pom.xml no cambia, Docker no volverá a descargar las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y empaquetamos
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (Imagen final)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Buena práctica de seguridad: No correr la app como root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos el JAR compilado directamente
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto estándar de la app
EXPOSE 8080

# Iniciamos la app con el comando estándar
ENTRYPOINT ["java", "-jar", "app.jar"]
