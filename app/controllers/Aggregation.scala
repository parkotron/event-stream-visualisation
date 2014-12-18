package controllers

import lib.aggregations.Histogram
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram
import scala.concurrent.ExecutionContext.Implicits._

import play.api.mvc._
import play.api.libs.json._

object Aggregation extends Controller {
  def histogram(in: String) = Action.async {
    Histogram.eventSources.map { query =>
      val r = for {
        key <- in.split('+').toList
      } yield {
        Histogram.parseResponse[DateHistogram, Terms](query, key, "byDeviceTime", "bySource")
      }

      Ok(JsArray(r))
    }
  }
}
