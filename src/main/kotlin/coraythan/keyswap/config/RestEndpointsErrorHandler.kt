package coraythan.keyswap.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
class RestErrorHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException::class)
    fun badRequestException(ex: BadRequestException, request: HttpServletRequest): ErrorResponse {
        logBadRequestInfo(ex, request)
        return ErrorResponse(ex.message!!)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException::class)
    fun illegalState(ex: IllegalStateException, request: HttpServletRequest): ErrorResponse {
        logBadRequestInfo(ex, request)
        return ErrorResponse(ex.message!!)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgument(ex: IllegalArgumentException, request: HttpServletRequest): ErrorResponse {
        logBadRequestInfo(ex, request)
        return ErrorResponse(ex.message!!)
    }

    private fun logBadRequestInfo(ex: Exception, request: HttpServletRequest) {
        log.info(
                "In bad request response handler ${ex.message}. " +
                        "For request url: ${request.requestURI} " +
                        "remote address ${request.remoteAddr} " +
                        "remote host ${request.remoteHost}"
        )
    }

}

class BadRequestException(message: String) : RuntimeException(message)

data class ErrorResponse(
        val message: String
)
