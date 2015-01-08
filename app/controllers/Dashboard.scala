package controllers

import play.api.mvc._

object Dashboard extends Controller {

  def revenue = Action {
    Ok(views.html.Dashboard.revenue("Who is most valuable to us?"))
  }

  def subscription = Action {
    Ok(views.html.Dashboard.subscription("Subscriptions"))
  }

  //######################


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