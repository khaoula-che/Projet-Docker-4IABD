import streamlit as st
import requests
from PIL import Image
import psycopg2
import pandas as pd

API_URL = "http://ms2:9001/predict"

DB_CONFIG = {
    "host": "postgres",
    "database": "weather_db",
    "user": "weather_user",
    "password": "weather_pass"
}

st.set_page_config(page_title="Weather Recognition", page_icon="🌤", layout="wide")

# Style
st.markdown("""
<style>
.main-title {font-size:2.5rem;font-weight:700;text-align:center;}
.sub-title {text-align:center;color:gray;margin-bottom:2rem;}
.result-label {font-size:2rem;font-weight:700;text-align:center;}
</style>
""", unsafe_allow_html=True)

st.markdown('<p class="main-title">Weather Image Recognition</p>', unsafe_allow_html=True)
st.markdown('<p class="sub-title">ResNet50 - Transfer Learning</p>', unsafe_allow_html=True)

# Sidebar
with st.sidebar:
    st.header("Infos")
    st.write("ResNet50 | 11 classes météo")

# Tabs
tab1, tab2, tab3 = st.tabs(["Prediction", "Historique", "Stats"])

with tab1:
    uploaded_file = st.file_uploader("Choisis une image", type=["jpg", "jpeg", "png"])

    if uploaded_file is not None:
        col1, col2 = st.columns(2)

        with col1:
            image = Image.open(uploaded_file)
            st.image(image)

        with col2:
            st.subheader("Résultat")

            with st.spinner("Analyse..."):
                uploaded_file.seek(0)

                response = requests.post(
                    API_URL,
                    data=uploaded_file.getvalue(),
                    headers={"Content-Type": "application/octet-stream"}
                )

            st.write("Status:", response.status_code)

            if response.status_code == 200:
                result = response.json()

                label = result["label"]
                confidence = result["confidence"]
                predictions = result["predictions"]

                st.markdown(f'<p class="result-label">{label.upper()}</p>', unsafe_allow_html=True)
                st.metric("Confiance", f"{confidence}%")

                st.divider()

                for k, v in sorted(predictions.items(), key=lambda x: x[1], reverse=True)[:5]:
                    st.progress(min(v / 100, 1.0), text=f"{k}: {v}%")

                st.success("Résultat sauvegardé en BDD")

            else:
                st.error("Erreur API")
                st.write(response.text)

# =========================
# HISTORIQUE
# =========================
with tab2:
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()

        cur.execute("""
        SELECT image_name, predicted_label, confidence, created_at
        FROM predictions
        ORDER BY created_at DESC
        LIMIT 20
        """)

        rows = cur.fetchall()
        conn.close()

        if rows:
            df = pd.DataFrame(rows, columns=["Image", "Label", "Confidence", "Date"])
            st.dataframe(df)
        else:
            st.write("Aucune donnée")

    except Exception as e:
        st.error(str(e))

# =========================
# STATS
# =========================
with tab3:
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()

        cur.execute("SELECT COUNT(*) FROM predictions")
        total = cur.fetchone()[0]

        cur.execute("""
        SELECT predicted_label, COUNT(*)
        FROM predictions
        GROUP BY predicted_label
        """)

        stats = cur.fetchall()
        conn.close()

        st.metric("Total prédictions", total)

        if stats:
            df = pd.DataFrame(stats, columns=["Label", "Count"])
            st.bar_chart(df.set_index("Label"))

    except Exception as e:
        st.error(str(e))