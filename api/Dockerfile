FROM python:3.11-slim

WORKDIR /app

# Installer dépendance minimale (healthcheck)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Installer dépendances Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copier app
COPY api_model.py .
COPY label_mapping.json .

# Dossier models
RUN mkdir -p /app/models


EXPOSE 8000

HEALTHCHECK CMD curl -f http://localhost:8000/health || exit 1

CMD ["uvicorn", "api_model:app", "--host", "0.0.0.0", "--port", "8000"]