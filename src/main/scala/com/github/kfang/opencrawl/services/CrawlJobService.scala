package com.github.kfang.opencrawl.services

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy, Supervision}
import akka.stream.scaladsl.{Sink, Source}
import com.github.kfang.opencrawl.Database
import com.github.kfang.opencrawl.models.{CrawlJob, CrawlStatus}
import reactivemongo.api.WriteConcern
import reactivemongo.bson.BSONDocument

import scala.util.Success
import scala.concurrent.duration._

class CrawlJobService(db: Database) extends Actor with ActorLogging {

  import context.dispatcher
  private val settings = ActorMaterializerSettings(context.system)
    .withSupervisionStrategy(Supervision.getResumingDecider)
  private implicit val mat = ActorMaterializer(settings)
  private implicit val __db = db
  private implicit val __log = log

  private val stream = Source
    .queue[CrawlJob](0, OverflowStrategy.backpressure)
    .mapAsyncUnordered(db.config.CRAWL_CONCURRENCY)(j => new CrawlJobRunner().run(j))
    .to(Sink.ignore)
    .run()

  private def queueJob(job: CrawlJob, requester: ActorRef) = {
    val jobID = UUID.randomUUID().toString
    db.CrawlJobs.findAndUpdate(
      selector = BSONDocument("_id" -> jobID),
      update = job.copy(
        status = Some(CrawlStatus.Pending),
        createdOn = Some(System.currentTimeMillis())
      ),
      fetchNewObject = true,
      upsert = true,
      sort = None,
      fields = None,
      bypassDocumentValidation = false,
      writeConcern = WriteConcern.Acknowledged,
      maxTime = Some(300.seconds),
      collation = None,
      arrayFilters = Nil
    ).map(_.result[CrawlJob]).andThen({
      case Success(Some(j)) =>
        requester ! j
        stream.offer(j)
    })
  }

  override def preStart(): Unit = {
    db.CrawlJobs.find(BSONDocument("status" -> CrawlStatus.Pending.bson), None).cursor[CrawlJob]().fold(0)({
      (r, job) =>
        stream.offer(job)
        r + 1
    }).andThen({
      case Success(i) => log.info(s"Requeued $i jobs")
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
