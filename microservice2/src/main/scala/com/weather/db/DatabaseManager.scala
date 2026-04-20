package com.weather.db

import java.sql.{Connection, DriverManager}
import scala.util.control.NonFatal

object DatabaseManager {

  val url = "jdbc:postgresql://postgres:5432/weather_db"
  val user = "weather_user"
  val password = "weather_pass"

  // ============================
  // Connexion DB
  // ============================
  def getConnection(): Connection = {
    DriverManager.getConnection(url, user, password)
  }

  // ============================
  // Sauvegarde prédiction
  // ============================
  def savePrediction(
    imageName: String,
    predictedLabel: String,
    confidence: Double,
    allPredictions: String,
    modelName: String,
    modelAccuracy: Double
  ): Unit = {

    val conn = getConnection()

    try {
      // 🔒 Sécuriser confidence (0 → 100)
      val safeConfidence = math.max(0.0, math.min(confidence, 100.0))

      // 🔍 Debug JSON (important si bug)
      println(s"[DEBUG JSON] $allPredictions")

      val sql =
        """
        INSERT INTO predictions (
          image_name,
          predicted_label,
          confidence,
          all_predictions,
          model_name,
          model_accuracy
        )
        VALUES (?, ?, ?, ?::jsonb, ?, ?)
        """

      val stmt = conn.prepareStatement(sql)

      stmt.setString(1, imageName)
      stmt.setString(2, predictedLabel)
      stmt.setDouble(3, safeConfidence)
      stmt.setString(4, allPredictions) // ⚠️ doit être JSON valide
      stmt.setString(5, modelName)
      stmt.setDouble(6, modelAccuracy)

      stmt.executeUpdate()
      stmt.close()

      println(s"[DB] OK: $imageName → $predictedLabel ($safeConfidence%)")

    } catch {
      case NonFatal(e) =>
        println(s"[DB ERROR] ${e.getMessage}")
        throw e
    } finally {
      conn.close()
    }
  }

  // ============================
  // Lire historique
  // ============================
  def getAllPredictions(): Unit = {

    val conn = getConnection()

    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery(
        "SELECT image_name, predicted_label, confidence, created_at FROM predictions ORDER BY created_at DESC"
      )

      println("\n=== HISTORIQUE DES PRÉDICTIONS ===")

      while (rs.next()) {
        println(
          s"${rs.getTimestamp("created_at")} | ${rs.getString("image_name")} | ${rs.getString("predicted_label")} | ${rs.getDouble("confidence")}%"
        )
      }

      rs.close()
      stmt.close()

    } catch {
      case NonFatal(e) =>
        println(s"[DB ERROR] ${e.getMessage}")
    } finally {
      conn.close()
    }
  }
}