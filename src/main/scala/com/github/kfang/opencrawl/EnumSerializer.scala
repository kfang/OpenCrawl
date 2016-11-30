package com.github.kfang.opencrawl

import enumeratum._
import reactivemongo.bson.{BSONHandler, BSONString}
import spray.json.DefaultJsonProtocol._
import spray.json.{JsString, JsValue, RootJsonFormat}

trait EnumSerializer[T <: EnumEntry] {
  this: Enum[T] =>

  implicit val __jsf = new RootJsonFormat[T] {
    override def write(obj: T): JsValue = JsString(obj.entryName)
    override def read(json: JsValue): T = withName(json.convertTo[String])
  }

  implicit val __bsf = new BSONHandler[BSONString, T] {
    override def read(bson: BSONString): T = withName(bson.value)
    override def write(t: T): BSONString = BSONString(t.entryName)
  }

}
