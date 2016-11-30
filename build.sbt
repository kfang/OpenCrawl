name := "open-crawl"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "3.0.1",
  "org.seleniumhq.selenium" % "selenium-support" % "3.0.1",
  "org.seleniumhq.selenium" % "htmlunit-driver" % "2.23.1",
  "com.beachape" %% "enumeratum" % "1.4.16",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.14",
  "org.reactivemongo" %% "reactivemongo" % "0.12.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)