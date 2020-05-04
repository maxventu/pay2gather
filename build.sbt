name := "pay2gather"

version := "0.1"

scalaVersion := "2.12.11"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
)

val catsVersion = "2.1.0"
val sttpVersion =  "1.7.2"
val expressionEvaluatorVersion = "0.4.8"
val log4catsVersion = "1.0.1"
val slf4jVersion = "1.7.30"

libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsVersion,
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "async-http-client-backend-cats" % "1.7.2",
  "org.slf4s" %% "slf4s-api" % "1.7.25",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test",
  // https://mvnrepository.com/artifact/net.objecthunter/exp4j
  "net.objecthunter" % "exp4j" % expressionEvaluatorVersion,
  "io.chrisdavenport" %% "log4cats-core"  % log4catsVersion,
  "io.chrisdavenport" %% "log4cats-slf4j" % log4catsVersion,
  "org.slf4j" % "slf4j-api"    % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
)