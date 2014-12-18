package lib.queries

import lib.ESClient
import com.sksamuel.elastic4s.ElasticDsl.search
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import play.api.libs.json._
import scala.concurrent.Future
import scala.collection.JavaConverters._

object Scatter {

  private def doParse(hits:List[SearchHit], tag: String) = {
    val data = hits.map { hit =>
      val source = hit.getSource.asScala

      (for {
        unstruct <- source.get("unstruct_event_1")
        from <- unstruct.asInstanceOf[java.util.Map[String, _]].asScala.get("from")
        rawParams <- unstruct.asInstanceOf[java.util.Map[String, _]].asScala.get("params")
        ageF <- from.asInstanceOf[java.util.Map[String, _]].asScala.get("age")
        params = rawParams.asInstanceOf[java.util.Map[String, _]].asScala
        max <- params.get("age-max")
        min <- params.get("age-min")
      } yield {
        val range = List[Int](max.asInstanceOf[Int], min.asInstanceOf[Int])
        val avg: Int = range.sum/range.length
        JsArray(List(JsNumber(ageF.asInstanceOf[Int]) , JsNumber(avg)))
      }) getOrElse JsArray()
    }

    JsObject(List(
      "name" -> JsString(s"Average age searched for by a $tag"),
      "data" -> JsArray(data)
    ))
  }

  def parseResponse(query: SearchResponse): Seq[JsValue] = {
    val (men, women): (List[SearchHit], List[SearchHit]) = query.getHits.getHits.toList.partition { hit =>
      val raw = Json.parse(hit.getSourceAsString)
      (raw \ "unstruct_event_1" \ "from" \ "gender").as[String] == "Man"
    }

    Seq(doParse(men, "Male"), doParse(women, "Female"))
  }

  def searchAges: Future[SearchResponse] = {
    ESClient.client.execute {
      search in "events/enriched" query "unstruct_event_1.eventSource:search" limit 20000
    }
  }
}
