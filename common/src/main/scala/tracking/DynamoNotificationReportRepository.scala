package tracking

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions._

import cats.data.Xor
import cats.implicits._

import org.joda.time.DateTime

import com.amazonaws.services.dynamodbv2.model._

import aws.AsyncDynamo
import aws.AsyncDynamo._
import aws.DynamoJsonConversions._

import models.{NotificationReport, NotificationType}
import tracking.Repository.RepositoryResult


class DynamoNotificationReportRepository(client: AsyncDynamo, tableName: String)
  (implicit ec: ExecutionContext)
  extends SentNotificationReportRepository {

  private val SentTimeField = "sentTime"
  private val IdField = "id"
  private val TypeField = "type"
  private val SentTimeIndex = "sentTime-index"

  override def store(report: NotificationReport): Future[RepositoryResult[Unit]] = {
    val putItemRequest = new PutItemRequest(tableName, toAttributeMap(report))
    client.putItem(putItemRequest) map { _ => ().right }
  }

  override def getByTypeWithDateRange(notificationType: NotificationType, from: DateTime, to: DateTime): Future[RepositoryResult[List[NotificationReport]]] = {
    val q = new QueryRequest(tableName)
      .withIndexName(SentTimeIndex)
      .withKeyConditions(Map(
        TypeField -> keyEquals(notificationType.value),
        SentTimeField -> keyBetween(from.toString, to.toString)
      ))

    client.query(q) map { result =>
      (result.getItems.toList.flatMap { item =>
        fromAttributeMap[NotificationReport](item.toMap).asOpt
      }).right
    }
  }

  override def getByUuid(uuid: UUID): Future[RepositoryResult[NotificationReport]] = {
    val q = new QueryRequest(tableName)
      .withKeyConditions(Map(IdField -> keyEquals(uuid.toString)))
      .withConsistentRead(true)

    client.query(q) map { result =>
      for {
        item <- Xor.fromOption(result.getItems.headOption, RepositoryError("UUID not found"))
        parsed <- Xor.fromOption(fromAttributeMap[NotificationReport](item.toMap).asOpt, RepositoryError("Unable to parse report"))
      } yield parsed
    }
  }
}
