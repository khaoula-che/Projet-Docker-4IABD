# Classification d’Images Météo – Architecture Microservices

## Présentation

Ce projet est un système distribué de machine learning permettant de classifier des images météorologiques, basé sur une architecture microservices avec Docker.

Il combine plusieurs technologies :

- Deep Learning (ResNet50 – Transfer Learning)
- Microservices Scala (traitement et orchestration)
- FastAPI (API de prédiction)
- PostgreSQL (stockage des résultats)
- Streamlit (interface utilisateur)

---

## Fonctionnalités

- Classification d’images (11 types de météo)
- Prédiction en temps réel via API
- Architecture microservices (Scala et Python)
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
            │   FastAPI    │
            │   Modèle ML  │
            └──────┬───────┘
                   │
                   ▼
            ┌──────────────┐
            │ PostgreSQL   │
            │  Base de     │
            │  données     │
            └──────────────┘

    ┌──────────────┐
    │     MS1      │
    │ Prétraitement│
    │   (Scala)    │
    └──────────────┘


---

## Microservices

### MS1 – Prétraitement (Scala + Spark)
- Lecture des images
- Transformation en format Parquet
- Préparation des données

### MS2 – Prédiction (Scala)
- Réception des images depuis le frontend
- Appel à l’API de machine learning
- Enregistrement des résultats dans PostgreSQL

### API – Machine Learning (FastAPI)
- Chargement du modèle ResNet50
- Prédiction
- Retour des résultats au format JSON

### Frontend – Streamlit
- Upload d’images
- Affichage des résultats
- Visualisation des statistiques

---

## Base de données

PostgreSQL stocke :
- Les prédictions
- Les scores de confiance
- Les métadonnées du modèle
- Les dates de prédiction

---

## Modèle

- Architecture : ResNet50
- Technique : Transfer Learning
- Taille d’entrée : 64x64
- Nombre de classes : 11

dew, fogsmog, frost, glaze, hail,
lightning, rain, rainbow, rime,
sandstorm, snow


---

## Installation et démarrage

### Prérequis

- Docker
- Docker Compose

---
