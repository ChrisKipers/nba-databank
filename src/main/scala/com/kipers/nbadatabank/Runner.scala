package com.kipers.nbadatabank

import com.kipers.nbadatabank.common.{StatsAPI, DBService, StatsAPIImpl}
import com.kipers.nbadatabank.apiservices._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import com.kipers.nbadatabank.common.NbaObservable._

object Runner extends App {
  val insertBatchSize = 1000
  val requestDelayInMillis = 50

  val allSeasons = {
    (1990 until 2016).map(year => {
      val endingPartOfSeason = (year  + 1) % 100
      f"$year-$endingPartOfSeason%02d"
    })
  }

  val gameLogService = new GameLogService {
    override val statsAPI: StatsAPI = StatsAPIImpl
  }

  val teamService = new TeamService {
    override val statsAPI: StatsAPI = StatsAPIImpl
  }

  val boxScoreService = new BoxScoreService {
    override val statsApi: StatsAPI = StatsAPIImpl
  }

  val playerService = new PlayerService {
    override val statsApi: StatsAPI = StatsAPIImpl
  }

  val commonPlayerStream = allSeasons.map(playerService.getAllCommonPlayerStream(_, requestDelayInMillis)).reduce(_.merge(_))
  commonPlayerStream.batchInsertResults("commonplayers", insertBatchSize)

  val teamStream = teamService.getTeamsList()
  val teamIdStream = teamStream.map(_("TEAM_ID").asInstanceOf[Int])
  val teamIdWithSeasonStream = allSeasons.map(s => teamIdStream.map(t => (t, s))).reduce(_.merge(_))
  val gameLogStream = teamIdWithSeasonStream.flatMap(ts =>
    gameLogService.getGameLogs(ts._1, ts._2, delayInMillis = requestDelayInMillis))

  gameLogStream.batchInsertResults("gamelogs", insertBatchSize)

  val gameIdStream = gameLogStream.map(_("Game_ID").asInstanceOf[String]).distinct

  val boxScoreStreams =
    gameIdStream
      .flatMap(gameId => boxScoreService.getBoxScoreStreams(gameId, delayInMillis = requestDelayInMillis))

  val playerStatsStream = boxScoreService.getBoxScoreStream(boxScoreStreams, BoxScoreSteamType.PlayerStats)

  playerStatsStream.batchInsertResults("playerstats", insertBatchSize)
}


