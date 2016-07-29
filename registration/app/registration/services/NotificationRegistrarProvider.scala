package registration.services

import error.{NotificationsError, RequestError}
import models.{Android, iOS, Registration, WindowsMobile}
import registration.services.azure.{APNSNotificationRegistrar, GCMNotificationRegistrar, WindowsNotificationRegistrar}

import scala.concurrent.ExecutionContext
import scalaz.\/
import scalaz.syntax.either._

trait RegistrarProvider {
  def registrarFor(registration: Registration): \/[NotificationsError, NotificationRegistrar]
}

case class UnsupportedPlatform(platform: String) extends RequestError {
  override def reason: String = s"Platform '$platform' is not supported"
}

final class NotificationRegistrarProvider(
  windowsNotificationRegistrar: WindowsNotificationRegistrar,
  gcmNotificationRegistrar: GCMNotificationRegistrar,
  apnsNotificationRegistrar: APNSNotificationRegistrar)
  (implicit executionContext: ExecutionContext) extends RegistrarProvider {

  override def registrarFor(registration: Registration): NotificationsError \/ NotificationRegistrar = registration.platform match {
    case WindowsMobile => windowsNotificationRegistrar.right
    case Android => gcmNotificationRegistrar.right
    case `iOS` => apnsNotificationRegistrar.right
    case _ => UnsupportedPlatform(registration.platform.toString).left
  }
}
