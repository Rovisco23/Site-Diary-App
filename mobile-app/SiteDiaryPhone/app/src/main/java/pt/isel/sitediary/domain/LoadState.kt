package pt.isel.sitediary.domain

sealed class LoadState<out T>

data object Idle : LoadState<Nothing>()

data object Loading : LoadState<Nothing>()

data class Loaded<T>(val value: Result<T>) : LoadState<T>()

fun idle(): Idle = Idle

fun loading(): Loading = Loading

fun <T> loaded(value: Result<T>): Loaded<T> = Loaded(value)

fun <T> LoadState<T>.getOrNull(): T? = when (this) {
    is Loaded -> value.getOrNull()
    else -> null
}

fun <T> LoadState<T>.getOrThrow(): T = when (this) {
    is Loaded -> value.getOrThrow()
    else -> throw IllegalStateException("No value available")
}

fun <T> LoadState<T>.exceptionOrNull(): Throwable? = when (this) {
    is Loaded -> value.exceptionOrNull()
    else -> null
}