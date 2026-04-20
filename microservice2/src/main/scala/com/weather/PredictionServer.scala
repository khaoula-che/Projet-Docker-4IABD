package com.weather

import com.sun.net.httpserver.{HttpServer, HttpExchange}
import java.net.{InetSocketAddress, HttpURLConnection, URL}
import java.io.{File, DataOutputStream}
import java.nio.file.Files

import com.weather.predict.PredictionService
import com.weather.db.DatabaseManager

object PredictionServer {

  def start(): Unit = {

    val server = HttpServer.create(new InetSocketAddress(9001), 0)
    println("[SERVER] MS2 démarré sur le port 9001")

    server.createContext("/predict", (exchange: HttpExchange) => {

      if (exchange.getRequestMethod.equalsIgnoreCase("POST")) {
        try {

          // recevoir image
          val imageBytes = exchange.getRequestBody.readAllBytes()

          val tempOriginal = new File("/tmp/upload.jpg")
          Files.write(tempOriginal.toPath, imageBytes)

          // appel MS1 pour resize
          val resizedImage = callMS1(tempOriginal)

          // appel API Python
          val (label, confidence, predictions) =
            PredictionService.predict(resizedImage)

          println(s"[API] Résultat : $label ($confidence%)")

          // sauvegarde DB
          DatabaseManager.savePrediction(
            imageName = tempOriginal.getName,
            predictedLabel = label,
            confidence = confidence,
            allPredictions = predictions,
            modelName = "ResNet50",
            modelAccuracy = 98.0
          )

          val json =
            s"""{"label":"$label","confidence":$confidence,"predictions":$predictions}"""

          exchange.getResponseHeaders.set("Content-Type", "application/json")
          exchange.sendResponseHeaders(200, json.getBytes.length)

          val os = exchange.getResponseBody
          os.write(json.getBytes)
          os.close()

          // 🧹 clean
          tempOriginal.delete()
          resizedImage.delete()

        } catch {
          case e: Exception =>
            e.printStackTrace()

            val error = s"""{"error":"${e.getMessage}"}"""
            exchange.getResponseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(500, error.getBytes.length)

            val os = exchange.getResponseBody
            os.write(error.getBytes)
            os.close()
        }

      } else {
        exchange.sendResponseHeaders(405, -1)
      }
    })

    server.createContext("/health", (exchange: HttpExchange) => {
      val response = """{"status":"ok","service":"ms2"}"""

      exchange.getResponseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(200, response.getBytes.length)

      val os = exchange.getResponseBody
      os.write(response.getBytes)
      os.close()
    })

    server.start()
  }
  def callMS1(image: File): File = {

    val url = new URL("http://ms1:8080/resize")
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]

    conn.setRequestMethod("POST")
    conn.setDoOutput(true)
    conn.setConnectTimeout(5000)
    conn.setReadTimeout(5000)

    val os = new DataOutputStream(conn.getOutputStream)
    os.write(Files.readAllBytes(image.toPath))
    os.close()

    val responseCode = conn.getResponseCode

    if (responseCode != 200) {
      throw new RuntimeException(s"MS1 error: code $responseCode")
    }

    val responseBytes = conn.getInputStream.readAllBytes()

    val resized = new File("/tmp/resized.jpg")
    Files.write(resized.toPath, responseBytes)

    resized
  }
}