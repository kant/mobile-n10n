package notifications.providers

import java.util.UUID
import azure.{WNSRegistrationId, NotificationHubClient, RawWindowsRegistration, RegistrationResponse => HubRegistrationResponse}
import RawWindowsRegistration.fromMobileRegistration
import models.{Registration, UserId, WindowsMobile}
import org.joda.time.DateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.Future
import scalaz.syntax.either._

class WindowsNotificationProviderSpec(implicit ev: ExecutionEnv) extends Specification
with Mockito {
  "Windows Notification Provider registration" should {
    "create new registration when no registrations found for channel uri" in new registrations {
      val channelUri = registration.deviceId
      hubClient.registrationsByChannelUri(channelUri) returns Future.successful(List.empty.right)
      hubClient.create(fromMobileRegistration(registration)) returns Future.successful(hubRegResponse.right)

      val response = provider.register(registration)

      response must beEqualTo(RegistrationResponse(
        deviceId = channelUri,
        platform = WindowsMobile,
        userId = registration.userId,
        topics = Set.empty
      ).right).await
    }

    "update existing registration when registration already exist" in new registrations {
      val channelUri = registration.deviceId
      hubClient.registrationsByChannelUri(channelUri) returns Future.successful(List(hubRegResponse).right)
      hubClient.update(hubRegResponse.registration, fromMobileRegistration(registration)) returns Future.successful(hubRegResponse.right)
  
      val response = provider.register(registration)
  
      response must beEqualTo(RegistrationResponse(
        deviceId = channelUri,
        platform = WindowsMobile,
        userId = registration.userId,
        topics = Set.empty
      ).right).await
    }

  }

  trait registrations extends Scope {
    val hubClient = mock[NotificationHubClient]
    val provider = new WindowsNotificationProvider(hubClient)

    val userId = UserId(UUID.fromString("988ADFF8-8961-11E5-96E3-D0DB64696656"))

    val registration = Registration("deviceId", WindowsMobile, userId, Set.empty)
    val hubRegResponse = HubRegistrationResponse(
      registration = WNSRegistrationId("regId"),
      tags = List(s"user:${userId.id.toString}"),
      channelUri = registration.deviceId,
      expirationTime = DateTime.now)
  }

}
