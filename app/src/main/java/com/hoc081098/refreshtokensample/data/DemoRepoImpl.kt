package com.hoc081098.refreshtokensample.data

import com.hoc081098.refreshtokensample.domain.DemoRepo
import javax.inject.Inject

class DemoRepoImpl @Inject constructor() : DemoRepo {
  override suspend fun demo(): String {
    TODO("Not yet implemented")
  }
}
