import sbtassembly.AssemblyPlugin.autoImport._
import sbtassembly.MergeStrategy

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val microservice1 = RootProject(file("../microservice1"))

lazy val root = (project in file("."))
  .dependsOn(microservice1)
  .settings(
    name := "weather-microservice2",

    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.7.3",
      "org.apache.spark" %% "spark-core" % "3.5.1",
      "org.apache.spark" %% "spark-sql" % "3.5.1"
    ),

    dependencyOverrides += "com.github.luben" % "zstd-jni" % "1.5.5-4",

    Compile / mainClass := Some("com.weather.ServerMain"),

    // ✅ FIX JDBC (le seul vraiment important)
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", _*) => MergeStrategy.concat
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }o
  )