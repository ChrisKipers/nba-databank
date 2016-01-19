package com.kipers.nbadatabank.common

import com.kipers.nbadatabank.common.Types.NbaResult
import dispatch._
import net.liftweb.json._
import rx.lang.scala.{Subscription, Observable}

import scala.concurrent.ExecutionContext

object StatsAPI {
  val NbaBaseUrl = "http://stats.nba.com/stats/"

  def get(endpoint: String, params: Map[String, String], delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[(String, List[NbaResult])] = {
    val req = url(s"$NbaBaseUrl$endpoint") <<? params
    val body = Http(req OK as.String)
    val requestFuture = body.map(formatResponse)
    if(delayInMillis > 0) Thread.sleep(delayInMillis)
    Observable.create(subscriber => {
      requestFuture.onSuccess {
        case results =>
          results.foreach(r => subscriber.onNext(r))
          subscriber.onCompleted()
      }
      Subscription {}
    })
  }


  def getResultStreamFromRequestStream(requestStream: Observable[(String, List[NbaResult])], targetResultName: String): Observable[NbaResult] = {
      val completeResultStream = requestStream
        .filter{case (resultName, results) => resultName == targetResultName}
        .map{case (_, results) => results}

      Observable.create(subscriber => {
        completeResultStream
          .doOnCompleted{subscriber.onCompleted()}
          .foreach(allResults => {
            allResults.foreach(result => subscriber.onNext(result))
          })
        Subscription {}
      })
  }

  private def formatResponse(response: String): Map[String, List[NbaResult]] = {
    val json = parse(response)
    val values = json.asInstanceOf[JObject].values
    val resultSets = values("resultSets").asInstanceOf[List[Map[String, AnyRef]]]
    resultSets.foldLeft(Map.empty[String, List[Map[String, Any]]])((resultKeyToResult, resultSet) => {
      resultKeyToResult + (resultSet("name").asInstanceOf[String] -> formatResultSet(resultSet))
    })
  }

  private def formatResultSet(resultSet: Map[String, AnyRef]): List[NbaResult] = {
    val headers = resultSet("headers").asInstanceOf[List[String]]
    val rows = resultSet("rowSet").asInstanceOf[List[List[Any]]]
    // Need to remove BigInts because they blow up mongodb
    val mappedRows = rows.map(r => r.collect {
      case i: BigInt => i.toInt
      case notBigInt => notBigInt
    })
    mappedRows.map(row => headers.zip(row).toMap)
  }
}
