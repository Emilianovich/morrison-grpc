FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 morrison
COPY --from=build /workspace/target/morrison-grpc-2.0.0.jar app.jar
USER morrison
EXPOSE 50051
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
