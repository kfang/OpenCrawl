package com.github.kfang.opencrawl

import akka.actor.{ActorRef, ActorLogging, Actor}
import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import scala.collection.JavaConversions._
import scala.util.Try

class Forever21Dresses(productUrlPublisher: ActorRef) extends Actor with ActorLogging {
  val PAGE_1 = "http://www.forever21.com/Product/Category.aspx?br=f21&category=dress&pagesize=30&page=1"
  var pages = 0
  val driver = new FirefoxDriver()
  val waitingDriver = new WebDriverWait(driver, 10)
  driver.get(PAGE_1)

  var nextElem: Option[WebElement] = Some(driver.findElementByLinkText(">"))

  while(nextElem.isDefined){
    nextElem.get.click()

    driver.findElementsByTagName("a").flatMap(webElem => {
      Try(webElem.getAttribute("href").toLowerCase).toOption
    }).filter(url => {
      url.startsWith("http://") &&
      url.contains("productid") &&
      url.contains("category=dress")
    }).foreach(url => {
      productUrlPublisher ! url
    })

    nextElem = Try(waitingDriver.until(ExpectedConditions.presenceOfElementLocated(By.linkText(">")))).toOption
    println(driver.getCurrentUrl)
  }

  def receive = {
    case msg => log.debug(s"unknown message received: $msg")
  }
}


