package pt.isel.sitediary.utils

sealed class Result<out L , out R> {
    data class Left<out L>(val value: L) : Result<L, Nothing>()
    data class Right<out R>(val value: R) : Result<Nothing, R>()
}

fun <R> success(value: R) = Result.Right(value)
fun <L> failure(error: L) = Result.Left(error)

typealias Success<S> = Result.Right<S>
typealias Failure<F> = Result.Left<F>