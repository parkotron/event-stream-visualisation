package controllers

import play.api.mvc._

object Dashboard extends Controller {

  def histogram = Action {
    Ok(views.html.Dashboard.histogram())
  }

  def presub = Action {
    Ok(views.html.Dashboard.presub())
  }

  def scatter = Action {
    Ok(views.html.Dashboard.scatter())
  }
}