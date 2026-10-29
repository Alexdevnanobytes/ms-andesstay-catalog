# Imagen Docker en dos etapas (multi-stage build).
# Etapa 1 "build": usa una imagen con Maven y el JDK 17 para compilar el proyecto.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# Primero se copia el pom.xml y luego el código fuente.
COPY pom.xml .
COPY src ./src
# Compila y empaqueta el .jar (sin correr los tests, que se corren aparte con mvn test).
RUN mvn -q -DskipTests package
# Etapa 2: imagen final liviana, solo con el JRE 17 (sin Maven ni código fuente).
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copia desde la etapa de compilación únicamente el .jar resultante.
COPY --from=build /build/target/ms-andesstay-catalog-1.0.0.jar app.jar
# Documenta el puerto en que escucha la aplicación.
EXPOSE 8082
# Comando que se ejecuta al iniciar el contenedor.
ENTRYPOINT ["java", "-jar", "app.jar"]
