package controllers

import lib.ESClient
import com.sksamuel.elastic4s.ElasticDsl.{aggregation, search}
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import scala.collection.JavaConverters._
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram
import scala.concurrent.ExecutionContext.Implicits._

import play.api.mvc._
import play.api.libs.json._

object Query extends Controller {
  def index(in: String) = Action.async {
    ESClient.client.execute {
      search in "events/enriched" aggregations {
        aggregation datehistogram "byDeviceTime" field "dvce_tstamp" interval DateHistogram.Interval.HOUR aggs {
          aggregation terms "bySource" field "unstruct_event_1.eventSource" size 10
        }
      }
    } map { query =>
      val r = for {
        key <- in.split('+').toList
      } yield {
        JsObject(List(
          "id" -> JsString(key),
          "name" -> JsString(key),
          "data" -> JsArray(query.getAggregations.get[DateHistogram]("byDeviceTime").getBuckets.asScala.toList.map { bucket =>
            val bySource = bucket.getAggregations.get[Terms]("bySource")
            val viewsLine: Long = bySource.getBuckets.asScala.find(_.getKey == key).map(_.getDocCount).getOrElse(0)
            JsArray(JsNumber(bucket.getKeyAsDate.getMillis) :: JsNumber(viewsLine) :: Nil)
          })
        ))
      }

      Ok(JsArray(r))
    }
  }
}
