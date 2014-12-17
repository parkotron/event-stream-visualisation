name := "event-stream-visualisation"

version := "0.1"

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.4.0"
)

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

