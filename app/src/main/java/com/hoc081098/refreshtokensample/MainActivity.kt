package com.hoc081098.refreshtokensample

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hoc081098.refreshtokensample.databinding.ActivityMainBinding
import com.hoc081098.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview

@FlowPreview
@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
  private val binding by viewBinding<ActivityMainBinding>()
  private val vm by viewModels<MainVM>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    vm.userFlow.collectIn(this) {
      binding.textUser.text = when (it) {
        is Lce.Content -> it.content?.toString() ?: "Logged out!"
        is Lce.Error -> "Error: ${it.exception}"
        Lce.Loading -> "Loading..."
      }

      binding.buttonLogoutLogin.text = when (it) {
        is Lce.Content -> if (it.content == null) "Login" else "Logout"
        is Lce.Error -> null
        Lce.Loading -> null
      }
      binding.buttonLogoutLogin.isEnabled = binding.buttonLogoutLogin.text !== null
    }

    binding.buttonLogoutLogin.setOnClickListener {
      if (binding.buttonLogoutLogin.text == "Login") {
        vm.dispatch(MainAction.Login)
      } else {
        vm.dispatch(MainAction.Logout)
      }
    }
  }
}
