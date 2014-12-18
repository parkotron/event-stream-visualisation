package lib.aggregations

import com.sksamuel.elastic4s.ElasticDsl.{aggregation, search}
import lib.ESClient
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.search.aggregations.bucket.histogram.{DateHistogram, Histogram => ESHistogram}
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

  def eventSources: Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" aggregations {
        aggregation datehistogram "byDeviceTime" field "dvce_tstamp" interval DateHistogram.Interval.HOUR aggs {
          aggregation terms "bySource" field "unstruct_event_1.eventSource" size 10
        }
      }
    }
  }
}
