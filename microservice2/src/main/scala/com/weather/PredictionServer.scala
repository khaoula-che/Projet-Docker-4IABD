package com.weather

import com.sun.net.httpserver.{HttpServer, HttpExchange}
import java.net.InetSocketAddress
import java.io.{File, FileOutputStream}

import com.weather.predict.PredictionService
import com.weather.transform.ImagePreprocessor
import com.weather.db.DatabaseManager

object PredictionServer {

  def start(): Unit = {

    val server = HttpServer.create(new InetSocketAddress(9001), 0)
    println("[SERVER] MS2 démarré sur le port 9001")

    server.createContext("/predict", (exchange: HttpExchange) => {

      if (exchange.getRequestMethod.equalsIgnoreCase("POST")) {
        try {

          val inputStream = exchange.getRequestBody
          val imageBytes = inputStream.readAllBytes()
          inputStream.close()

          val tempOriginal = new File("/tmp/upload.jpg")
          val fos = new FileOutputStream(tempOriginal)
          fos.write(imageBytes)
          fos.close()

          val resizedImage = new File("/tmp/resized.jpg")
          ImagePreprocessor.resizeImage(tempOriginal, resizedImage)

          val (label, confidence, predictions) =
            PredictionService.predict(resizedImage)

          println(s"[API] Résultat : $label ($confidence%)")

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

          // 🧹 Clean
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

      val json = """{"status":"ok","service":"ms2"}"""

      exchange.getResponseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(200, json.getBytes.length)

      val os = exchange.getResponseBody
      os.write(json.getBytes)
      os.close()
    })

    server.setExecutor(null)
    server.start()
  }
}