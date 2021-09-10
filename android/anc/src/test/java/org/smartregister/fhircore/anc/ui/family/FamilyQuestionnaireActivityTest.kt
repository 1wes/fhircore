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

package org.smartregister.fhircore.anc.ui.family

import android.app.Activity
import android.content.Intent
import com.google.android.fhir.sync.Sync
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.spyk
import io.mockk.unmockkObject
import java.time.OffsetDateTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import org.smartregister.fhircore.anc.AncApplication
import org.smartregister.fhircore.anc.activity.ActivityRobolectricTest
import org.smartregister.fhircore.anc.data.FamilyRepository
import org.smartregister.fhircore.anc.shadow.AncApplicationShadow
import org.smartregister.fhircore.anc.shadow.FakeKeyStore
import org.smartregister.fhircore.anc.ui.family.form.FamilyQuestionnaireActivity
import org.smartregister.fhircore.engine.ui.questionnaire.QuestionnaireActivity.Companion.QUESTIONNAIRE_ARG_FORM

@Config(shadows = [AncApplicationShadow::class])
internal class FamilyQuestionnaireActivityTest : ActivityRobolectricTest() {

  private lateinit var familyQuestionnaireActivity: FamilyQuestionnaireActivity

  private lateinit var familyQuestionnaireActivitySpy: FamilyQuestionnaireActivity

  @Before
  fun setUp() {
    mockkObject(Sync)
    every { Sync.basicSyncJob(any()).stateFlow() } returns flowOf()
    every { Sync.basicSyncJob(any()).lastSyncTimestamp() } returns OffsetDateTime.now()

    val intent = Intent().apply { putExtra(QUESTIONNAIRE_ARG_FORM, "family-member-registration") }

    familyQuestionnaireActivity =
      Robolectric.buildActivity(FamilyQuestionnaireActivity::class.java, intent).create().get()
    familyQuestionnaireActivitySpy = spyk(objToCopy = familyQuestionnaireActivity)
  }

  @After
  fun cleanup() {
    unmockkObject(Sync)
  }

  @Test
  fun testActivityShouldNotNull() {
    assertNotNull(familyQuestionnaireActivity)
  }

  @Test
  fun testHandleFamilyMemberRegistrationShouldCallPostProcessFamilyMember() {
    val familyRepository = mockk<FamilyRepository>()
    coEvery { familyRepository.postProcessFamilyMember(any(), any()) } just runs

    runBlocking {
      AncApplication.getContext().fhirEngine.save(Questionnaire().apply { id = "1832" })
    }

    familyQuestionnaireActivity.familyRepository = familyRepository

    ReflectionHelpers.setField(familyQuestionnaireActivity, "questionnaire", Questionnaire())

    familyQuestionnaireActivity.handleQuestionnaireResponse(QuestionnaireResponse())

    coVerify(timeout = 2000) { familyRepository.postProcessFamilyMember(any(), any()) }
  }

  override fun getActivity(): Activity {
    return familyQuestionnaireActivity
  }

  companion object {
    @JvmStatic
    @BeforeClass
    fun beforeClass() {
      FakeKeyStore.setup
    }
  }
}