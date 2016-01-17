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

trait TeamService {
  val statsAPI: StatsAPI
  val teamListEndpoint = "commonteamyears"
  val teamListParams = Map("LeagueId" -> "00")

  val teamRosterEndpoint = "commonteamroster"

  def getTeamsList(delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[NbaResult] = {
    val stream = statsAPI.get(teamListEndpoint, teamListParams, delayInMillis)
    statsAPI.getResultStreamFromRequestStream(stream, "TeamYears")
  }
  
  def getTeamRosterStreams(teamId: Int, season: String, delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[(String, List[NbaResult])] = {
    statsAPI.get(teamRosterEndpoint, Map("TeamId" -> teamId.toString, "Season" -> season), delayInMillis)
  }

  def getTeamRosterStream(teamRosterStreams: Observable[(String, List[NbaResult])], teamRosterStreamType: TeamRosterStreamType): Observable[NbaResult] = {
    statsAPI.getResultStreamFromRequestStream(teamRosterStreams, teamRosterStreamType.toString)
  }
}
