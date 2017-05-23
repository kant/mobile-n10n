package registration

import play.api.{Configuration, Environment, Logger}
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.core.SourceMapper
import play.mvc.Http

import scala.concurrent.Future

class LogHttpErrorHandler(
  environment: Environment,
  configuration: Configuration,
  sourceMapper: Option[SourceMapper] = None,
  router: => Option[Router] = None) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router) {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Logger.error(s"HTTP $statusCode $request $message ${request.headers.get(Http.HeaderNames.USER_AGENT)}")
    super.onClientError(request, statusCode, message)
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = super.onServerError(request, exception)
}
