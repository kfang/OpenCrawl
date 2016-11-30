package com.github.kfang.opencrawl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.kfang.opencrawl.routing.V1Routes

import scala.concurrent.ExecutionContext

object Main extends App {

  private val config = new Configuration
  implicit val system = ActorSystem("open-crawl", config.CONFIG)
  implicit val materializer = ActorMaterializer()

  implicit val __ctx: ExecutionContext = system.dispatcher

  for {
    database  <- Database.connect(config)
    services  = new Services(system, database)
    routes    = new V1Routes(services).routes
    binding   <- Http().bindAndHandle(routes, "0.0.0.0", 8080)
  } yield {
    system.log.info(s"Successfully bound ${binding.localAddress}")
    binding
  }

}
