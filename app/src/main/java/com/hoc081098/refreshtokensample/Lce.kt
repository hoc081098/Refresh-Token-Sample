package com.hoc081098.refreshtokensample

sealed class Lce<out T> {
  object Loading : Lce<Nothing>()
  data class Content<out T>(val content: T) : Lce<T>()
  data class Error<out T>(val exception: Throwable) : Lce<T>()

  companion object Factory {
    fun <T> content(content: T): Lce<T> = Content(content)
    fun <T> loading(): Lce<T> = Loading
    fun <T> error(exception: Throwable): Lce<T> = Error(exception)
  }
}
