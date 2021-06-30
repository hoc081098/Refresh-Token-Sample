package com.hoc081098.refreshtokensample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.refreshtokensample.domain.AuthRepo
import com.hoc081098.refreshtokensample.domain.DemoRepo
import com.hoc081098.refreshtokensample.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainAction {
  object Logout : MainAction
  object Login : MainAction
  object Demo : MainAction
}

sealed interface MainSingleEvent {
  data class LoginFailed(val throwable: Throwable) : MainSingleEvent
  data class LogoutFailed(val throwable: Throwable) : MainSingleEvent
}

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class MainVM @Inject constructor(
  private val repo: AuthRepo,
  private val demoRepo: DemoRepo,
) : ViewModel() {
  val userFlow: StateFlow<Lce<User?>> = repo.user()
    .map { Lce.content(it) }
    .onStart { emit(Lce.loading()) }
    .catch { emit(Lce.error(it)) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = Lce.loading(),
    )
  val demoFlow: StateFlow<Lce<String>>

  private val actions = Channel<MainAction>(Channel.UNLIMITED)
  private val events = Channel<MainSingleEvent>(Channel.UNLIMITED)

  fun dispatch(action: MainAction) {
    actions.trySend(action)
  }

  val eventFlow: Flow<MainSingleEvent> get() = events.receiveAsFlow()

  init {
    val actionsFlow =
      actions.consumeAsFlow().shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    actionsFlow
      .filterIsInstance<MainAction.Logout>()
      .flatMapMerge {
        repo::logout.asFlow()
          .catch { events.send(MainSingleEvent.LogoutFailed(it)) }
      }
      .launchIn(viewModelScope)

    actionsFlow
      .filterIsInstance<MainAction.Login>()
      .flatMapMerge {
        repo::login.asFlow()
          .catch { events.send(MainSingleEvent.LoginFailed(it)) }
      }
      .launchIn(viewModelScope)

    demoFlow = actionsFlow
      .filterIsInstance<MainAction.Demo>()
      .flatMapMerge {
        demoRepo::demo.asFlow()
          .map { Lce.content(it) }
          .onStart { emit(Lce.loading()) }
          .catch { emit(Lce.error(it)) }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Lce.loading(),
      )
  }
}
