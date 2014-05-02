name := "eplay-bid"

version := "1.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.0" % "test",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  "commons-io" % "commons-io" % "2.4",
  "se.radley" %% "play-plugins-salat" % "1.4.0",
  "com.wordnik" %% "swagger-play2" % "1.3.5"
)    

play.Project.playScalaSettings

scalariformSettings
