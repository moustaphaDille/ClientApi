# TP Java Avancé — API REST Gestion de Clients

**Auteur :** Moustapha Bouzou Dille
**Stack :** Spring Boot 4 / Spring Data JPA / MySQL / Redis

## Description

API REST de gestion de clients (CRUD complet), développée avec Spring Boot et Spring Data JPA, connectée à une base MySQL. Le projet va au-delà des exigences de base du TP en intégrant une configuration proche d'un environnement de production : rate limiting distribué avec Bucket4j + Redis, cache Redis, en-têtes de sécurité HTTP (HSTS, CSP), et un déploiement réel sur Railway.

## Lien de démonstration en ligne

L'API est déployée et accessible publiquement en HTTPS :

```
https://clientapi-production-b8bf.up.railway.app/api/clients
```

## Fonctionnalités

### 1. API REST CRUD `/api/clients`

| Méthode | URL | Action |
|---|---|---|
| POST | `/api/clients` | Création d'un nouveau client |
| GET | `/api/clients` | Liste de tous les clients |
| GET | `/api/clients/{id}` | Recherche d'un client par id |
| PUT | `/api/clients/{id}` | Mise à jour d'un client |
| DELETE | `/api/clients/{id}` | Suppression d'un client |

### 2. Limitation du nombre de requêtes (rate limiting)

- **15 requêtes acceptées par heure**, appliquées par adresse IP cliente
- Implémenté avec **Bucket4j** (algorithme du token bucket) couplé à **Redis** comme backend de stockage distribué : le compteur persiste même après un redémarrage de l'application ou en cas de scaling sur plusieurs instances
- Appliqué via un filtre global (`RateLimitFilter`) sur toutes les routes `/api/**`, plutôt que dupliqué dans chaque contrôleur
- Réponse `429 Too Many Requests` une fois la limite atteinte

### 3. Configuration HSTS

Force l'utilisation exclusive d'HTTPS, avec une durée de validité d'un an et extension aux sous-domaines :

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### 4. Configuration CSP

N'autorise que les scripts et contenus provenant du domaine d'origine :

```
Content-Security-Policy: default-src 'self'
```

### 5. Cache Redis

- La méthode `getAllClients()` est mise en cache (`@Cacheable`) avec un TTL de 10 minutes
- Le cache est automatiquement invalidé (`@CacheEvict`) à chaque création, modification ou suppression d'un client, pour éviter de servir des données périmées
- Sérialisation JSON lisible dans Redis, clés en texte clair

## Architecture technique

```
com.moustapha.tp.clients_api
├── config/
│   ├── SecurityConfig.java       → HSTS, CSP, autorisation des routes
│   ├── RedisConfig.java          → Client Redis (Lettuce) pour le rate limiting
│   └── RedisCacheConfig.java     → Configuration du cache Redis (TTL, sérialisation, CacheManager)
├── security/
│   ├── RateLimitService.java     → Logique Bucket4j (token bucket, 15 req/h)
│   └── RateLimitFilter.java      → Filtre appliquant la limite sur /api/**
├── model/
│   └── Client.java               → Entité JPA
├── repository/
│   └── ClientRepository.java     → Spring Data JPA
├── service/
│   └── ClientService.java        → Logique métier + cache
└── web/
    └── ClientController.java     → Contrôleur REST
```

## Lancer le projet en local

### Prérequis

- Java 17+
- Maven
- Docker (pour MySQL et Redis)

### 1. Démarrer MySQL et Redis

```bash
docker compose up -d
```

(fichier `docker-compose.yml` fourni à la racine du projet)

### 2. Générer un certificat HTTPS local de confiance (mkcert)

```bash
mkcert -install
cd src/main/resources
mkcert -pkcs12 -p12-file keystore.p12 localhost 127.0.0.1 ::1
```

### 3. Lancer l'application

```bash
mvn spring-boot:run
```

L'API est alors accessible sur `https://localhost:8443/api/clients`.

## Tester le rate limiting

```bash
for i in {1..17}; do curl -k -s -o /dev/null -w "%{http_code}\n" https://localhost:8443/api/clients; done
```

Résultat attendu : 15× `200`, puis `429` pour les 2 dernières requêtes.

## Déploiement

Le projet est déployé sur **Railway**, avec :
- Une instance MySQL managée
- Une instance Redis managée (avec authentification)
- Un profil Spring dédié (`application-prod.properties`, activé via `SPRING_PROFILES_ACTIVE=prod`) qui adapte la configuration à l'environnement de production (variables d'environnement Railway, désactivation du SSL applicatif car le HTTPS est terminé en amont par le reverse proxy de Railway)
- Un domaine public HTTPS avec certificat automatiquement valide

## Notes techniques

- `spring.jpa.hibernate.ddl-auto=update` est utilisé pour ce TP ; en environnement de production réel, des outils de migration comme Flyway ou Liquibase seraient préférables
- Le rate limiting utilise une stratégie de type *token bucket* plutôt qu'un simple compteur à fenêtre fixe, pour éviter les pics de requêtes à la frontière de deux fenêtres de temps
