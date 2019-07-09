package com.github.kfang.opencrawl

import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConverters._

class Configuration {

  val CONFIG: Config = ConfigFactory.load()

  val CRAWL_CONCURRENCY: Int = CONFIG.getInt("crawl.concurrency")

  val MONGO_NODES: List[String] = CONFIG.getStringList("mongo.nodes").asScala.toList
  val MONGO_DATABASE: String = CONFIG.getString("mongo.database")

}
