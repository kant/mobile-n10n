package azure

case class WNSRawPush(body: String, tags: Option[Tags]) {
  def tagQuery: Option[String] = tags.map { set =>
    set.tags.map(_.encodedTag).mkString("(", " || ", ")")
  }
}