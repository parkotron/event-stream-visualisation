package controllers

import lib.aggregations.Histogram
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.histogram.{Histogram => ESHistogram, DateHistogram}
import scala.concurrent.ExecutionContext.Implicits._

import play.api.mvc._
import play.api.libs.json._

object Aggregation extends Controller {
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
