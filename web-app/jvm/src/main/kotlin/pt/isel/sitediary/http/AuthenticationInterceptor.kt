package pt.isel.sitediary.http

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.slf4j.LoggerFactory
import pt.isel.sitediary.domainmodel.authentication.AuthenticatedUser

@Component
class AuthenticationInterceptor (
    private val authorizationHeaderProcessor: RequestTokenProcessor
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        logger.info("on preHandle")
        logger.info("Before calling $handler (${handler.javaClass.name})")
        if (request.requestURI.endsWith("/swagger-ui/index.html")) {
            logger.info("Skipping authentication for Swagger UI endpoint")
            return true
        }
        if (handler is HandlerMethod && handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {
            val cookie = request.cookies?.find { it.name == "token" }
            val bearer = request.getHeader(NAME_AUTHORIZATION_HEADER)
            // enforce authentication
            val user = if (cookie != null) authorizationHeaderProcessor.processAuthorizationCookieValue(cookie)
            else authorizationHeaderProcessor.processAuthorizationHeaderValue(bearer)

            logger.info("Exiting preHandle inside if")
            return if (user == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                response.contentType = "application/json"
                response.writer.write("Login Required")
                false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
                true
            }
        }
        logger.info("Exiting preHandle outside if")
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}