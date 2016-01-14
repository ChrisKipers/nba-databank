package com.kipers.nbadatabank.common

import com.kipers.nbadatabank.common.Types.NbaResult
import com.mongodb.casbah.{MongoConnection}
import com.mongodb.casbah.Implicits.mapAsDBObject


object DBService {
  val nbaDBName = "nba-data-bank"
  val mongoConn = MongoConnection()
  val mongoDB = mongoConn(nbaDBName)

  def insertResult(results: Seq[NbaResult], collectionName: String) {
    val mongoColl = mongoDB(collectionName)
    val bulkInsertBuilder = mongoColl.initializeOrderedBulkOperation
    results.foreach(obj => bulkInsertBuilder.insert(obj.asDBObject))
    bulkInsertBuilder.execute()
  }
}
