package com.weather

import com.sun.net.httpserver.{HttpServer, HttpExchange}
import java.net.InetSocketAddress
import java.io.{File, FileOutputStream}
import java.nio.file.Files

import com.weather.transform.ImagePreprocessor

object PreprocessingServer {

  def main(args: Array[String]): Unit = {

    val server = HttpServer.create(new InetSocketAddress(8080), 0)

    println("[MS1] Server started on port 8080")

    server.createContext("/resize", (exchange: HttpExchange) => {

      if (exchange.getRequestMethod.equalsIgnoreCase("POST")) {
        try {

          val imageBytes = exchange.getRequestBody.readAllBytes()

          val inputFile = new File("/tmp/input.jpg")
          Files.write(inputFile.toPath, imageBytes)

          val outputFile = new File("/tmp/resized.jpg")

          ImagePreprocessor.resizeImage(inputFile, outputFile)

          val outputBytes = Files.readAllBytes(outputFile.toPath)

          exchange.sendResponseHeaders(200, outputBytes.length)

          val os = exchange.getResponseBody
          os.write(outputBytes)
          os.close()

          inputFile.delete()
          outputFile.delete()

        } catch {
          case e: Exception =>
            e.printStackTrace()

            val error = "resize error"
            exchange.sendResponseHeaders(500, error.length)

            val os = exchange.getResponseBody
            os.write(error.getBytes)
            os.close()
        }

      } else {
        exchange.sendResponseHeaders(405, -1)
      }
    })

    server.createContext("/health", (exchange: HttpExchange) => {

      val response = """{"status":"ok"}"""

      exchange.sendResponseHeaders(200, response.getBytes.length)

      val os = exchange.getResponseBody
      os.write(response.getBytes)
      os.close()
    })

    server.start()
  }
}