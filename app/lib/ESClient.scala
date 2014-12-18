package lib

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.settings.ImmutableSettings
import play.api.Play

object ESClient {
  lazy val remote = Play.current.configuration.getString("elasticsearch.uri").getOrElse("localhost")
  val settings = ImmutableSettings.settingsBuilder.put("cluster.name", "analytics").build
  val client = ElasticClient.remote(settings, (remote, 9300))
}