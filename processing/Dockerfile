# ==========================================
# Etapa 1: Construcción (Builder)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copiar el wrapper de maven y el pom.xml para aprovechar la caché de Docker
# Se copian primero estos archivos para descargar las dependencias y cachearlas
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Descargar las dependencias (modo offline para aprovechar la caché)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir el empaquetado final omitiendo los tests
RUN ./mvnw clean package -DskipTests

# ==========================================
# Etapa 2: Extracción de capas (Layer Extractor)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /app

# Copiar el JAR generado de la etapa de construcción
COPY --from=builder /build/target/*.jar app.jar

# Extraer las capas del JAR usando la herramienta integrada de Spring Boot
# Esto permite una mejor caché de las capas si solo cambian nuestras clases
RUN java -Djarmode=layertools -jar app.jar extract

# ==========================================
# Etapa 3: Imagen Final (Runtime)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Buena práctica: No ejecutar la aplicación como usuario root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Exponer el puerto de la aplicación (usualmente 8080)
EXPOSE 8080

# Copiar las capas extraídas (el orden es importante para optimizar la caché)
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

# Definir variables de entorno (Opcional, para configuración de memoria, perfiles, etc.)
ENV SPRING_PROFILES_ACTIVE=prod
# Añadir flags útiles de la JVM para entornos contenerizados (ya el UseContainerSupport viene por defecto en Java 21)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Ejecutar usando el JarLauncher de Spring Boot para aprovechar el Classpath dividido
# Nota: En Spring Boot 3.2+ la clase cambió a org.springframework.boot.loader.launch.JarLauncher
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
