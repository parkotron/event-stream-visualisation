package controllers

import play.api.mvc._

object Dashboard extends Controller {

  def index = Action {
    Ok(views.html.Dashboard.index("Event Histogram"))
  }

}