package com.github.kfang.opencrawl.services

import com.github.kfang.opencrawl.models.{CrawlJob, ElemProp, Finder}
import org.openqa.selenium.htmlunit.HtmlUnitDriver

import scala.util.{Failure, Success, Try}
import scala.collection.JavaConversions._

class CrawlJobRunner {

  def run(job: CrawlJob): CrawlJob = {
    val driver = new HtmlUnitDriver()

    driver.get(job.url)

    val fields = job.fields.map(field => {
      Try {
        val elements = field.finder match {
          case Finder.XPathElem  => List(driver.findElementByXPath(field.finderArgs))
          case Finder.XPathElems => driver.findElementsByXPath(field.finderArgs).toList
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

    job.copy(fields = fields)
  }

}
