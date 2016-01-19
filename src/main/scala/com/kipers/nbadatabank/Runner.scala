package com.kipers.nbadatabank

import com.kipers.nbadatabank.apiservices._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import com.kipers.nbadatabank.common.NbaObservable._

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


  val commonPlayerStream = allSeasons.map(PlayerService.getAllCommonPlayerStream(_, requestDelayInMillis)).reduce(_.merge(_))
  commonPlayerStream.doOnError(println)
  commonPlayerStream.batchInsertResults("commonplayers", insertBatchSize)

  val teamStream = TeamService.getTeamsList()
  teamStream.batchInsertResults("teams", insertBatchSize)

  val teamIdStream = teamStream.map(_("TEAM_ID").asInstanceOf[Int])
  val teamIdWithSeasonStream = allSeasons.map(s => teamIdStream.map(t => (t, s))).reduce(_.merge(_))

  val rosterStreams = teamIdWithSeasonStream.flatMap{ case(teamId, season) => TeamService.getTeamRosterStreams(teamId, season, requestDelayInMillis)}
  val commonRosterStream = TeamService.getTeamRosterStream(rosterStreams, TeamRosterStreamType.CommonTeamRoster)
  val coachRosterStream = TeamService.getTeamRosterStream(rosterStreams, TeamRosterStreamType.Coaches)

  commonRosterStream.batchInsertResults("commonteamroster", insertBatchSize)
  coachRosterStream.batchInsertResults("coachroster", insertBatchSize)

  val gameLogStream = teamIdWithSeasonStream.flatMap(ts =>
    GameLogService.getGameLogs(ts._1, ts._2, delayInMillis = requestDelayInMillis))

  gameLogStream.batchInsertResults("gamelogs", insertBatchSize)

  val gameIdStream = gameLogStream.map(_("Game_ID").asInstanceOf[String]).distinct

  val boxScoreStreams =
    gameIdStream
      .flatMap(gameId => BoxScoreService.getBoxScoreStreams(gameId, delayInMillis = requestDelayInMillis))

  val playerStatsStream = BoxScoreService.getBoxScoreStream(boxScoreStreams, BoxScoreSteamType.PlayerStats)

  playerStatsStream.batchInsertResults("playerstats", insertBatchSize)
}


