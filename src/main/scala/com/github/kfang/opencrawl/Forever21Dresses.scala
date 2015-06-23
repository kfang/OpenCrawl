package com.github.kfang.opencrawl

import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import scala.collection.mutable.ListBuffer
import scala.util.Try
import scala.collection.JavaConversions._

class Forever21Dresses {
  val PAGE_1 = "http://www.forever21.com/Product/Category.aspx?br=f21&category=dress&pagesize=30&page=1"


  var pages = 0
  val driver = new FirefoxDriver()
  val waitingDriver = new WebDriverWait(driver, 10)
  driver.get(PAGE_1)

  var nextElem: Option[WebElement] = Some(driver.findElementByLinkText(">"))
  val productUrls = ListBuffer[String]()

  while(nextElem != None){
    nextElem.get.click()

    val urls = driver.findElementsByTagName("a").flatMap(webElem => {
      Try(webElem.getAttribute("href").toLowerCase).toOption
    }).filter(url => {
      url.startsWith("http://") &&
      url.contains("productid") &&
      url.contains("category=dress")
    })
    productUrls ++= urls

    nextElem = Try(waitingDriver.until(ExpectedConditions.presenceOfElementLocated(By.linkText(">")))).toOption
    println(driver.getCurrentUrl)
  }

  productUrls.foreach(println)
  println(productUrls.size)
}
