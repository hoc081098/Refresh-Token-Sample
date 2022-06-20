package com.hoc081098.refreshtokensample.data

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.domain.DemoRepo
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.withContext

class DemoRepoImpl @Inject constructor(
  private val apiService: ApiService,
  private val appDispatchers: AppDispatchers,
) : DemoRepo {
  private val count = AtomicInteger()

  override suspend fun demo() = withContext(appDispatchers.io) {
    apiService.demo(count.getAndIncrement())
  }
}
