package com.kipers.nbadatabank.apiservices

import com.kipers.nbadatabank.common.StatsAPI
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext}

object GameLogService {
  val Endpoint = "teamgamelog"

  def getGameLogs(teamId: Int, season: String, delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[Map[String, Any]] = {
    val params = Map("TeamId" -> teamId.toString, "SeasonType" -> "Regular Season", "season" -> season)
    val stream = StatsAPI.get(Endpoint, params, delayInMillis)
    StatsAPI.getResultStreamFromRequestStream(stream, "TeamGameLog")
  }

  def getGameLogId(gameLog: Map[String, Any]): String = {
    gameLog("Game_ID").asInstanceOf[String]
  }
}
