package com.github.kfang.opencrawl.services

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.javadsl.Sink
import akka.stream.scaladsl.Source
import com.github.kfang.opencrawl.Database
import com.github.kfang.opencrawl.models.{CrawlJob, CrawlStatus}
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import scala.util.Success

class CrawlJobService(db: Database) extends Actor with ActorLogging {

  import context.dispatcher
  private implicit val mat = ActorMaterializer()


  private def markProcessing(job: CrawlJob): Future[CrawlJob] = {
    db.CrawlJobs.findAndUpdate(
      selector = BSONDocument("_id" -> job._id),
      update = BSONDocument("$set" -> BSONDocument("status" -> CrawlStatus.Processing.bson)),
      fetchNewObject = true,
      upsert = false
    ).map(_.result[CrawlJob].get)
  }

  private def markProcessed(job: CrawlJob): Future[CrawlJob] = {
    val status = if(job.fields.exists(_.error.isDefined)) CrawlStatus.Failure else CrawlStatus.Success
    db.CrawlJobs.findAndUpdate(
      selector = BSONDocument("_id" -> job._id),
      update = job.copy(status = Some(status), processedOn = Some(System.currentTimeMillis())),
      fetchNewObject = true,
      upsert = false
    ).map(_.result[CrawlJob].get)
  }

  //TODO: Streamline
  private val stream = Source
    .queue[CrawlJob](0, OverflowStrategy.backpressure)
    .mapAsync(10)(markProcessing)
    .mapAsyncUnordered(10)(j => Future(new CrawlJobRunner().run(j)))
    .mapAsync(10)(markProcessed)
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