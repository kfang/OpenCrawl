package com.github.kfang.opencrawl.services

import akka.event.LoggingAdapter
import com.github.kfang.opencrawl.Database
import com.github.kfang.opencrawl.models.{CrawlJob, CrawlStatus, ElemProp, Finder}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import reactivemongo.api.WriteConcern
import reactivemongo.bson.BSONDocument

import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class CrawlJobRunner(implicit db: Database, ctx: ExecutionContext, log: LoggingAdapter) {

  private def markProcessing(job: CrawlJob): Future[CrawlJob] = {
    db.CrawlJobs.findAndUpdate(
      selector = BSONDocument("_id" -> job._id),
      update = BSONDocument("$set" -> BSONDocument("status" -> CrawlStatus.Processing.bson)),
      fetchNewObject = true,
      upsert = false,
      sort = None,
      fields = None,
      bypassDocumentValidation = false,
      writeConcern = WriteConcern.Acknowledged,
      maxTime = Some(300.seconds),
      collation = None,
      arrayFilters = Nil
    ).map(_.result[CrawlJob].get).andThen({
      case Success(j) => log.debug(s"[PROCESSING] ${j._id.get}")
    })
  }

  private def markProcessed(job: CrawlJob): Future[CrawlJob] = {
    val status = if(job.fields.exists(_.error.isDefined)) CrawlStatus.Failure else CrawlStatus.Success
    db.CrawlJobs.findAndUpdate(
      selector = BSONDocument("_id" -> job._id),
      update = job.copy(status = Some(status), processedOn = Some(System.currentTimeMillis())),
      fetchNewObject = true,
      upsert = false,
      sort = None,
      fields = None,
      bypassDocumentValidation = false,
      writeConcern = WriteConcern.Acknowledged,
      maxTime = Some(300.seconds),
      collation = None,
      arrayFilters = Nil
    ).map(_.result[CrawlJob].get).andThen({
      case Success(j) => log.debug(s"[${status.entryName.toUpperCase}] ${j._id.get}")
    })
  }

  def run(j0: CrawlJob): Future[CrawlJob] = {
    markProcessing(j0).map(job => {
      val driver = new HtmlUnitDriver(false)

      Try(driver.get(job.url))

      val fields = job.fields.map(field => {
        Try {
          val elements = field.finder match {
            case Finder.XPathElem  => List(driver.findElementByXPath(field.finderArgs))
            case Finder.XPathElems => driver.findElementsByXPath(field.finderArgs).asScala.toList

            case Finder.TagNameElem  => List(driver.findElementByTagName(field.finderArgs))
            case Finder.TagNameElems => driver.findElementsByTagName(field.finderArgs).asScala.toList
          }

          field.prop match {
            case ElemProp.Attribute => elements.map(_.getAttribute(field.propArgs).trim)
            case ElemProp.CSS       => elements.map(_.getCssValue(field.propArgs).trim)
            case ElemProp.Text      => elements.map(_.getText.trim)
            case ElemProp.Element   => elements.map(_.toString.trim)
          }
        } match {
          case Success(value) => field.copy(value = Some(value))
          case Failure(error) => field.copy(error = Some(error.toString))
        }
      })

      val pageTitle = Try(driver.getTitle).toOption

      job.copy(fields = fields, pageTitle = pageTitle)
    }).flatMap(j1 => {
      markProcessed(j1)
    })
  }

}
