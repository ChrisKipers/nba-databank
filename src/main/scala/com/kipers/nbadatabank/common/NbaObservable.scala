package com.kipers.nbadatabank.common

import com.kipers.nbadatabank.common.Types.NbaResult
import rx.lang.scala.Observable

object NbaObservable {
  implicit def observableToNbaObservable(observable: Observable[NbaResult]) = new NbaObservable(observable)
}

// TODO: inject DBService
class NbaObservable(val observable: Observable[NbaResult]) {
  def batchInsertResults(collectionName: String, batchSize: Int) {
    observable.doOnCompleted(println(s"Done inserted all $collectionName"))
      .slidingBuffer(batchSize, batchSize)
      .zipWithIndex
      .subscribe(gameLogsWithIndex => {
        val (gameLogs, indexOfBatch) = gameLogsWithIndex
        val indexOfGamelog = indexOfBatch * batchSize
        println(s"Inserting $collectionName $indexOfGamelog through ${indexOfGamelog + gameLogs.length}")
        DBService.insertResult(gameLogs, collectionName)
      })
  }
}