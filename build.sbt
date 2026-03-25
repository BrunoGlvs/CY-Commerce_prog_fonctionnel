name := "cy-commerce"
version := "0.1"
scalaVersion := "2.13.12"

val akkaVersion = "2.8.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.13"
)
