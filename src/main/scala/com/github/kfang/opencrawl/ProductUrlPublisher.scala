package com.github.kfang.opencrawl

import akka.actor.{Props, ActorLogging}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import scala.annotation.tailrec

class ProductUrlPublisher extends ActorPublisher[String] with ActorLogging {

  var productUrls = Vector[String]()

  @tailrec private def dequeue(): Unit = if(totalDemand > 0){
    if(totalDemand <= Int.MaxValue) {
      val (toSend, toKeep) = productUrls.splitAt(totalDemand.toInt)
      productUrls = toKeep
      toSend foreach onNext
    } else {
      val (toSend, toKeep) = productUrls.splitAt(Int.MaxValue)
      productUrls = toKeep
      toSend foreach onNext
      dequeue()
    }
  }

  private def enqueue(url: String): Unit = {
    if(productUrls.isEmpty && totalDemand > 0){
      onNext(url)
    } else {
      productUrls :+= url
      dequeue()
    }
  }

  def receive = {
    case url: String => enqueue(url)
    case Request(_) => dequeue()
    case Cancel => context.stop(self)
  }
}

object ProductUrlPublisher {
  def props: Props = Props(classOf[ProductUrlPublisher])
}
