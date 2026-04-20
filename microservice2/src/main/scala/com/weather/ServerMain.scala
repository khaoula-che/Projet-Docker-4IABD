package com.weather

object ServerMain {

  def main(args: Array[String]): Unit = {

    println("=== MS2 SERVER START ===")

    PredictionServer.start()

    println("MS2 READY ON PORT 9001")

    Thread.currentThread().join()
  }
}