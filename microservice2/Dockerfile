FROM eclipse-temurin:11-jdk-jammy AS builder

WORKDIR /build

# Installer SBT
RUN apt-get update && \
    apt-get install -y curl && \
    curl -L "https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.tgz" | tar -xz -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/bin/sbt && \
    rm -rf /var/lib/apt/lists/*

# Copier projets
COPY microservice1 /build/microservice1
COPY microservice2 /build/microservice2

WORKDIR /build/microservice2

# Télécharger dépendances
RUN sbt update

# Build
RUN sbt clean assembly


# ========= RUNTIME =========
FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

# Installer outils utiles uniquement
RUN apt-get update && \
    apt-get install -y curl postgresql-client && \
    rm -rf /var/lib/apt/lists/*

# Copier JAR
COPY --from=builder /build/microservice2/target/scala-*/*-assembly-*.jar ./ms2.jar

# Dossiers
RUN mkdir -p /data/parquet /data/predictions /app/models

# ENV utiles
ENV MODEL_PATH=/app/models
ENV PARQUET_PATH=/data/parquet
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=weather_db
ENV DB_USER=weather_user
ENV DB_PASSWORD=weather_pass
ENV API_HOST=api
ENV API_PORT=8000

# Port
EXPOSE 9001

# Script simple
CMD sh -c "\
echo 'Waiting for PostgreSQL...' && \
until pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER; do sleep 2; done && \
echo 'Postgres OK' && \
echo 'Waiting API...' && \
until curl -s http://$API_HOST:$API_PORT/health > /dev/null; do sleep 2; done && \
echo 'API OK' && \
java -Xmx2G -jar /app/ms2.jar"