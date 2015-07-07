name := "open-crawl"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "2.46.0",
  "org.scala-lang" % "scala-xml" % "2.11.0-M4",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-RC4"
)