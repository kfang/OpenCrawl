package com.github.kfang.opencrawl.models

import com.github.kfang.opencrawl.EnumSerializer
import enumeratum._

sealed abstract class ElemProp(override val entryName: String) extends EnumEntry

object ElemProp extends Enum[ElemProp] with EnumSerializer[ElemProp]{
  val values = findValues

  case object Attribute extends ElemProp("attribute")
  case object CSS extends ElemProp("css")
  case object Text extends ElemProp("text")
  case object Element extends ElemProp("element")

}
