package com.github.kfang.opencrawl.routing

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.github.kfang.opencrawl.{Database, Services}
import com.github.kfang.opencrawl.models.CrawlJob
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class V1Routes(db: Database, services: Services)(implicit ctx: ExecutionContext) {

  implicit val __srvs = services

  private def queueJob(job: CrawlJob): Future[CrawlJob] = {
    implicit val __to = Timeout(10.seconds)
    services.crawlJobService.ask(job).mapTo[CrawlJob]
  }

  private def getJob(id: UUID): Future[Option[CrawlJob]] = {
    db.CrawlJobs.find(BSONDocument("_id" -> id.toString)).one[CrawlJob]
  }

  private def removeJob(id: UUID): Future[Boolean] = {
    db.CrawlJobs.remove(BSONDocument("_id" -> id.toString)).map(_.n == 1)
  }

  private val crawlJobRoutes: Route = pathPrefix("crawl-jobs"){
    //Create a Job
    (post & pathEnd & entity(as[CrawlJob])){
      (job) => onComplete(queueJob(job)){
        case Success(j) => complete(j)
        case Failure(e) => complete(StatusCodes.BadRequest -> e.getMessage)
      }
    } ~
    //Retrieve a Job
    (get & path(JavaUUID)){
      (uuid) => onComplete(getJob(uuid)){
        case Success(Some(j)) => complete(j)
        case Success(None)    => complete(StatusCodes.NotFound)
        case Failure(e)       => complete(StatusCodes.InternalServerError -> e.getMessage)
      }
    } ~
    //Delete a Job
    (delete & path(JavaUUID)){
      (uuid) => onComplete(removeJob(uuid)){
        case Success(true)  => complete(StatusCodes.NoContent)
        case Success(false) => complete(StatusCodes.NotFound)
        case Failure(e)     => complete(StatusCodes.InternalServerError -> e.getMessage)
      }
    }
  }


  val routes: Route = pathPrefix("v1"){
    crawlJobRoutes ~
    new JsoupRoutes().routes
  }

}
