package com.github.kfang.opencrawl.models.jsoup

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class JsoupJobExtractor(
  extractor: String,
  args: Option[Seq[String]]
)

object JsoupJobExtractor {
  implicit val __jsf: RootJsonFormat[JsoupJobExtractor] = jsonFormat2(JsoupJobExtractor.apply)
}

case class JsoupJobSelector(
  key: String,
  selector: String,
  extractor: JsoupJobExtractor
)

object JsoupJobSelector {
  implicit val __jsf: RootJsonFormat[JsoupJobSelector] = jsonFormat3(JsoupJobSelector.apply)
}

case class JsoupJob(
  url: String,
  selectors: Seq[JsoupJobSelector]
)

object JsoupJob {
  implicit val __jsf: RootJsonFormat[JsoupJob] = jsonFormat2(JsoupJob.apply)
}
