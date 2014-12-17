package lib

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.settings.ImmutableSettings

object ESClient {
  val settings = ImmutableSettings.settingsBuilder.put("cluster.name", "analytics").build
  lazy val client = ElasticClient.remote(settings, ("URL", 9300))
}