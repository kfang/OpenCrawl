package com.github.kfang.opencrawl.models

import com.github.kfang.opencrawl.EnumSerializer
import enumeratum._
import reactivemongo.bson.BSONValue

sealed abstract class CrawlStatus(override val entryName: String) extends EnumEntry {
  def bson: BSONValue = CrawlStatus.__bsf.write(this)
}

object CrawlStatus extends Enum[CrawlStatus] with EnumSerializer[CrawlStatus] {

  override val values: Seq[CrawlStatus] = findValues

  case object Pending extends CrawlStatus("pending")
  case object Processing extends CrawlStatus("processing")
  case object Success extends CrawlStatus("success")
  case object Failure extends CrawlStatus("failure")
}
