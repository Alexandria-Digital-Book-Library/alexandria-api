FROM gradle:7-alpine as builder

WORKDIR /build
COPY . .

RUN gradle build -x test

FROM eclipse-temurin:17-alpine

WORKDIR /app
COPY --from=builder /build/build/libs/alexandria-0.0.1-SNAPSHOT.jar alexandria.jar

ENV PORT=8080
EXPOSE ${PORT}

CMD ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "./alexandria.jar"]
