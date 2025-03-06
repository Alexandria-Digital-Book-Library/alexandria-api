FROM --platform=$BUILDPLATFORM gradle:7 as builder

WORKDIR /build
COPY . .

ENV GRADLE_OPTS=-Dorg.gradle.daemon=false
RUN ./gradlew build --no-daemon -x test --continue

FROM --platform=$BUILDPLATFORM eclipse-temurin:17

WORKDIR /app
COPY --from=builder /build/build/libs/alexandria-0.0.1-SNAPSHOT.jar alexandria.jar

ENV PORT=8080
EXPOSE ${PORT}

CMD ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "./alexandria.jar"]
