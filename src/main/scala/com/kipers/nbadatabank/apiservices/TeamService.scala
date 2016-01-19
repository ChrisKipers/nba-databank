package com.kipers.nbadatabank.apiservices

import com.kipers.nbadatabank.apiservices.TeamRosterStreamType.TeamRosterStreamType
import com.kipers.nbadatabank.common.StatsAPI
import com.kipers.nbadatabank.common.Types._
import rx.lang.scala.Observable

import scala.concurrent.{Future, ExecutionContext}

object TeamRosterStreamType extends Enumeration {
  type TeamRosterStreamType = Value
  val CommonTeamRoster, Coaches = Value
}

object TeamService {
  val teamListEndpoint = "commonteamyears"
  val teamListParams = Map("LeagueId" -> "00")

  val teamRosterEndpoint = "commonteamroster"

  def getTeamsList(delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[NbaResult] = {
    val stream = StatsAPI.get(teamListEndpoint, teamListParams, delayInMillis)
    StatsAPI.getResultStreamFromRequestStream(stream, "TeamYears")
  }
  
  def getTeamRosterStreams(teamId: Int, season: String, delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[(String, List[NbaResult])] = {
    StatsAPI.get(teamRosterEndpoint, Map("TeamId" -> teamId.toString, "Season" -> season), delayInMillis)
  }

  def getTeamRosterStream(teamRosterStreams: Observable[(String, List[NbaResult])], teamRosterStreamType: TeamRosterStreamType): Observable[NbaResult] = {
    StatsAPI.getResultStreamFromRequestStream(teamRosterStreams, teamRosterStreamType.toString)
  }
}
