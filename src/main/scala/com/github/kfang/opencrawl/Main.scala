package com.github.kfang.opencrawl

import java.io.PrintWriter

import org.openqa.selenium.firefox.FirefoxDriver

object Main {
  val WEB_URL = "http://macys.com"

  def main (args: Array[String]): Unit = {
    println("starting open crawl")

    val writer = new PrintWriter("rendered_page.html")
    val driver = new FirefoxDriver()

    println("created firefox driver")

    driver.get(WEB_URL)
    writer.print(driver.getPageSource)
    driver.close()
  }
}
