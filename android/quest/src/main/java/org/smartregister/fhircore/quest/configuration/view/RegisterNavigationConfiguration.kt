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

package org.smartregister.fhircore.quest.configuration.view

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.smartregister.fhircore.engine.configuration.Configuration

@Serializable sealed class NavigationAction

@Serializable
@SerialName("questionnaire")
data class QuestionnaireNavigationAction(val form: String, val readOnly: Boolean) :
  NavigationAction()

@Serializable
@SerialName("questionnaire_data_details")
data class QuestionnaireDataDetailsNavigationAction(val classification: String) :
  NavigationAction()

@Serializable
class NavigationConfiguration(
  override val appId: String,
  override val classification: String,
  val navigationOptions: List<NavigationOption>
) : Configuration

@Serializable
data class NavigationOption(
  val id: String,
  val title: String,
  val icon: String,
  val action: NavigationAction
)