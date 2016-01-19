package com.kipers.nbadatabank

import java.util.concurrent.CountDownLatch

import com.kipers.nbadatabank.apiservices._
import com.kipers.nbadatabank.common.DBCollections.DBCollections
import com.kipers.nbadatabank.common.{DBCollections, DBService}
import com.kipers.nbadatabank.common.Types.NbaResult
import rx.lang.scala.Observable
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

/**
 * Builds a MongoDB database with NBA statistics pulled from stats.nba.com.
 */
object Main extends App {
  val InsertBatchSize = 1000
  // Request to stats.nba.com must be throttled to prevent IP from being blocked.
  val RequestDelayInMillis = 100

  val StartSeasonYear = 1990
  val EndSeasonYear = 2016

  // Get a list of all the seasons in the format xxxx-xx (e.i. 2015-16)
  val allSeasons = {
    (StartSeasonYear until EndSeasonYear).map(year => {
      val endingPartOfSeason = (year + 1) % 100
      f"$year-$endingPartOfSeason%02d"
    })
  }

  // Latch is used to block main thread until all observables that insert results into the DB are complete
  val numberOfStreamsToBlockOn = DBCollections.values.size
  val latch = new CountDownLatch(numberOfStreamsToBlockOn)

  // Create a stream of all players for every season
  val commonPlayerStream =
    allSeasons
      .map(PlayerService.getAllCommonPlayerStream(_, RequestDelayInMillis))
      .reduce(_.merge(_))

  insertStreamIntoDB(commonPlayerStream, DBCollections.CommonPlayersCollection)

  val teamStream = TeamService.getTeamsList()
  insertStreamIntoDB(teamStream, DBCollections.TeamCollection)

  val teamIdStream = teamStream.map(_ ("TEAM_ID").asInstanceOf[Int])
  // Create a stream of every team with every season
  val teamIdWithSeasonStream =
    allSeasons
      .map(s => teamIdStream.map(t => (t, s)))
      .reduce(_.merge(_))

  // Stream of player and coach rosters for every team and season
  val rosterStreams =
    teamIdWithSeasonStream
      .flatMap { case (teamId, season) => TeamService.getTeamRosterStreams(teamId, season, RequestDelayInMillis) }

  val commonRosterStream = TeamService.getTeamRosterStream(rosterStreams, TeamRosterStreamType.CommonTeamRoster)
  insertStreamIntoDB(commonRosterStream, DBCollections.TeamRosterCollection)

  val coachRosterStream = TeamService.getTeamRosterStream(rosterStreams, TeamRosterStreamType.Coaches)
  insertStreamIntoDB(coachRosterStream, DBCollections.CoachRosterCollection)

  val gameLogStream = teamIdWithSeasonStream.flatMap {
    case (teamId, season) =>
      GameLogService.getGameLogs(teamId, season, delayInMillis = RequestDelayInMillis)
  }

  insertStreamIntoDB(gameLogStream, DBCollections.GameLogsCollection)

  val gameIdStream = gameLogStream.map(_ ("Game_ID").asInstanceOf[String]).distinct

  val boxScoreStreams =
    gameIdStream
      .flatMap(gameId => BoxScoreService.getBoxScoreStreams(gameId, delayInMillis = RequestDelayInMillis))

  val playerStatsStream = BoxScoreService.getBoxScoreStream(boxScoreStreams, BoxScoreSteamType.PlayerStats)

  insertStreamIntoDB(playerStatsStream, DBCollections.PlayerStatsCollection)

  latch.await()

  /**
   * Inserts a stream of NbaResults into a mongodb collection and decrements the latch. Results are inserted in batches
   * for performance.
   * @param stream The stream of results to be inserted into MongoDB.
   * @param dbCollection The collection the results will be inserted into.
   */
  private def insertStreamIntoDB(stream: Observable[NbaResult], dbCollection: DBCollections) {
    val dbCollectionName = dbCollection.toString
    stream
      .tumblingBuffer(InsertBatchSize)
      .doOnCompleted({
        println(s"Done inserted all $dbCollectionName")
        latch.countDown()
      })
      .zipWithIndex
      .subscribe(resultsWithIndex => {
        val (results, indexOfBatch) = resultsWithIndex
        val indexOfResults = indexOfBatch * InsertBatchSize
        println(s"Inserting $dbCollectionName $indexOfResults through ${indexOfResults + results.length}")
        DBService.insertResult(results, dbCollectionName)
      })
  }
}


