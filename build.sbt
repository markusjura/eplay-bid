import PlayKeys._

name := "eplay-bid"

version := "0.2.0"

scalaVersion := "2.10.4"

resolvers += Resolver.url("Markus Jura fork swagger-play2", url("http://markusjura.github.com/swagger-play2"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  ws,
  "org.scalatest" %% "scalatest" % "2.1.0" % "test",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  "commons-io" % "commons-io" % "2.4",
  "se.radley" %% "play-plugins-salat" % "1.4.0",
  "swagger-play2" %% "swagger-play2" % "1.3.5"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalariformSettings
