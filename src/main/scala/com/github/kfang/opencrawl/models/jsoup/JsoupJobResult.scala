package com.github.kfang.opencrawl.models.jsoup

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class JsoupJobSelectorResult(
  key: String,
  value: Option[String],
  error: Option[String],
  numElementsSelected: Int
)

object JsoupJobSelectorResult {
  implicit val __jsf: RootJsonFormat[JsoupJobSelectorResult] = jsonFormat4(JsoupJobSelectorResult.apply)
}


case class JsoupJobResult(
  pageTitle: String = "",
  results: Seq[JsoupJobSelectorResult] = Nil,
  error: Option[String] = None
)

object JsoupJobResult {
  implicit val __jsf: RootJsonFormat[JsoupJobResult] = jsonFormat3(JsoupJobResult.apply)
}

