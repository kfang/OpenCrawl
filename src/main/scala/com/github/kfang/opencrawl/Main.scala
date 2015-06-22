package com.github.kfang.opencrawl

import java.io.PrintWriter

import org.openqa.selenium.firefox.FirefoxDriver
import scala.collection.JavaConversions._

object Main {
  val WEB_URL = "http://macys.com"

  def main (args: Array[String]): Unit = {
    println("starting open crawl")

    val writer = new PrintWriter("rendered_page.html")
    val driver = new FirefoxDriver()

    println("created firefox driver")

    driver.get(WEB_URL)
    writer.print(driver.getPageSource)

    val aTags = driver.findElementsByTagName("a")
    aTags.map(webElem => webElem.getAttribute("href")).filter(s => {
      if(s == null) false else true
    }).filter(_.contains("-men-")).foreach(println)

    driver.close()
  }
}
