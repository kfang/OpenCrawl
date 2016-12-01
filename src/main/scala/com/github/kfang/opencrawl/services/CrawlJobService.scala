package com.github.kfang.opencrawl.services

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy, Supervision}
import akka.stream.javadsl.Sink
import akka.stream.scaladsl.Source
import com.github.kfang.opencrawl.Database
import com.github.kfang.opencrawl.models.{CrawlJob, CrawlStatus}
import reactivemongo.bson.BSONDocument
import scala.util.Success

class CrawlJobService(db: Database) extends Actor with ActorLogging {

  import context.dispatcher
  private val settings = ActorMaterializerSettings(context.system)
    .withSupervisionStrategy(Supervision.getResumingDecider)
  private implicit val mat = ActorMaterializer(settings)
  private implicit val __db = db

  private val stream = Source
    .queue[CrawlJob](0, OverflowStrategy.backpressure)
    .mapAsyncUnordered(db.config.CRAWL_CONCURRENCY)(j => new CrawlJobRunner().run(j))
    .to(Sink.ignore)
    .run()

  private  def queueJob(job: CrawlJob, requester: ActorRef) = {
    val jobID = UUID.randomUUID().toString
    db.CrawlJobs.findAndUpdate(
      selector = BSONDocument("_id" -> jobID),
      update = job.copy(
        status = Some(CrawlStatus.Pending),
        createdOn = Some(System.currentTimeMillis())
      ),
      fetchNewObject = true,
      upsert = true
    ).map(_.result[CrawlJob]).andThen({
      case Success(Some(j)) =>
        requester ! j
        stream.offer(j)
    })
  }

  def receive: Receive = {
    case job: CrawlJob => queueJob(job, sender())
  }
}

object CrawlJobService {

  val NAME = "crawl-job-service"

  def start(implicit sys: ActorSystem, db: Database): ActorRef = {
    sys.actorOf(Props(classOf[CrawlJobService], db), NAME)
  }
}
