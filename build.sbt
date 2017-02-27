name := "open-crawl"

organization := "com.github.kfang"

version := "0.0.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "3.0.1",
  "org.seleniumhq.selenium" % "selenium-support" % "3.0.1",
  "org.seleniumhq.selenium" % "htmlunit-driver" % "2.23.1",
  "org.jsoup" % "jsoup" % "1.10.2",
  "com.beachape" %% "enumeratum" % "1.4.16",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.14",
  "org.reactivemongo" %% "reactivemongo" % "0.12.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
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
    from("java")
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