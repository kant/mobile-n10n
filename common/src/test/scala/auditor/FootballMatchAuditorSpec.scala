package auditor

import models.{Topic, TopicTypes}
import org.joda.time.DateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pa.{MatchDay, PaClient}
import org.mockito.Matchers.{eq => argEq}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class FootballMatchAuditorSpec(implicit ev: ExecutionEnv) extends Specification with Mockito {
  "Football match client" should {
    "do not query PA if there are no football matches in topic list" in {
      val paClient = mock[PaClient]
      val topics = Set(
        Topic(TopicTypes.Breaking, "test-1"),
        Topic(TopicTypes.Content, "test-2"),
        Topic(TopicTypes.TagSeries, "test-3")
      )

      val auditor = FootballMatchAuditor(paClient)
      val filteredTopics = auditor.expiredTopics(topics)
      filteredTopics must beEqualTo(Set.empty).awaitFor(5 seconds)
      there were no(paClient).matchInfo(any[String])(any[ExecutionContext])
    }
  }
}
