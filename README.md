# Classification d’Images Météo – Architecture Microservices

## Présentation

Ce projet est un système distribué de machine learning permettant de classifier des images météorologiques, basé sur une architecture microservices conteneurisée avec Docker.

Il combine plusieurs technologies :

- Deep Learning (ResNet50 – Transfer Learning)
- Microservices Scala (prétraitement et orchestration)
- FastAPI (API de prédiction)
- PostgreSQL (stockage des résultats)
- Streamlit (interface utilisateur)

---

## Fonctionnalités

- Classification d’images (11 types de météo)
- Prédiction en temps réel via API
- Architecture microservices distribuée (Scala + Python)
- Conteneurisation complète avec Docker
- Historique et statistiques des prédictions
- Précision du modèle d’environ 98 %

---

## Architecture
        ┌──────────────┐
        │  Streamlit   │
        │  Frontend    │
        └──────┬───────┘
               │ HTTP
               ▼
        ┌──────────────┐
        │     MS2      │
        │ Prédiction   │
        │   (Scala)    │
        └──────┬───────┘
               │ HTTP
               ▼
        ┌──────────────┐
        │     MS1      │
        │ Prétraitement│
        │   (Scala)    │
        └──────┬───────┘
               │ HTTP
               ▼
        ┌──────────────┐
        │   FastAPI    │
        │   Modèle ML  │
        └──────┬───────┘
               │
               ▼
        ┌──────────────┐
        │ PostgreSQL   │
        │ Base de      │
        │ données      │
        └──────────────┘


---

## Microservices

### MS1 – Prétraitement (Scala)

- Expose une API HTTP (`/resize`)
- Redimensionne les images
- Utilisé par MS2 avant la prédiction
- Peut aussi fonctionner en mode batch (Parquet)

---

### MS2 – Prédiction (Scala)

- Reçoit les images depuis le frontend
- Appelle MS1 pour le prétraitement
- Appelle l’API FastAPI pour la prédiction
- Sauvegarde les résultats dans PostgreSQL
- Expose une API (`/predict`, `/health`)

---

### API – Machine Learning (FastAPI)

- Charge le modèle ResNet50
- Effectue les prédictions
- Retourne un JSON avec :
  - label
  - confidence
  - probabilités

---

### Frontend – Streamlit

- Upload d’images
- Affichage des résultats
- Visualisation des statistiques

---

## Base de données

PostgreSQL stocke :

- Les prédictions
- Les scores de confiance
- Les probabilités complètes (JSONB)
- Les métadonnées du modèle
- Les timestamps

---

## Modèle

- Architecture : ResNet50
- Technique : Transfer Learning
- Taille d’entrée : 64x64
- Nombre de classes : 11

Classes :
dew, fogsmog, frost, glaze, hail,
lightning, rain, rainbow, rime,
sandstorm, snow

---

## Installation et démarrage

### Prérequis

- Docker
- Docker Compose

---

## Lancer le projet

```bash
docker-compose build 
docker-compose up