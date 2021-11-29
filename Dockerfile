ARG registry_image_gradle=gradle:6.7.0-jdk11
ARG registry_image_jdk=openjdk:11.0.11-jre

FROM ${registry_image_gradle} as base-image

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

FROM ${registry_image_jdk}

WORKDIR /msa

COPY configuration/local/msa.yml msa.yml
COPY --from=build /msa/build/install/msa .

CMD bin/msa server msa.yml
