package com.kipers.nbadatabank.common

import com.kipers.nbadatabank.common.Types.NbaResult
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext

trait StatsAPI {
  def get(endpoint: String, params: Map[String, String], delayInMillis: Int = 0)(implicit exec: ExecutionContext): Observable[(String, List[NbaResult])]

  def getResultStreamFromRequestStream(requestStream: Observable[(String, List[NbaResult])], resultName: String): Observable[NbaResult]
}
