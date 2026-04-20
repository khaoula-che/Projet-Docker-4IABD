package com.weather.parquet

import com.weather.domain.WeatherImageRecord
import org.apache.spark.sql.{SparkSession, Row, SaveMode}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

object ParquetExporter {

  def write(outputPath: String, records: Seq[WeatherImageRecord]): Unit = {
    val spark = SparkSession.builder()
      .appName("WeatherRecognition")
      .master("local[*]")
      .config("spark.driver.memory", "8g")
      .getOrCreate()

    val schema = StructType(Seq(
      StructField("imagePath", StringType),
      StructField("fileName", StringType),
      StructField("label", StringType),
      StructField("labelId", IntegerType),
      StructField("note", DoubleType),
      StructField("width", IntegerType),
      StructField("height", IntegerType),
      StructField("channels", IntegerType),
      StructField("features", ArrayType(FloatType))
    ))

    val rows = records.map { r =>
      Row(r.imagePath, r.fileName, r.label, r.labelId, r.note,
          r.width, r.height, r.channels, r.features.toArray)
    }

    val javaRows = java.util.Arrays.asList(rows: _*)
    val df = spark.createDataFrame(javaRows, schema)

    // Écriture en Parquet
    df.write.mode(SaveMode.Overwrite).parquet(outputPath)
    println(s"[SPARK] Parquet écrit : $outputPath")

    // Relecture
    val dfRead = spark.read.parquet(outputPath)

    // Nombre total
    println(s"\n[SPARK] Nombre total : ${dfRead.count()}")

    // Schema
    dfRead.printSchema()

    // Aperçu
    println(s"[SPARK] Aperçu :")
    dfRead.select("fileName", "label", "labelId", "width", "height").show(5)

    // Distribution par label
    println(s"[SPARK] Distribution par label :")
    dfRead.groupBy("label").count().orderBy(desc("count")).show()

    // Filtrer une catégorie
    println(s"[SPARK] Images de pluie :")
    dfRead.filter(col("label") === "rain").select("fileName", "label").show(5)

    // Statistiques
    println(s"[SPARK] Statistiques :")
    dfRead.select("width", "height", "labelId").describe().show()

    // Spark SQL
    dfRead.createOrReplaceTempView("weather_images")
    println(s"[SPARK SQL] Top 3 catégories :")
    spark.sql("""
      SELECT label, COUNT(*) as total
      FROM weather_images
      GROUP BY label
      ORDER BY total DESC
      LIMIT 3
    """).show()

    spark.stop()
  }
}