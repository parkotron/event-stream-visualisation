package controllers

import controllers.Aggregation._
import lib.aggregations.Histogram
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Interval
import play.api.libs.json.JsNumber
import play.api.mvc.Action
import scala.concurrent.ExecutionContext.Implicits._

object Metric {
  def revenue(to: String, from: String) = Action.async {
    lib.aggregations.Metric.subscriptionRevenue(from, to).map { query =>
      Ok(lib.aggregations.Metric.parseSum(query, "byRevenue"))
    }
  }

  def subscribers(from: String) = Action.async {
    lib.aggregations.Metric.subscriberNumbers(from).map { query =>
      Ok(JsNumber(query.getHits.totalHits))
    }
  }

  private def variance(a: Double, b: Double): Long = {
    ((b - a) / a) * 100 round
  }

  def weekOnWeek(from: String, to: String) = Action.async {
    timeOnTime(from, to, Interval.WEEK)
  }

  def monthOnMonth(from: String, to: String) = Action.async {
    timeOnTime(from, to, Interval.MONTH)
  }

  def timeOnTime(from: String, to: String, interval: Interval) = {
    lib.aggregations.Metric.intervalComparison(from, to, interval).map { query =>
      val results = Histogram.extractResponse[DateHistogram](query, "byDeviceTime", "byRevenue")
        .dropRight(1).takeRight(2).map(a => a._2)

      val va = for {
        a <- results.headOption
        b <- results.lastOption
      } yield {
        variance(a, b)
      }

      va.map { num =>
        Ok(JsNumber(num))
      }.getOrElse(NotFound)
    }
  }

  def avgTimeToSubscribe = Action.async {
    lib.aggregations.Metric.averageTimeFromRegistrationToSubscription.map { query =>
      Ok(lib.aggregations.Metric.parseAverage(query, "byTimeToSubscribe"))
    }
  }
}
