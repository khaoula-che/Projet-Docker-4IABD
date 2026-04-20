import sbtassembly.AssemblyPlugin.autoImport._
import sbtassembly.MergeStrategy

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"   // ✅ FIX CRITIQUE

lazy val root = (project in file("."))
  .settings(
    name := "microservice1",

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.5.1",
      "org.apache.spark" %% "spark-sql" % "3.5.1"
    ),

    dependencyOverrides += "com.github.luben" % "zstd-jni" % "1.5.5-4",

    // Assembly
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },

    assembly / assemblyJarName := "microservice1-assembly.jar"
  )