package com.github.kfang.opencrawl.services

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.github.kfang.opencrawl.models.jsoup.{JsoupJob, JsoupJobResult, JsoupJobSelectorResult}
import org.jsoup.Jsoup

import scala.util.{Failure, Success, Try}

class JsoupService extends Actor with ActorLogging {

  def process(job: JsoupJob, requester: ActorRef): Unit = {
    val document = Jsoup.connect(job.url).get()
    val title = document.title()

    val results = job.selectors.map(sel => {

      val selection = document.select(sel.selector)

      val extractor = sel.extractor.extractor
      val extractorArgs = sel.extractor.args.getOrElse(Nil)
      val key = sel.key

      Try(extractor match {
        case "attr" =>
          extractorArgs match {
            case a1 :: _ => selection.attr(a1)
            case Nil     => selection.attr("")
          }
        case "text" => selection.text()
        case "html" => selection.html()
      }) match {
        case Success(value) =>
          JsoupJobSelectorResult(key = key, value = Some(value), error = None, numElementsSelected = selection.size())
        case Failure(error) =>
          JsoupJobSelectorResult(key = key, value = None, error = Some(error.getMessage), numElementsSelected = selection.size())
      }
    })

    requester ! JsoupJobResult(
      pageTitle = title,
      results = results
    )
  }

  def receive: Receive = {
    case job: JsoupJob => process(job, sender())
  }

}

object JsoupService {
  val NAME = "jsoup-service"

  def start(implicit sys: ActorSystem): ActorRef = {
    sys.actorOf(Props(classOf[JsoupService]), NAME)
  }
}
