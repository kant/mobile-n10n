package azure

case class NotificationHubConnection(
  endpoint: String,
  sharedAccessKeyName: String,
  sharedAccessKey: String
) {
  def authorizationHeader(uri: String): String = SasTokenGeneration.generateSasToken(
    sasKeyName = sharedAccessKeyName,
    sasKeyValue = sharedAccessKey,
    uri = uri
  )
}