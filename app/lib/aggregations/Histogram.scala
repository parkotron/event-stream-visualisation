package lib.aggregations

import com.sksamuel.elastic4s.ElasticDsl.{aggregation, search}
import lib.ESClient
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.Aggregation
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.search.aggregations.bucket.histogram.{DateHistogram, Histogram => ESHistogram}
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.concurrent.Future

object Histogram {

  def parseResponse[A <: ESHistogram, B <: MultiBucketsAggregation](query: SearchResponse, key: String, outerAddId: String, innerAggId: String) = {
    JsObject(List(
      "id" -> JsString(key),
      "name" -> JsString(key),
      "data" -> JsArray(query.getAggregations.get[A](outerAddId).getBuckets.asScala.toList.map { bucket =>
        val bySource = bucket.getAggregations.get[B](innerAggId)
        val viewsLine: Long = bySource.getBuckets.asScala.find(_.getKey == key).map(_.getDocCount).getOrElse(0)
        JsArray(JsNumber(bucket.getKeyAsNumber.longValue) :: JsNumber(viewsLine) :: Nil)
      })
    ))
  }

  def extractResponse[A <: ESHistogram](query: SearchResponse, outerAddId: String, innerAggId: String): List[(Long, Double)] = {
    query.getAggregations.get[A](outerAddId).getBuckets.asScala.toList.map { bucket =>
      bucket.getKeyAsNumber.longValue -> bucket.getAggregations.get[Sum](innerAggId).getValue / 100
    }
  }

  def formatHighChartResponse(data: List[(Long, Double)]) = {
    JsObject(List(
      "data" -> JsArray(data.map { result =>
        JsArray(JsNumber(result._1) :: JsNumber(result._2) :: Nil)
      })
    ))
  }

  def subscriptionRevenue: Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" aggregations {
        aggregation datehistogram "byDeviceTime" field "dvce_tstamp" interval DateHistogram.Interval.HOUR aggs {
          aggregation sum "byRevenue" field "unstruct_event_1.financial.eventValue"
        }
      }
    }
  }

  def subscriptionRevenueByAgeSegment: Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" aggregations {
        aggregation datehistogram "byDeviceTime" field "dvce_tstamp" interval DateHistogram.Interval.WEEK aggs {
          aggregation range "byAge" field "unstruct_event_1.from.age" ranges (18.0 -> 24, 25.0 -> 34, 35.0 -> 44, 45.0 -> 54, 55.0 -> 64, 64.0 -> 100) aggs {
            aggregation terms "byGender" field "unstruct_event_1.from.gender" size 2 aggs {
              aggregation sum "byRevenue" field "unstruct_event_1.financial.eventValue"
            }
          }
        }
      }
    }
  }

  def eventSources: Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" aggregations {
        aggregation datehistogram "byDeviceTime" field "dvce_tstamp" interval DateHistogram.Interval.HOUR aggs {
          aggregation terms "bySource" field "unstruct_event_1.eventSource" size 10
        }
      }
    }
  }

  def eventsBeforeSubscription: Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" aggregations {
        aggregation datehistogram "bySeconds" field "timesincesubscription" minDocCount 5 interval 10000 aggs {
          aggregation terms "bySource" field "unstruct_event_1.eventSource" size 1
        }
      }
    }
  }
}
