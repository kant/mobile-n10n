package notification.services


import azure.{WNSRawPush, NotificationHubClient}
import models._
import models.Importance.{Minor, Major}
import notification.{DateTimeFreezed, NotificationsFixtures}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import tracking.TopicSubscriptionsRepository
import scalaz.syntax.either._
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.Future
import scalaz.syntax.std.option._

class WindowsNotificationSenderSpec(implicit ev: ExecutionEnv) extends Specification
  with Mockito with DateTimeFreezed {

  "the notification sender" should {
    "filter out Minor notifications" in new WNSScope {
      override val importance = Minor
      val expectedReport = senderReport(Senders.Windows).right
      val result = windowsNotificationSender.sendNotification(userPush)

      result should beEqualTo(expectedReport).await
      there was no(hubClient).sendWNSNotification(any[WNSRawPush])
    }

    "process a Major notification" in {
      "send two separate with notifications with differently encoded topics when addressed to topic" in new WNSScope {
        val result = windowsNotificationSender.sendNotification(topicPush)

        result should beEqualTo(senderReport(Senders.Windows, platformStats = PlatformStatistics(WindowsMobile, 2).some).right).await
        got {
          one(hubClient).sendWNSNotification(pushConverter.toRawPush(topicPush))
        }
      }

      "send only one notification when destination is user so that user do not receive the same message twice" in new WNSScope {
        val result = windowsNotificationSender.sendNotification(userPush)

        result should beEqualTo(senderReport(Senders.Windows, platformStats = PlatformStatistics(WindowsMobile, 1).some).right).await
        got {
          one(hubClient).sendWNSNotification(pushConverter.toRawPush(userPush))
        }
      }
    }
  }

  trait WNSScope extends Scope with NotificationsFixtures {
    def importance: Importance = Major
    val userPush = userTargetedBreakingNewsPush(importance)
    val topicPush = topicTargetedBreakingNewsPush(
      breakingNewsNotification(Set(
        Topic(TopicTypes.Breaking, "world/religion"),
        Topic(TopicTypes.Breaking, "world/isis")
      ))
    )

    val configuration = mock[Configuration].debug returns true
    val hubClient = {
      val client = mock[NotificationHubClient]
      client.sendWNSNotification(any[WNSRawPush]) returns Future.successful(().right)
      client
    }

    val pushConverter = new AzureWNSPushConverter(configuration)

    val topicSubscriptionsRepository = {
      val m = mock[TopicSubscriptionsRepository]
      m.count(any[Topic]) returns Future.successful(1.right)
      m
    }

    val windowsNotificationSender = new WNSNotificationSender(hubClient, configuration, topicSubscriptionsRepository)
  }
}
