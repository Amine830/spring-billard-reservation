# 🐳 Docker & Déploiement - Billard‑Book

## 🎯 Objectifs

- Déployer rapidement l'API (et le client) via Docker
- Offrir un mode développement itératif
- Standardiser les scripts
- Préparer l’extension (DB, monitoring, prod)

## ⚡ Quick Start (API seule)

docker compose down
Méthode directe sans docker-compose (recommandée désormais) :

```bash
# Build multi-stage (contexte = dossier server/, plus léger)
docker build -t monuser/billard-book-api:dev server/

# Exécuter localement
docker run --rm -p 8080:8080 monuser/billard-book-api:dev

# Vérifier health (dans un autre terminal)
curl -f http://localhost:8080/actuator/health
```

Script d'automatisation build + test + push : `scripts/docker/build-push.sh`.

## 🏗️ Architecture Conteneurs

```text
Host
 ├─ billard-api :8080  (Spring Boot, JWT, Actuator)
 ├─ (option) nginx :80  (client statique + proxy /api)
 └─ (option) postgres :5432 (persistance future)
```

Réseau : `billard-network` (bridge).  
Volumes : `logs/` (API), (futur) `postgres_data`.

## 📂 Fichiers Clés

| Fichier | Rôle |
|---------|------|
| `docker-compose.yml` | (Optionnel / Legacy) Orchestration multi‑services si besoin futur |
| `server/Dockerfile` | Multi‑stage (build Maven + runtime JRE) |
| `nginx.conf` | Proxy + cache + compression (si client conteneurisé) |
| `scripts/docker/build-push.sh` | Build + test health + push Docker Hub |
| `scripts/docker/stop.sh` | Arrêt compose (optionnel si compose utilisé) |
| `server/.dockerignore` | Réduction contexte build |
| `server/src/main/resources/application-docker.properties` | Profil Spring docker |

## 🧱 Dockerfile (multi-stage)

- Stage build : Eclipse Temurin JDK 21 + Maven cache deps
- Stage runtime : JRE 21 Alpine, user non-root
- Avantages : image compacte, surface d’attaque réduite, cache efficace

## 🧪 Health & Monitoring

Endpoint Actuator documenté dans l’OpenAPI :

```text
GET /actuator/health -> { status: "UP", groups: ["liveness","readiness"] }
```

Healthcheck Compose (30s interval, restart automatique selon policies).

## 🔐 Sécurité de base

- User non-root dans l’image finale
- Réseau privé Docker (pas de ports internes exposés inutilement)
- Possibilité future : activer HTTPS via nginx + certbot (non inclus encore)

## ⚙️ Variables d’environnement (extraits)

Dans `docker-compose.yml`:

```text
SPRING_PROFILES_ACTIVE=docker
JAVA_OPTS=-Xmx512m -Xms256m
JWT_EXPIRATION_MS=3600000
```

Adapter mémoire selon la machine cible.

## 🔄 Flux de travail minimal

| Besoin | Commande |
|--------|----------|
| Build local | `docker build -t monuser/billard-book-api:0.1.0 server/` |
| Test local | `docker run --rm -p 8080:8080 monuser/billard-book-api:0.1.0` |
| Health check | `curl -f http://localhost:8080/actuator/health` |
| Build + push (script) | `./scripts/docker/build-push.sh monuser 0.1.0` |
| Pull & retest | `docker pull monuser/billard-book-api:0.1.0` |

Les scripts legacy (deploy/dev/status/start-daemon/setup) ont été supprimés pour alléger le projet.

## 🛠️ Scripts (interface proposée)

En-tête standard dans chaque script : description + usage.  
Tous supposent Docker opérationnel.

## 🐞 Debug & Logs

```bash
# Logs API
docker compose logs -f billard-api
# Une seule fois (dernier bloc)
docker compose logs --tail=100 billard-api
# Inspect health
curl -s http://localhost:8080/actuator/health | jq
```

## 🧰 Mode développement

Développement rapide conseillé en dehors du conteneur :

```bash
cd server
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

Pour tester l'image : rebuild ponctuel (voir flux ci-dessus). Un mode auto‑reload conteneur n'est plus fourni (trop coûteux pour ce projet).

## 🚀 Passage production (roadmap)

1. Ajouter DB Postgres + migrations
2. CI (GitHub Actions) pour build & push sur tag
3. Observabilité (metrics + logs centralisés)
4. Reverse proxy (TLS, rate limiting)
5. Hardening image (distroless / slim JRE)

## 🧾 Exemple extension DB (future)

```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_DB: billardbook
    POSTGRES_USER: billard_user
    POSTGRES_PASSWORD: change_me
  volumes:
    - postgres_data:/var/lib/postgresql/data
  networks:
    - billard-network
```

## 🧹 Nettoyage

```bash
# Conteneur arrêté automatiquement (run --rm)
# Nettoyage images non utilisées
docker image prune -f
# Nettoyage global (dangereux) :
docker system prune -f
```

## ❓ FAQ Rapide

| Problème | Piste |
|----------|-------|
| Health DOWN | Regarder logs API, vérifier port 8080 collision |
| Rebuild lent | Vérifier `.dockerignore`, cache dépendances Maven (stage séparé) |
| Pas d’auto reload | Utiliser `mvn spring-boot:run` pour dev |
| Permissions logs | Vérifier UID dans image / chmod dossier `logs` |

## 📌 Notes

- Anciennes docs séparées fusionnées ici
- README racine ne garde qu’un aperçu et un lien vers ce fichier