import play.api.GlobalSettings

object Global extends GlobalSettings {
  override def onStart(application: play.api.Application): Unit = {}
  override def onStop(application: play.api.Application) {}
}