package com.github.kfang.opencrawl.models

import reactivemongo.bson.Macros
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class CrawlJobField (
  key: String,
  value: Option[Seq[String]] = None,
  error: Option[String] = None,

  finder: Finder,
  finderArgs: String,
  prop: ElemProp,
  propArgs: String
)

object CrawlJobField {
  implicit val __jsf: RootJsonFormat[CrawlJobField] = jsonFormat7(CrawlJobField.apply)
  implicit val __bsf = Macros.handler[CrawlJobField]
}


case class CrawlJob(
  _id: Option[String],
  status: Option[CrawlStatus],
  createdOn: Option[Long],
  processedOn: Option[Long],
  url: String,
  fields: Seq[CrawlJobField],
  pageTitle: Option[String]
)

object CrawlJob {
  implicit val __jsf: RootJsonFormat[CrawlJob] = jsonFormat7(CrawlJob.apply)
  implicit val __bsf = Macros.handler[CrawlJob]
}
