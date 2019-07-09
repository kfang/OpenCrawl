package com.github.kfang.opencrawl.models

import com.github.kfang.opencrawl.EnumSerializer
import enumeratum._

sealed abstract class Finder(override val entryName: String) extends EnumEntry

object Finder extends Enum[Finder] with EnumSerializer[Finder]{
  val values = findValues

  case object XPathElem extends Finder("xpath-elem")
  case object XPathElems extends Finder("xpath-elems")

  case object TagNameElem extends Finder("tag-name-elem")
  case object TagNameElems extends Finder("tag-name-elems")
}
