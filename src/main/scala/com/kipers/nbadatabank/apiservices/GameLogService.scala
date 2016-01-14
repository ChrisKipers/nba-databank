package com.kipers.nbadatabank.apiservices

import com.kipers.nbadatabank.common.StatsAPI
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext}

trait GameLogService {
  val statsAPI: StatsAPI
  val endpoint = "teamgamelog"

  def getGameLogs(teamId: Int, season: String, delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[Map[String, Any]] = {
    val params = Map("TeamId" -> teamId.toString, "SeasonType" -> "Regular Season", "season" -> season)
    val stream = statsAPI.get(endpoint, params, delayInMillis)
    statsAPI.getResultStreamFromRequestStream(stream, "TeamGameLog")
  }

  def getGameLogId(gameLog: Map[String, Any]): String = {
    gameLog("Game_ID").asInstanceOf[String]
  }
}
