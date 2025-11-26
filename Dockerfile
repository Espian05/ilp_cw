FROM eclipse-temurin:21

EXPOSE 8080

WORKDIR /app

COPY ./target/ilp_cw2-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]