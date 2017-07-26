package registration.services

import cats.data.Xor
import cats.implicits._
import error.NotificationsError
import models._
import play.api.Logger
import registration.models.LegacyNewsstandRegistration

class LegacyNewsstandRegistrationConverter extends RegistrationConverter[LegacyNewsstandRegistration] {

  val logger = Logger(classOf[LegacyNewsstandRegistrationConverter])

  def toRegistration(legacyRegistration: LegacyNewsstandRegistration): NotificationsError Xor Registration = {
    val udid = NewsstandUdid.fromDeviceToken(legacyRegistration.pushToken)

    logger.info(s"Registering for newstand with pushtoken: ${legacyRegistration.pushToken}")

    Registration(
      deviceId = legacyRegistration.pushToken,
      platform = Newsstand,
      udid = udid,
      topics = Set(Topic(TopicTypes.Newsstand, "newsstand")),
      buildTier = None
    ).right
  }

  def fromResponse(legacyRegistration: LegacyNewsstandRegistration, response: RegistrationResponse): LegacyNewsstandRegistration =
    legacyRegistration
}
