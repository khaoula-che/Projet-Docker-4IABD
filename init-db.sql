
\c weather_db;

-- ===== Tables métadonnées =====
CREATE TABLE IF NOT EXISTS weather_classes (
    id SERIAL PRIMARY KEY,
    class_id INTEGER UNIQUE NOT NULL,
    class_name VARCHAR(50) UNIQUE NOT NULL,
    image_count INTEGER DEFAULT 0
);

-- Insérer les 11 classes météo
INSERT INTO weather_classes (class_id, class_name) VALUES
    (0, 'dew'),
    (1, 'fogsmog'),
    (2, 'frost'),
    (3, 'glaze'),
    (4, 'hail'),
    (5, 'lightning'),
    (6, 'rain'),
    (7, 'rainbow'),
    (8, 'rime'),
    (9, 'sandstorm'),
    (10, 'snow')
ON CONFLICT (class_id) DO NOTHING;

-- ===== Table des prédictions =====
CREATE TABLE IF NOT EXISTS predictions (
    id SERIAL PRIMARY KEY,
    image_name VARCHAR(255) NOT NULL,
    predicted_label VARCHAR(50) NOT NULL,
    confidence DECIMAL(6, 2) NOT NULL CHECK (confidence >= 0 AND confidence <= 100),
    all_predictions JSONB,
    model_name VARCHAR(100) DEFAULT 'ResNet50',
    model_accuracy DECIMAL(5, 2) DEFAULT 98.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index
CREATE INDEX IF NOT EXISTS idx_predictions_created_at ON predictions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_predictions_label ON predictions(predicted_label);
CREATE INDEX IF NOT EXISTS idx_predictions_image_name ON predictions(image_name);

CREATE OR REPLACE VIEW prediction_statistics AS
SELECT 
    COUNT(*) as total_predictions,
    COUNT(DISTINCT predicted_label) as unique_labels,
    AVG(confidence) as average_confidence,
    MIN(created_at) as first_prediction,
    MAX(created_at) as last_prediction
FROM predictions;

CREATE OR REPLACE VIEW predictions_by_category AS
SELECT 
    predicted_label,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / NULLIF(SUM(COUNT(*)) OVER(), 0), 2) as percentage,
    ROUND(AVG(confidence), 2) as avg_confidence,
    ROUND(MIN(confidence), 2) as min_confidence,
    ROUND(MAX(confidence), 2) as max_confidence,
    MIN(created_at) as first_prediction,
    MAX(created_at) as last_prediction
FROM predictions
GROUP BY predicted_label
ORDER BY count DESC;

-- Permissions
GRANT ALL PRIVILEGES ON DATABASE weather_db TO weather_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO weather_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO weather_user;

DO $$
BEGIN
    RAISE NOTICE 'Database weather_db initialized successfully!';
END $$;