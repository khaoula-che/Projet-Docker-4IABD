FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Installer dépendances Python
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copier app
COPY app.py .

# Config Streamlit
RUN mkdir -p ~/.streamlit && \
    echo "\
[server]\n\
port = 8501\n\
address = \"0.0.0.0\"\n\
headless = true\n\
enableCORS = false\n\
enableXsrfProtection = false\n\
" > ~/.streamlit/config.toml

# ENV utiles uniquement
ENV PYTHONUNBUFFERED=1
ENV MS2_HOST=ms2
ENV MS2_PORT=9001

# Port
EXPOSE 8501

CMD ["streamlit", "run", "app.py", "--server.port=8501", "--server.address=0.0.0.0"]