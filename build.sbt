ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"
val zioVersion = "1.0.16"

lazy val root = (project in file("."))
  .settings(
    name := "sensor-statistics",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-streams"  % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion  % "test",
      "dev.zio" %% "zio-test-sbt" % zioVersion  % "test"
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
