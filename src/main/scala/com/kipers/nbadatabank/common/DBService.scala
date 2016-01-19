package com.kipers.nbadatabank.common

import com.kipers.nbadatabank.common.Types.NbaResult
import com.mongodb.casbah.{MongoConnection}
import com.mongodb.casbah.Implicits.mapAsDBObject

object DBCollections extends Enumeration {
  type DBCollections = Value
  val CommonPlayersCollection = Value("commonplayers")
  val TeamCollection = Value("teams")
  val TeamRosterCollection = Value("commonteamroster")
  val CoachRosterCollection = Value("coachroster")
  val GameLogsCollection = Value("gamelogs")
  val PlayerStatsCollection = Value("playerstats")
}

object DBService {
  val NbaDBName = "nba-databank"
  
  val mongoConn = MongoConnection()
  val mongoDB = mongoConn(NbaDBName)

  def insertResult(results: Seq[NbaResult], collectionName: String) {
    val mongoColl = mongoDB(collectionName)
    val bulkInsertBuilder = mongoColl.initializeOrderedBulkOperation
    results.foreach(obj => bulkInsertBuilder.insert(obj.asDBObject))
    bulkInsertBuilder.execute()
  }
}
