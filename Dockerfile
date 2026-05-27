# Etapa de construcción
# Usamos una imagen de Maven basada en Eclipse Temurin
FROM maven:3.9-eclipse-temurin-17 AS build
COPY . .
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
# Usamos JRE (Java Runtime Environment) de Eclipse Temurin para que sea más ligero
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]