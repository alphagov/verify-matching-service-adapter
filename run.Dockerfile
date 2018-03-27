FROM govukverify/java8

WORKDIR /app

ADD configuration/local/msa.yml msa.yml
ADD build/distributions/verify-matching-service-adapter-*-local.zip msa.zip

RUN unzip msa.zip

CMD verify-matching-service-adapter-*-local/bin/verify-matching-service-adapter server msa.yml
