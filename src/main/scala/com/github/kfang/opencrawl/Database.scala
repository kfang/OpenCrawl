package com.github.kfang.opencrawl

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{ExecutionContext, Future}

case class Database(config: Configuration, db: DefaultDB) {
  val CrawlJobs: BSONCollection = db[BSONCollection]("crawl_jobs")
}

object Database {
  def connect(config: Configuration)(implicit ctx: ExecutionContext): Future[Database] = {
    val driver: MongoDriver = MongoDriver(config.CONFIG)

    driver.system.log.info(s"Connecting to ${config.MONGO_NODES}")
    val connection: MongoConnection = driver.connection(nodes = config.MONGO_NODES)

    driver.system.log.info(s"Using DB: ${config.MONGO_DATABASE}")
    for {
      db <- connection.database(config.MONGO_DATABASE)
    } yield {
      new Database(config, db)
    }
  }
}
