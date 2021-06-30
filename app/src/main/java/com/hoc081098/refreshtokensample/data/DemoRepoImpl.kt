package com.hoc081098.refreshtokensample.data

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.domain.DemoRepo
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DemoRepoImpl @Inject constructor(
  private val apiService: ApiService,
  private val appDispatchers: AppDispatchers,
) : DemoRepo {
  override suspend fun demo() = withContext(appDispatchers.io) { apiService.demo() }
}
