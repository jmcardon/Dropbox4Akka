name := """Dropbox4Akka"""

version := "1.0"

scalaVersion := "2.11.8"
val akkaV = "2.4.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.12.0"
)
