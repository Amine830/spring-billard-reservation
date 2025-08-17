## -----------------------------------------------------------
## Dockerfile multi-stage optimisé Billard-Book API (Spring)
## - Stage build: image Maven officielle avec Temurin 21
## - Stage runtime: JRE allégé (Temurin) + support PORT
## - Healthcheck HTTP
## -----------------------------------------------------------

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Pré-télécharger les dépendances pour bénéficier du cache
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copier le code source
COPY src ./src

# Build (skip tests & checkstyle par optimisation)
RUN mvn -q -DskipTests -Dcheckstyle.skip=true clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker \
		JAVA_OPTS="-Xmx512m -Xms256m" \
		PORT=8080

# Ajouter curl/wget pour healthcheck (wget dispo de base busybox -> suffisant)

# Copier le .war généré
COPY --from=build /app/target/*.war /app/app.war

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
	CMD wget -q -O - http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar /app/app.war"]
