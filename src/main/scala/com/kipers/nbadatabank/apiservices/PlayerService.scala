package com.kipers.nbadatabank.apiservices

import com.kipers.nbadatabank.common.StatsAPI
import com.kipers.nbadatabank.common.Types.NbaResult
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext

/**
 * Created by ckipers on 1/14/16.
 */
trait PlayerService {
  val statsApi: StatsAPI

  val allCommonPlayerEndpoint = "commonallplayers"

  val defaultCommonPlayerParams = Map("IsOnlyCurrentSeason" -> "1", "LeagueId" -> "00")

  def getAllCommonPlayerStream(season: String, delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[NbaResult] = {
    val stream = statsApi.get(allCommonPlayerEndpoint, defaultCommonPlayerParams + ("Season" -> season), delayInMillis)
    statsApi.getResultStreamFromRequestStream(stream, "CommonAllPlayers")
  }
}
