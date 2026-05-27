# Etapa de construcción
FROM maven:3.9-eclipse-temurin-17 AS build
COPY . .
# ¡Esta línea soluciona el error 126!
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]