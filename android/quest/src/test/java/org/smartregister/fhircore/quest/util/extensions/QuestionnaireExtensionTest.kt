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

package org.smartregister.fhircore.quest.util.extensions

import android.content.Intent
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StringType
import org.junit.Assert
import org.junit.Test
import org.smartregister.fhircore.engine.configuration.QuestionnaireConfig
import org.smartregister.fhircore.engine.util.extension.FieldType
import org.smartregister.fhircore.engine.util.extension.asLabel
import org.smartregister.fhircore.engine.util.extension.cqfLibraryIds
import org.smartregister.fhircore.engine.util.extension.find
import org.smartregister.fhircore.engine.util.extension.isExtractionCandidate
import org.smartregister.fhircore.engine.util.extension.prepareQuestionsForReadingOrEditing
import org.smartregister.fhircore.quest.HiltActivityForTest
import org.smartregister.fhircore.quest.robolectric.RobolectricTest
import org.smartregister.fhircore.quest.ui.questionnaire.QuestQuestionnaireFragment
import org.smartregister.fhircore.quest.ui.questionnaire.QuestionnaireActivity

class QuestionnaireExtensionTest : RobolectricTest() {
  @Test
  fun testIsExtractionCandidateShouldVerifyAllScenarios() {
    Assert.assertFalse(Questionnaire().isExtractionCandidate())

    Assert.assertTrue(
      Questionnaire()
        .apply {
          addExtension().apply {
            url =
              "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-targetStructureMap"
            setValue(CanonicalType("test"))
          }
        }
        .isExtractionCandidate()
    )

    Assert.assertTrue(
      Questionnaire()
        .apply {
          addExtension().apply {
            url =
              "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-itemExtractionContext"
          }
        }
        .isExtractionCandidate()
    )
  }

  @Test
  fun testCqfLibraryIdShouldReturnExpectedUrl() {
    Assert.assertTrue(Questionnaire().cqfLibraryIds().isEmpty())

    Assert.assertEquals(
      "",
      Questionnaire()
        .apply {
          addExtension().apply {
            url = "cqf-library"
            setValue(StringType("Library/"))
          }
        }
        .cqfLibraryIds()
        .first()
    )

    Assert.assertEquals(
      "112233",
      Questionnaire()
        .apply {
          addExtension().apply {
            url = "cqf-library"
            setValue(StringType("Library/112233"))
          }
        }
        .cqfLibraryIds()
        .first()
    )
  }

  @Test
  fun testShouldFindMatchingItems() {

    val questionnaire =
      Questionnaire().apply {
        addItem().apply {
          linkId = "family"
          addInitial(Questionnaire.QuestionnaireItemInitialComponent(StringType("Mr")))
        }
      }

    Assert.assertEquals("Mr", questionnaire.find("family")?.initialFirstRep?.valueStringType?.value)

    questionnaire.find("family")?.addItem()?.apply {
      linkId = "name"
      addInitial(Questionnaire.QuestionnaireItemInitialComponent(StringType("John")))
    }

    Assert.assertEquals("John", questionnaire.find("name")?.initialFirstRep?.valueStringType?.value)
  }

  @Test
  fun testQuestionnaireResponseItemComponentAsLabel() {
    val item = QuestionnaireResponse().addItem().apply { linkId = "my_test_link" }

    Assert.assertEquals("My test link: ", item.asLabel())
  }

  @Test
  fun testShouldFindMatchingItemsByFieldType() {

    val questionnaire =
      Questionnaire().apply {
        id = "12345"
        item =
          listOf(
            Questionnaire.QuestionnaireItemComponent().apply {
              type = Questionnaire.QuestionnaireItemType.CHOICE
              linkId = "q1-gender"
              definition = "some-element-definition-identifier"
            },
            Questionnaire.QuestionnaireItemComponent().apply {
              type = Questionnaire.QuestionnaireItemType.CHOICE
              linkId = "q2-marital-status"
            },
            Questionnaire.QuestionnaireItemComponent().apply {
              type = Questionnaire.QuestionnaireItemType.DATE
              linkId = "q3-date"
            }
          )
      }

    Assert.assertEquals(3, questionnaire.item.size)

    val filtered =
      questionnaire.find(FieldType.TYPE, Questionnaire.QuestionnaireItemType.CHOICE.name)

    Assert.assertEquals(2, filtered.size)

    val filteredDates =
      questionnaire.find(FieldType.TYPE, Questionnaire.QuestionnaireItemType.DATE.name)

    Assert.assertEquals(1, filteredDates.size)

    val filteredDefinitions =
      questionnaire.find(FieldType.DEFINITION, "some-element-definition-identifier")

    Assert.assertEquals(1, filteredDefinitions.size)
  }

  @Test
  fun `Questionnaire#prepareQuestionsForReadingOrEditing should retain custom extension`() {
    val questionnaire = mutableListOf<Questionnaire.QuestionnaireItemComponent>()
    questionnaire.add(
      Questionnaire.QuestionnaireItemComponent().apply {
        text = "Group"
        type = Questionnaire.QuestionnaireItemType.GROUP
      }
    )
    questionnaire.add(
      Questionnaire.QuestionnaireItemComponent().apply {
        prefix = "1."
        text = "Photo of device"
        readOnly = false
        addExtension(
          Extension().apply {
            url = QuestQuestionnaireFragment.PHOTO_CAPTURE_URL
            setValue(StringType().apply { value = QuestQuestionnaireFragment.PHOTO_CAPTURE_NAME })
          }
        )
      }
    )
    questionnaire.add(
      Questionnaire.QuestionnaireItemComponent().apply {
        prefix = "2."
        text = "Barcode"
        readOnly = false
        addExtension(
          Extension().apply {
            url = QuestQuestionnaireFragment.BARCODE_URL
            setValue(StringType().apply { value = QuestQuestionnaireFragment.BARCODE_NAME })
          }
        )
      }
    )

    questionnaire.prepareQuestionsForReadingOrEditing("path", true)

    Assert.assertTrue(questionnaire[1].hasExtension(QuestQuestionnaireFragment.PHOTO_CAPTURE_URL))
    Assert.assertTrue(questionnaire[1].readOnly)
    Assert.assertTrue(questionnaire[2].hasExtension(QuestQuestionnaireFragment.BARCODE_URL))
    Assert.assertTrue(questionnaire[2].readOnly)
  }

  @Test
  fun testLaunchQuestionnaireLaunchesIntentWithCorrectValues() {
    val activity = mockk<HiltActivityForTest>(relaxed = true)
    //    every { activity.getActivity() } returns activity

    val computedValuesMap: Map<String, Any> = mapOf(Pair("firstName", "John"))
    val questionnaireConfig = QuestionnaireConfig(id = "remove_family")

    activity.launchQuestionnaire<QuestionnaireActivity>(
      questionnaireConfig = questionnaireConfig,
      computedValuesMap = computedValuesMap
    )

    val intentSlot = slot<Intent>()
    val intSlot = slot<Int>()
    verify { activity.startActivityForResult(capture(intentSlot), capture(intSlot)) }

    Assert.assertEquals(0, intSlot.captured)
    val capturedQuestionnaireConfig =
      intentSlot.captured.getSerializableExtra(QuestionnaireActivity.QUESTIONNAIRE_CONFIG) as
        QuestionnaireConfig

    Assert.assertEquals("remove_family", capturedQuestionnaireConfig.id)

    val capturedComputedValuesMap =
      intentSlot.captured.getSerializableExtra(
        QuestionnaireActivity.QUESTIONNAIRE_COMPUTED_VALUES_MAP
      ) as
        Map<String, Any>
    Assert.assertEquals("John", capturedComputedValuesMap["firstName"])
  }
}