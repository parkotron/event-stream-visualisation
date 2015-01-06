package controllers

import lib.queries.Scatter
import scala.concurrent.ExecutionContext.Implicits._

import play.api.mvc._
import play.api.libs.json._

object Query extends Controller {

  def scatter = Action.async {
    Scatter.searchAges.map { query =>
      val scatter = Scatter.parseResponse(query)
      Ok(JsArray(scatter))
    }
  }
}
