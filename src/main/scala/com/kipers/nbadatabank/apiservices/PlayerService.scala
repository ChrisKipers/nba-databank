package com.kipers.nbadatabank.apiservices

import com.kipers.nbadatabank.common.StatsAPI
import com.kipers.nbadatabank.common.Types.NbaResult
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext

/**
 * Created by ckipers on 1/14/16.
 */
object PlayerService {
  val AllCommonPlayerEndpoint = "commonallplayers"

  val DefaultCommonPlayerParams = Map("IsOnlyCurrentSeason" -> "1", "LeagueId" -> "00")

  def getAllCommonPlayerStream(season: String, delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[NbaResult] = {
    val stream = StatsAPI.get(AllCommonPlayerEndpoint, DefaultCommonPlayerParams + ("Season" -> season), delayInMillis)
    StatsAPI.getResultStreamFromRequestStream(stream, "CommonAllPlayers")
  }
}
