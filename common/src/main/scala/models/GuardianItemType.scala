package models

import play.api.data.validation.ValidationError
import play.api.libs.json.Json

sealed case class GuardianItemType(mobileAggregatorPrefix: String)

object GuardianItemType {
  implicit val reads = Json.reads[GuardianItemType].collect(ValidationError("Unrecognised item type")) {
    case GuardianItemType("section") => GITSection
    case GuardianItemType("latest") => GITTag
    case GuardianItemType("item-trimmed") => GITContent
  }

  implicit val writes = Json.writes[GuardianItemType]
}

object GITSection extends GuardianItemType("section")
object GITTag extends GuardianItemType("latest")
object GITContent extends GuardianItemType("item-trimmed")