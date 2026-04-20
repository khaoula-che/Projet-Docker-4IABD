package com.weather

import com.weather.db.DatabaseManager
import com.weather.predict.PredictionService
import com.weather.parquet.ParquetReader
import com.weather.transform.ImagePreprocessor
import com.weather.PredictionServer  

import java.io.File

object Main {

  def main(args: Array[String]): Unit = {

    println("=== MICROSERVICE 2 ===")

    val parquetPath = "/data/parquet/weather.parquet"
    val testImage = new File("/data/input/rain/1800.jpg")
    val resizedImage = new File("/data/temp/resized.jpg")

    // Créer dossier temp si absent
    resizedImage.getParentFile.mkdirs()

    // Sécurité
    if (!testImage.exists()) {
      println(s"[ERROR] Image introuvable: ${testImage.getAbsolutePath}")
      System.exit(1)
    }

    println("\n[ÉTAPE 1] Lecture du fichier Parquet...")
    ParquetReader.readParquet(parquetPath)

    println(s"\n[ÉTAPE 2] Prétraitement : ${testImage.getName}")
    ImagePreprocessor.resizeImage(testImage, resizedImage)

    println("\n[ÉTAPE 3] Prédiction via API Python...")
    val (label, confidence, allPredictions) =
      PredictionService.predict(resizedImage)

    println(s"Résultat : $label ($confidence%)")

    println("\n[ÉTAPE 4] Sauvegarde en BDD...")

    val predictionsJson =
      if (allPredictions.startsWith("{")) allPredictions
      else allPredictions.toString

    DatabaseManager.savePrediction(
      imageName = testImage.getName,
      predictedLabel = label,
      confidence = confidence,
      allPredictions = predictionsJson,
      modelName = "ResNet50",
      modelAccuracy = 98.0
    )

    DatabaseManager.getAllPredictions()

    if (resizedImage.exists()) resizedImage.delete()

    println("\n[ÉTAPE 5] Lancement serveur HTTP...")
    PredictionServer.start()

    // Bloquer le thread (important pour serveur)
    Thread.currentThread().join()
  }
}