FROM eclipse-temurin:21-jdk

COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

COPY src src
COPY config config

RUN ./gradlew --no-daemon dependencies

# build-stage = Gradle Build
RUN --mount=type=secret,id=sentry_auth_token,env=SENTRY_AUTH_TOKEN \
    ./gradlew --no-daemon build

ENV JAVA_OPTS="-Xmx512M -Xms512M"

EXPOSE 7070

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]