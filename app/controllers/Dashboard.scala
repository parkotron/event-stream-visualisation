package controllers

import play.api.mvc._

object Dashboard extends Controller {

  def section(request: play.api.mvc.Request[Any]): String = {
    request.path.split('/').drop(1).take(1).headOption.getOrElse("")
  }

  def revenue = Action { implicit request =>
    Ok(views.html.Dashboard.revenue(section(request), "Who is most valuable to us?"))
  }

  def subscription = Action { implicit request =>
    Ok(views.html.Dashboard.subscription(section(request), "Subscriptions"))
  }

  def subscriptionDailyKPI = Action { implicit request =>
    println(section(request))

    Ok(views.html.Dashboard.subscriptionbyday(section(request), "Subscription metrics by day"))
  }

  def activity = Action { implicit request =>
    Ok(views.html.Dashboard.activity(section(request), "User activity"))
  }

  //######################


  /*def histogram = Action { implicit request =>
    Ok(views.html.Dashboard.histogram())
  }

  def presub = Action { implicit request =>
    Ok(views.html.Dashboard.presub())
  }

  def scatter = Action { implicit request =>
    Ok(views.html.Dashboard.scatter())
  }*/
}