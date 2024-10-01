package pt.isel.sitediary.utils

import org.springframework.http.ResponseEntity

inline fun <reified T> handleResponse(
    res: Result<Errors, T>,
    makeResponse: (T) -> ResponseEntity<*>
): ResponseEntity<*> {
    return when (res) {
        is Success -> {
            makeResponse(res.value)
        }
        is Failure -> {
            Errors.response(res.value)
        }
    }
}