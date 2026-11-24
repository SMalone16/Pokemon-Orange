FROM python:3.12-slim AS spritebuilder
WORKDIR /spritebuild
COPY scripts/prepare-sprites.py ./scripts/
COPY data ./data
COPY src/main/resources/static/assets/sprites/pokemon ./src/main/resources/static/assets/sprites/pokemon
COPY art-overrides ./art-overrides
RUN python scripts/prepare-sprites.py --output /generated

FROM maven:3.9.13-eclipse-temurin-8-noble AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY config ./config
COPY data ./data
COPY --from=spritebuilder /generated/ ./src/main/resources/static/assets/sprites/pokemon/
# Serve browser libraries locally, avoiding CDN access on student devices.
RUN curl -fsSL --retry 3 https://code.jquery.com/jquery-3.7.1.min.js -o src/main/resources/static/js/lib/jquery.min.js && \
    curl -fsSL --retry 3 https://cdn.jsdelivr.net/npm/js-cookie@2.2.1/src/js.cookie.min.js -o src/main/resources/static/js/lib/js.cookie.min.js
RUN mvn -B package dependency:build-classpath -Dmdep.outputFile=.mvn-classpath && \
    sed -i 's|/root/.m2/repository|/opt/m2|g' .mvn-classpath

FROM eclipse-temurin:8-jre-noble
WORKDIR /app
COPY --from=build /app /app
COPY --from=build /root/.m2/repository /opt/m2
USER 10001:10001
EXPOSE 4567
CMD ["bash", "-c", "exec java -ea -Xmx1024m -cp target/cs32-projects-final-1.0.jar:$(cat .mvn-classpath) cs.brown.edu.aelp.pokemon.Main"]
