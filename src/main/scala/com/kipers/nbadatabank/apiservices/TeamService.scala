package com.kipers.nbadatabank.apiservices

import com.kipers.nbadatabank.common.StatsAPI
import rx.lang.scala.Observable

import scala.concurrent.{Future, ExecutionContext}


trait TeamService {
  val statsAPI: StatsAPI
  val endpoint = "commonteamyears"
  val endpointParams = Map("LeagueId" -> "00")

  def getTeamsList(delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[Map[String, Any]] = {
    val stream = statsAPI.get(endpoint, endpointParams, delayInMillis)
    statsAPI.getResultStreamFromRequestStream(stream, "TeamYears")
  }
}
