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
    val connection: MongoConnection = driver.connection(nodes = config.MONGO_NODES)
    for {
      db <- connection.database(config.MONGO_DATABASE)
    } yield {
      new Database(config, db)
    }
  }
}
