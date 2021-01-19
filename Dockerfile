FROM ghcr.io/alphagov/verify/gradle:gradle-jdk11 as build

WORKDIR /msa
USER root
ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY publish.gradle publish.gradle

RUN gradle installDist

COPY src/ src/
COPY configuration/ configuration/
COPY verify-matching-service-test-tool/ verify-matching-service-test-tool/

RUN gradle installDist

ENTRYPOINT ["gradle", "--no-daemon"]
CMD ["tasks"]

FROM ghcr.io/alphagov/verify/java:openjdk-11

WORKDIR /msa

COPY configuration/local/msa.yml msa.yml
COPY --from=build /msa/build/install/msa .

CMD bin/msa server msa.yml
