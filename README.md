# Billard-Book

<!-- Badges -->
[![Netlify Status](https://api.netlify.com/api/v1/badges/9b86218f-4efa-4e14-9348-1c4065a03a8b/deploy-status)](https://app.netlify.com/projects/billard-book/deploys)
![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-6DB33F?logo=springboot&logoColor=white)
![Vue 3](https://img.shields.io/badge/Vue.js-3-42b883?logo=vue.js&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white)
![Docker Multi‑Stage](https://img.shields.io/badge/Docker-multi--stage-2496ED?logo=docker&logoColor=white)
![License MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

Plateforme pédagogique de gestion de réservations de tables de billard composée d'une API Java (Spring-like structure) et d'un client web SPA Vue 3.

## 🎯 Objectifs pédagogiques

Ce projet sert de support pour :

- Illustrer une API REST avec hypermédia léger (liens suivis côté client)
- Utilisation de Spring Boot avec une architecture modulaire, respectant les bonnes pratiques (design patterns, séparation des préoccupations, DTO, DAO, filtres ...)
- Mettre en œuvre l'authentification stateless par JWT
- Pratiquer une architecture front moderne (Vue 3 + Pinia + routing SPA)
- Gérer l'état côté client (stores, rafraîchissements ciblés, UX santé backend)
- Introduire des notions de résilience (health check, backoff)
- Préparer l'industrialisation (Docker multi-stage, script build/push, variables d'environnement)
- Ouvrir la voie à des extensions futures (persistance, CI/CD, observabilité)

⚠️ Données de démonstration : le jeu de données est nettoyé périodiquement (purge régulière) afin de garder l'environnement léger. Ne stockez aucune information sensible.

## 🗺️ Contenu du dépôt

| Chemin | Rôle | Doc détaillée |
|--------|------|---------------|
| `server/` | API REST (réservations, users, JWT) | [`server/README-api.md`](server/README-api.md) |
| `client/` | Application web (Vue 3, Pinia, theming, SPA) | [`client/README-client.md`](client/README-client.md) |
| `nginx.conf` | Config exemple reverse proxy statique | - |
| `docs/docker/` | Documentation Docker consolidée | [`docs/docker/README.md`](docs/docker/README.md) |
| `scripts/docker/` | Scripts (build-push, stop, dev, deploy ...) | - |
| `docker-compose.yml` | Orchestration (API + placeholders pour client, DB) | Voir section Docker |

## 🚀 Démarrage ultra-rapide

Prérequis : Docker. (Compose reste optionnel / legacy.)

```bash
# Build image (contexte = server/)
docker build -t billard-book-api:dev server/

# Run local (port 8080)
docker run --rm -p 8080:8080 billard-book-api:dev

# Dans un autre terminal :
curl -f http://localhost:8080/actuator/health
```

Push vers Docker Hub (exemple) :

```bash
./scripts/docker/build-push.sh monuser 0.1.0
# push (:0.1.0 & :latest)
docker pull monuser/billard-book-api:0.1.0     # vérification
docker run --rm -p 8080:8080 monuser/billard-book-api:0.1.0
```

Plus de détails : voir la section "Déploiement vers Docker Hub" dans [docs/docker/README.md](docs/docker/README.md).

Anciennes commandes `deploy.sh`, `dev.sh`, `status.sh`, `start-daemon.sh`, `setup.sh` supprimées (simplification).

Client (mode développement) :

```bash
cd client
npm install
npm run dev
# Ouvrir http://localhost:5173 (port Vite par défaut)
```

## 🧩 Architecture globale

```text
┌──────────────┐        ┌──────────────┐
│   Client SPA │ <----> │    API REST  │
│  (Vue + TS)  │  JWT   │  (Java)      │
└──────────────┘        └──────────────┘
        ▲                       │
        │ Axios + Pinia         │ In-memory (évolutif DB)
        ▼                       ▼
   Thème clair/sombre       Services / DAOs / JWT
```

Points clés :

- Authentification par JWT (stockage côté client localStorage)
- Réservations avec inscription/désinscription, commentaires, statut complet
- Client responsive avec bascule thème sombre/clair
- Suivi des liens (HATEOAS simplifié) côté client pour enrichir les réservations

## 🔐 Sécurité (aperçu)

- Token JWT ajouté aux requêtes sortantes (intercepteur axios)
- Claims mis à jour après opérations (création, inscription, etc.)
- Aucune session serveur (stateless) → expiration configurée (`JWT_EXPIRATION_MS`)

## 🛠️ Variables d'environnement API (exemples)

À définir selon la plateforme :

- `SPRING_PROFILES_ACTIVE=docker`
- `JAVA_OPTS=-Xmx512m -Xms256m`
- `JWT_EXPIRATION_MS=3600000`
- `PORT` (fourni par certain PaaS, fallback 8080)

## 📦 Scripts principaux

Backend : build via Maven (voir [README API](server/README.md)).
Frontend : `npm run dev | build | preview | lint` (voir [README client](client/README.md)).

## 🧪 Tests

- Tests unitaires côté serveur (voir `server/`)
- (À prévoir) tests frontend (Vitest + Testing Library)

## 🐳 Docker (aperçu)

Image unique API construite via multi‑stage : [`server/Dockerfile`](server/Dockerfile).

- Exposition par défaut : `8080` (overridable via `-e PORT=...`)
- Health interne : `/actuator/health`
- Script automation build + test + push : `scripts/docker/build-push.sh`
- Guide complet : `docs/docker/README.md`
- `docker-compose.yml` conservé à titre optionnel (plus requis pour un simple test)

Exemple test rapide :

```bash
docker build -t test-api server/ && docker run --rm -p 8080:8080 test-api
```

## 🔗 Ressources complémentaires

- OpenAPI : [`server/openapi/Billard-Book-api.yaml`](server/openapi/Billard-Book-api.yaml)
- Collection Postman : [`server/postman/`](server/postman/)
- Favicon & PWA manifest : [`client/public/`](client/public/site.webmanifest)
- Captures d'écran : [`docs/screenshots/`](docs/screenshots/)

## 🗺️ Roadmap synthétique

- [ ] Ajout tests frontend
- [ ] WebSocket/SSE pour commentaires temps réel
- [ ] Pagination / filtrage réservations
- [ ] Internationalisation (FR/EN)
- [ ] Persistance BD (PostgreSQL) + migrations
- [ ] Amélioration du système d'authentification et gestion des rôles

## 🆕 Changements récents (Frontend)

- Profil utilisateur : édition du mot de passe (PUT `/users/{login}`) et suppression compte (DELETE) avec dialogue de confirmation.
- Réservations : écran dédié d'édition / suppression (PUT / DELETE `/reservations/{id}`) accessible uniquement au propriétaire (vérification owner côté UI + store).
- Optimisation joueurs : rafraîchissement ciblé via GET `/reservations/{id}/players` (évite rechargement complet après inscription/désinscription).
- Health‑check périodique : bannière rouge si backend DOWN via GET `/actuator/health` (backoff adaptatif pour cold start / services dormants).
- Sécurité UI : suppression de l'affichage du champ mot de passe (ex-`name`) partout; uniquement champ de saisie masqué lors de la modification.
- Liste utilisateurs : chargement détaillé individuel (follow des liens) avec stats créées / participations; fallback robuste si certaines listes manquent.
- Base URL API dynamique : `VITE_API_BASE_URL` pour différencier environnement local (proxy `/api`) et déploiement (URL distante).

## 🖼️ Aperçu visuel

![Accueil clair](docs/screenshots/billard0.png)

Plus de captures et légendes: voir [`docs/screenshots/`](docs/screenshots/).

## 🤝 Contribution

1. Fork + branche feature
2. Lint / build locaux OK
3. PR avec description claire (screens si UI)
4. Respect style TypeScript / conventions Java

---

Pour plus de détails techniques : consultez les README spécifiques dans [`server/`](server/) et [`client/`](client/).

## 📫 Contact

- Maintainer: [@Amine830](https://github.com/Amine830)
- Bugs / idées : créer une *issue* GitHub (labels `bug`, `enhancement`)
- Démo frontend : Netlify (voir badge statut en haut)
- Image API : Docker Hub (voir section Docker)

Pour demandes avancées (persistance, CI/CD, observabilité), ouvrir une issue dédiée.

## 📄 Licence

Projet sous licence MIT (voir fichier [`LICENSE`](LICENSE)).