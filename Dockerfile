
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/data ./data

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Xmx384m -Dserver.port=${PORT:-10000} -jar app.jar"]
