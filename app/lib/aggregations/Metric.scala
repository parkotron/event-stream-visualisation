package lib.aggregations

import com.sksamuel.elastic4s.ElasticDsl._
import lib.ESClient
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Interval
import org.elasticsearch.search.aggregations.metrics.stats.Stats
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.aggregations.metrics.avg.Avg
import play.api.libs.json.{JsNumber, JsObject}
import org.joda.time.{Duration, DateTime}

import scala.concurrent.Future

object Metric {

  def parseSum(query: SearchResponse, outerAddId: String) = {
    JsObject(List(
      "data" -> JsNumber(query.getAggregations.get[Sum](outerAddId).getValue / 100)
    ))
  }

  def parseAverage(query: SearchResponse, outerAddId: String) = {
    JsObject(List(
      "data" -> JsNumber(query.getAggregations.get[Avg](outerAddId).getValue.round)
    ))
  }

  def parseStats(query: SearchResponse, outerAddId: String) = {
    JsObject(List(
      "data" -> JsNumber(0)
    ))
  }

  def subscriptionRevenue(from: String, to: String): Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" query {
        filteredQuery filter {
          rangeFilter("dvce_tstamp") lte "now" gte from
        }
      } aggregations {
        aggregation sum "byRevenue" field "unstruct_event_1.financial.eventValue"
      }
    }
  }

  def subscriberNumbers(from: String): Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" query {
        filteredQuery filter {
          and (
            rangeFilter("dvce_tstamp") lte "now" gte from,
            termFilter("unstruct_event_1.eventSource", "subscription")
          )
        }
      }
    }
  }

  def intervalComparison(from: String, to: String, defined_interval: Interval): Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" query {
        filteredQuery filter {
          rangeFilter("dvce_tstamp") lte (defined_interval match {
            case Interval.WEEK => DateTime.now.weekOfWeekyear().roundFloorCopy().toString("yyyy-MM-dd'T'HH:mm:ss'Z'")
            case _ => DateTime.now.monthOfYear().roundFloorCopy().toString("yyyy-MM-dd'T'HH:mm:ss'Z'")
          })
        }
      } aggregations {
        aggregation datehistogram "byDeviceTime" field "dvce_tstamp" interval defined_interval aggs {
          aggregation sum "byRevenue" field "unstruct_event_1.financial.eventValue"
        }
      }
    }
  }

  def averageTimeFromRegistrationToSubscription: Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" query {
        filteredQuery filter {
          and (
            termFilter("unstruct_event_1.eventSource", "subscription"),
            rangeFilter("unstruct_event_1.from.timesinceregistration") lte new Duration(new DateTime("2014-12-01"), DateTime.now).getStandardSeconds.toString
          )
        }
      } aggregations {
        aggregation avg "byTimeToSubscribe" field "unstruct_event_1.from.timesinceregistration"
      }
    }
  }
}
