package com.github.kfang.opencrawl.routing

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.kfang.opencrawl.models.{CrawlJob, CrawlStatus}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.github.kfang.opencrawl.Services
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class V1Routes(services: Services) {

  private def queueJob(job: CrawlJob): Future[CrawlJob] = {
    implicit val __to = Timeout(10.seconds)
    services.crawlJobService.ask(job).mapTo[CrawlJob]
  }

  private val crawlJobRoutes: Route = pathPrefix("crawl-jobs"){
    (post & pathEnd & entity(as[CrawlJob])){
      (job) => {
        onComplete(queueJob(job)){
          case Success(j) => complete(j)
          case Failure(e) => complete(e.getMessage)
        }
      }
    }
  }


  val routes: Route = pathPrefix("v1"){
    crawlJobRoutes
  }

}
