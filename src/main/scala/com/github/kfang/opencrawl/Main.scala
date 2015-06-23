package com.github.kfang.opencrawl

object Main {
  val WEB_URL = "http://macys.com"

  def main (args: Array[String]): Unit = {
    println("starting open crawl")
    val forever21Crawl = new Forever21Dresses()
  }
}
