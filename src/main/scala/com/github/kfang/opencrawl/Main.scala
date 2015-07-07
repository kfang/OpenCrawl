package com.github.kfang.opencrawl

import java.io.{PrintWriter, FileOutputStream, File}

import akka.actor.{Props, ActorSystem}
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}

object Main {

  implicit val actorSystem = ActorSystem("open-crawl")
  implicit val materializaer = ActorMaterializer()

  val results = new File("results.json")
  val writer = new PrintWriter(new FileOutputStream(results))

  def main (args: Array[String]): Unit = {

    val source = Source.actorPublisher[String](ProductUrlPublisher.props)
    val sink = Sink.foreach[String](json => { writer.println(json); writer.flush() })

    val publisher = sink.runWith(source)
    actorSystem.actorOf(Props(classOf[Forever21Dresses], publisher))
  }
}
