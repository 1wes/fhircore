/*
 * Copyright 2021 Ona Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smartregister.fhircore.engine.ui

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import javax.inject.Inject
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.smartregister.fhircore.engine.auth.AccountAuthenticator
import org.smartregister.fhircore.engine.configuration.ConfigurationRegistry
import org.smartregister.fhircore.engine.robolectric.RobolectricTest
import org.smartregister.fhircore.engine.ui.register.RegisterViewModel
import org.smartregister.fhircore.engine.ui.register.model.RegisterFilterType
import org.smartregister.fhircore.engine.util.SharedPreferencesHelper

@HiltAndroidTest
class RegisterViewModelTest : RobolectricTest() {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @Inject lateinit var accountAuthenticator: AccountAuthenticator

  @Inject lateinit var sharedPreferencesHelper: SharedPreferencesHelper

  @Inject lateinit var configurationRegistry: ConfigurationRegistry

  private lateinit var viewModel: RegisterViewModel

  @Before
  fun setUp() {
    hiltRule.inject()
    configurationRegistry.loadAppConfigurations("appId", accountAuthenticator) {}
    viewModel =
      RegisterViewModel(
        fhirEngine = mockk(),
        syncJob = mockk(),
        fhirResourceDataSource = mockk(),
        configurationRegistry = configurationRegistry,
        dispatcher = mockk(),
        sharedPreferencesHelper = sharedPreferencesHelper
      )
  }

  @Test
  fun testUpdateViewConfigurationsShouldUpdateGlobalVariable() {
    viewModel.updateViewConfigurations(
      mockk {
        every { appId } returns "appId"
        every { appTitle } returns "Covax"
      }
    )

    Assert.assertEquals("appId", viewModel.registerViewConfiguration.value?.appId)
    Assert.assertEquals("Covax", viewModel.registerViewConfiguration.value?.appTitle)
  }

  @Test
  fun testLoadLanguagesShouldLoadEnglishLocaleOnly() {
    viewModel.loadLanguages()
    Assert.assertEquals(2, viewModel.languages.size)
    Assert.assertEquals("English", viewModel.languages[0].displayName)
  }

  @Test
  fun testUpdateFilterValueShouldUpdateGlobalFilter() {
    viewModel.updateFilterValue(RegisterFilterType.OVERDUE_FILTER, true)

    Assert.assertEquals(RegisterFilterType.OVERDUE_FILTER, viewModel.filterValue.value?.first)
    Assert.assertTrue(viewModel.filterValue.value?.second as Boolean)
  }

  @Test
  fun testSetRefreshRegisterDataShouldUpdateGlobalRegisterDate() {
    viewModel.setRefreshRegisterData(true)
    Assert.assertTrue(viewModel.refreshRegisterData.value!!)
  }

  @Test
  fun testSetLastSyncTimestampShouldUpdateGlobalSyncTimestamp() {
    viewModel.setLastSyncTimestamp("12345")
    Assert.assertEquals("12345", viewModel.lastSyncTimestamp.value)
  }
}