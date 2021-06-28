package com.hoc081098.refreshtokensample

import androidx.lifecycle.ViewModel
import com.hoc081098.refreshtokensample.domain.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
  private val repo: AuthRepo
): ViewModel()
