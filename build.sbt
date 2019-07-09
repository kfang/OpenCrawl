name := "open-crawl"

organization := "com.github.kfang"

version := "0.0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "3.141.59",
  "org.seleniumhq.selenium" % "selenium-support" % "3.141.59",
  "org.seleniumhq.selenium" % "htmlunit-driver" % "2.35.1",
  "org.jsoup" % "jsoup" % "1.12.1",
  "com.beachape" %% "enumeratum" % "1.5.9",
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.23",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "org.reactivemongo" %% "reactivemongo" % "0.18.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)



enablePlugins(DockerPlugin)

dockerfile in docker := {
  val jarFile = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
  val jarTarget = s"/app/${jarFile.getName}"

  // Make a colon separated classpath with the JAR file
//  val classpathString = classpath.files.map("/app/" + _.getName).mkString(":") + ":" + jarTarget
  val cp = s"""$jarTarget:/app/*"""

  new Dockerfile {
    // Base image
    from("openjdk:12")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    //expose the port
    expose(8080)
    // On launch run Java with the classpath and the main class
    entryPointShell("java", "-cp", cp, "${JAVA_OPTS}", mainclass)
  }
}

imageNames in docker := Seq(
  ImageName(
    namespace = Some("kfang"),
    repository = name.value,
    tag = Some("v" + version.value)
  ),
  ImageName(
    namespace = Some("kfang"),
    repository = name.value,
    tag = Some("latest")
  )
)
