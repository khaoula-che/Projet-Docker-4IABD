package com.weather.extractor

import scala.io.Source
import java.io.File

object MetadataReader {

  def readNotes(csvPath: String): Map[String, Double] = {
    val file = new File(csvPath)

    if (!file.exists()) {
      println(s"[INFO] Fichier de notes introuvable : $csvPath. Les notes par défaut seront à 0.0")
      return Map.empty
    }

    val source = Source.fromFile(file, "UTF-8")
    try {
      val lines = source.getLines().drop(1)

      lines.flatMap { line =>
        val cols = line.split(",", -1).map(_.trim)
        if (cols.length >= 2) {
          val fileName = cols(0)
          val noteOpt = cols(1).toDoubleOption
          noteOpt.map(note => fileName -> note)
        } else {
          None
        }
      }.toMap
    } finally {
      source.close()
    }
  }
}