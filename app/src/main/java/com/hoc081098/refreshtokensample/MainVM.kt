package com.hoc081098.refreshtokensample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.hoc081098.refreshtokensample.domain.AuthRepo
import com.hoc081098.refreshtokensample.domain.DemoRepo
import com.hoc081098.refreshtokensample.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

  private val actionsFlow = MutableSharedFlow<MainAction>(extraBufferCapacity = 1)
  private val eventsChannel = Channel<MainSingleEvent>(Channel.UNLIMITED)

  fun dispatch(action: MainAction) {
    viewModelScope.launch {
      actionsFlow.emit(action)
    }
  }

  val eventFlow: Flow<MainSingleEvent> get() = eventsChannel.receiveAsFlow()

  init {
    actionsFlow
      .filterIsInstance<MainAction.Logout>()
      .flatMapFirst {
        flowFromSuspend(repo::logout)
          .catch { eventsChannel.send(MainSingleEvent.LogoutFailed(it)) }
      }
      .launchIn(viewModelScope)

    actionsFlow
      .filterIsInstance<MainAction.Login>()
      .flatMapFirst {
        flowFromSuspend(repo::login)
          .catch { eventsChannel.send(MainSingleEvent.LoginFailed(it)) }
      }
      .launchIn(viewModelScope)

    demoFlow = actionsFlow
      .filterIsInstance<MainAction.Demo>()
      .flatMapMerge {
        flowFromSuspend(demoRepo::demo)
          .map { Lce.content(it) }
          .startWith(Lce.loading())
          .catch { emit(Lce.error(it)) }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Lce.loading(),
      )
  }
}
