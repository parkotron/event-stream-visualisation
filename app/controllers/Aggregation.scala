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

  case class GenderSegmentMetric(gender: String, range: String, revenue: Double) {
    def toJson = {
      JsObject (List(
        "gender" -> JsString(gender),
        "range" -> JsString(range),
        "revenue" -> JsNumber(revenue)
      ))
    }
  }

  case class SourceSegmentedGenderMetric(source: String, metric: List[GenderSegmentMetric]) {
    def toJson = {
      JsObject (List(
        "source" -> JsString(source),
        "metric" -> JsObject(metric.map( m => m.gender -> m.toJson))
      ))
    }
  }

  def revenue = Action.async {
    Histogram.subscriptionRevenue.map { query =>
      Ok(Histogram.formatHighChartResponse(Histogram.extractResponse[DateHistogram](query, "byDeviceTime", "byRevenue")))
    }
  }

  def revenueAgeSegment = Action.async {
    Histogram.subscriptionRevenueByAgeSegment().map { query =>
      val parsed = query.getAggregations.get[DateHistogram]("byDeviceTime").getBuckets.asScala.toList.map { bucket =>
        bucket.getAggregations.get[Range]("byAge").getBuckets.asScala.toList.map { ageSegment =>
          ageSegment.getAggregations.get[Terms]("byGender").getBuckets.asScala.toList.map { genderSegment =>
            GenderSegmentMetric(genderSegment.getKey, ageSegment.getKey, genderSegment.getAggregations.get[Sum]("byRevenue").getValue / 100)
          }
        }
      }

      Ok( JsArray(parsed.map { list =>
        JsArray(list.map { inner =>
          JsObject(inner.map( segment => segment.gender -> segment.toJson ))
        })
      }))
    }
  }

  def subscribers = Action.async {
    Histogram.subscribers.map { query =>
      val parsed = query.getAggregations.get[DateHistogram]("byDeviceTime").getBuckets.asScala.toList.map { bucket =>
        bucket.getKeyAsNumber.longValue -> bucket.getDocCount
      }

      Ok( JsArray(parsed.map( a => JsArray(List(JsNumber(a._1), JsNumber(a._2))) ) ) )
    }
  }

  def subscribersAgeSegment = Action.async {
    Histogram.subscriptionRevenueByAgeSegment().map { query =>
      val parsed = query.getAggregations.get[DateHistogram]("byDeviceTime").getBuckets.asScala.toList.map { bucket =>
        bucket.getAggregations.get[Range]("byAge").getBuckets.asScala.toList.map { ageSegment =>
          ageSegment.getAggregations.get[Terms]("byGender").getBuckets.asScala.toList.map { genderSegment =>
            GenderSegmentMetric(genderSegment.getKey, ageSegment.getKey, genderSegment.getDocCount)
          }
        }
      }

      Ok( JsArray(parsed.map { list =>
        JsArray(list.map { inner =>
          JsObject(inner.map( segment => segment.gender -> segment.toJson ))
        })
      }))
    }
  }

  def interactionsAgeSegment = Action.async {
    Histogram.interactionsByAgeSegment.map { query =>
      val parsed = query.getAggregations.get[DateHistogram]("byDeviceTime").getBuckets.asScala.toList.map { bucket =>
        bucket.getAggregations.get[Range]("byAge").getBuckets.asScala.toList.map { ageSegment =>
          ageSegment.getAggregations.get[Terms]("bySource").getBuckets.asScala.toList.map { sourceSegment =>
            SourceSegmentedGenderMetric(
              sourceSegment.getKey,
              sourceSegment.getAggregations.get[Terms]("byGender").getBuckets.asScala.toList.map { genderSegment =>
                GenderSegmentMetric(genderSegment.getKey, ageSegment.getKey, genderSegment.getDocCount)
              }
            )
          }
        }
      }

      Ok( JsArray(parsed.map { time =>
        JsArray(time.map { age =>
          JsArray(age.map { source =>
            JsObject(source.source -> source.toJson :: Nil)
          })
        })
      }))
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
