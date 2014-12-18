package controllers

import play.api.mvc._

object Dashboard extends Controller {

  def histogram = Action {
    Ok(views.html.Dashboard.histogram("Event Histogram"))
  }

  def scatter = Action {
    Ok(views.html.Dashboard.scatter("Scatter"))
  }
}