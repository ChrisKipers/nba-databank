package com.kipers.nbadatabank

import java.util.concurrent.CountDownLatch

import com.kipers.nbadatabank.apiservices._
import com.kipers.nbadatabank.common.DBCollections.DBCollections
import com.kipers.nbadatabank.common.{DBCollections, DBService}
import com.kipers.nbadatabank.common.Types.NbaResult
import rx.lang.scala.Observable
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

object Runner extends App {
  val insertBatchSize = 1000
  val requestDelayInMillis = 100

  val startSeasonYear = 1990
  val endSeasonYear = 2016

  val allSeasons = {
    (startSeasonYear until endSeasonYear).map(year => {
      val endingPartOfSeason = (year  + 1) % 100
      f"$year-$endingPartOfSeason%02d"
    })
  }

  val numberOfStreamsToBlockOn = DBCollections.values.size
  val latch = new CountDownLatch(numberOfStreamsToBlockOn)


  val commonPlayerStream = allSeasons.map(PlayerService.getAllCommonPlayerStream(_, requestDelayInMillis)).reduce(_.merge(_))
  insertStreamIntoDB(commonPlayerStream, DBCollections.CommonPlayersCollection)

  val teamStream = TeamService.getTeamsList()
  insertStreamIntoDB(teamStream, DBCollections.TeamCollection)

  val teamIdStream = teamStream.map(_("TEAM_ID").asInstanceOf[Int])
  val teamIdWithSeasonStream = allSeasons.map(s => teamIdStream.map(t => (t, s))).reduce(_.merge(_))

  val rosterStreams = teamIdWithSeasonStream.flatMap{ case(teamId, season) => TeamService.getTeamRosterStreams(teamId, season, requestDelayInMillis)}
  val commonRosterStream = TeamService.getTeamRosterStream(rosterStreams, TeamRosterStreamType.CommonTeamRoster)
  val coachRosterStream = TeamService.getTeamRosterStream(rosterStreams, TeamRosterStreamType.Coaches)

  insertStreamIntoDB(commonRosterStream, DBCollections.TeamRosterCollection)
  insertStreamIntoDB(coachRosterStream, DBCollections.CoachRosterCollection)

  val gameLogStream = teamIdWithSeasonStream.flatMap(ts =>
    GameLogService.getGameLogs(ts._1, ts._2, delayInMillis = requestDelayInMillis))

  insertStreamIntoDB(gameLogStream, DBCollections.GameLogsCollection)

  val gameIdStream = gameLogStream.map(_("Game_ID").asInstanceOf[String]).distinct

  val boxScoreStreams =
    gameIdStream
      .flatMap(gameId => BoxScoreService.getBoxScoreStreams(gameId, delayInMillis = requestDelayInMillis))

  val playerStatsStream = BoxScoreService.getBoxScoreStream(boxScoreStreams, BoxScoreSteamType.PlayerStats)

  insertStreamIntoDB(playerStatsStream, DBCollections.PlayerStatsCollection)

  latch.await()

  def insertStreamIntoDB(stream: Observable[NbaResult], dbCollection: DBCollections) {
    val dbCollectionName = dbCollection.toString
    stream
      .tumblingBuffer(insertBatchSize)
      .doOnCompleted(latch.countDown())
      .doOnCompleted(println(s"Done inserted all $dbCollectionName"))
      .zipWithIndex
      .subscribe(resultsWithIndex => {
        val (results, indexOfBatch) = resultsWithIndex
        val indexOfResults = indexOfBatch * insertBatchSize
        println(s"Inserting $dbCollectionName $indexOfResults through ${indexOfResults + results.length}")
        DBService.insertResult(results, dbCollectionName)
      })
  }
}


