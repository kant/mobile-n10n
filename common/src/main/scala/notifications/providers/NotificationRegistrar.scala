package notifications.providers

import models.{Topic, UserId, Platform, Registration}

import scala.concurrent.Future
import scalaz.\/

case class RegistrationResponse(deviceId: String, platform: Platform, userId: UserId, topics: Set[Topic])

object RegistrationResponse {
  import play.api.libs.json._

  implicit val jf = Json.format[RegistrationResponse]
}

trait NotificationRegistrar {
  def register(registration: Registration): Future[Error \/ RegistrationResponse]
}
