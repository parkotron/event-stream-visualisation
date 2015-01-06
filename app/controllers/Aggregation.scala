package controllers

import lib.aggregations.Histogram
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import scala.concurrent.ExecutionContext.Implicits._
import scala.collection.JavaConverters._
import org.elasticsearch.search.aggregations.bucket.range.Range

import play.api.mvc._
import play.api.libs.json._

object Aggregation extends Controller {

  case class RevenueGenderSegment(gender: String, range: String, revenue: Double) {
    def toJson = {
      JsObject (List(
        "gender" -> JsString(gender),
        "range" -> JsString(range),
        "revenue" -> JsNumber(revenue)
      ))
    }
  }


  def revenue = Action.async {
    Histogram.subscriptionRevenue.map { query =>
      Ok(Histogram.formatHighChartResponse(Histogram.extractResponse[DateHistogram](query, "byDeviceTime", "byRevenue")))
    }
  }

  def revenueAgeSegment = Action.async {
    Histogram.subscriptionRevenueByAgeSegment.map { query =>
      val r = query.getAggregations.get[DateHistogram]("byDeviceTime").getBuckets.asScala.toList.lastOption.map { bucket =>
        bucket.getAggregations.get[Range]("byAge").getBuckets.asScala.toList.map { ageSegment =>
          ageSegment.getAggregations.get[Terms]("byGender").getBuckets.asScala.toList.map { genderSegment =>
            RevenueGenderSegment(genderSegment.getKey, ageSegment.getKey, genderSegment.getAggregations.get[Sum]("byRevenue").getValue)
          }
        }
      }.getOrElse(Nil)

      Ok( JsArray(r.map( s => JsArray(s.map(_.toJson)))) )
    }
  }

  //##################

  def histogram(in: String) = Action.async {

    Histogram.eventSources.map { query =>

      val events = in.split('+').toList map { key =>
        Histogram.parseResponse[DateHistogram, Terms](query, key, "byDeviceTime", "bySource")
      }

      Ok(JsArray(events))
    }

  }

  def eventFromSubscription(in: String) = Action.async {
    Histogram.eventsBeforeSubscription.map { query =>

      val events = in.split('+').toList map { key =>
        Histogram.parseResponse[DateHistogram, Terms](query, key, "bySeconds", "bySource")
      }

      Ok(JsArray(events))
    }
  }
}
