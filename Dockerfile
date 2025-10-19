FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/MRP_Khouja-1.0-SNAPSHOT.jar app.jar
COPY --from=build /app/target/lib ./lib
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]