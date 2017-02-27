package com.github.kfang.opencrawl

import akka.actor.{ActorRef, ActorSystem}
import com.github.kfang.opencrawl.services.{CrawlJobService, JsoupService}

class Services(system: ActorSystem, db: Database) {
  private implicit val __sys = system
  private implicit val __db = db

  val crawlJobService: ActorRef = CrawlJobService.start
  val jsoupService: ActorRef = JsoupService.start

}
