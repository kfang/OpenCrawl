package com.github.kfang.opencrawl.routing

import akka.http.scaladsl.server.Route
import com.github.kfang.opencrawl.Services

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Directives._
import com.github.kfang.opencrawl.models.jsoup.{JsoupJob, JsoupJobResult}
import akka.pattern.ask
import spray.json._

import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.duration._

class JsoupRoutes(implicit srvs: Services, ctx: ExecutionContext) {

  private implicit val __to = akka.util.Timeout(1.minute)

  val routes: Route = pathPrefix("jsoup"){
    (post & pathEnd & entity(as[JsoupJob])){
      (job) => onComplete(srvs.jsoupService.ask(job).mapTo[JsoupJobResult]){
        case Success(res) => complete(res.toJson)
        case Failure(err) => complete(JsoupJobResult(error = Some(err.getMessage)))
      }
    }
  }

}
