FROM eclipse-temurin:11-jdk-jammy AS builder

WORKDIR /build

# Installer SBT
RUN apt-get update && \
    apt-get install -y curl && \
    curl -L "https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.tgz" | tar -xz -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/bin/sbt && \
    rm -rf /var/lib/apt/lists/*

# Copier projet
COPY project/ ./project/
COPY build.sbt ./build.sbt

RUN sbt update

COPY src/ ./src/

RUN sbt clean assembly


FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

# Copier JAR
COPY --from=builder /build/target/scala-*/microservice1-assembly.jar ./ms1.jar

RUN mkdir -p /data/input /data/parquet

ENV INPUT_PATH=/data/input
ENV OUTPUT_PATH=/data/parquet

# Lancement
CMD ["java", "-Xmx2G", "-jar", "ms1.jar"]