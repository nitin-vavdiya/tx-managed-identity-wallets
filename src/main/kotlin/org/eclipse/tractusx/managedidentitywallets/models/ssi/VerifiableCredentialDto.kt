/********************************************************************************
 * Copyright (c) 2021,2022 Contributors to the CatenaX (ng) GitHub Organisation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

@file:UseSerializers(AnySerializer::class)

package org.eclipse.tractusx.managedidentitywallets.models.ssi

import com.fasterxml.jackson.annotation.JsonProperty
import io.bkbn.kompendium.annotations.Field
import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.eclipse.tractusx.managedidentitywallets.plugins.AnySerializer

@Serializable
data class VerifiableCredentialDto(
    @Field(description = "The ID of credential as String (URI compatible)", name = "id")
    val id: String? = null,
    @JsonProperty("@context") @SerialName("@context")
    @Field(description = "List of Contexts", name = "@context")
    val context: List<String>,
    @Field(description = "List of Types", name = "type")
    val type: List<String>,
    @Field(description = "The DID of Issuer as String (URI compatible)", name = "issuer")
    val issuer: String,
    @Field(description = "The Issuance Date as String in Rfc3339 format", name = "issuanceDate")
    var issuanceDate: String, // In Rfc3339
    @Field(description = "The Expiration Date as String in Rfc3339 format", name = "expirationDate")
    val expirationDate: String? = null, // In Rfc3339
    @Field(description = "The Credential Subject including the DID of the Subject", name = "credentialSubject")
    val credentialSubject: Map<String, Any>,
    @Field(description = "The Credential Status if the credential is revocable", name = "credentialStatus")
    val credentialStatus: CredentialStatus? = null,
    @Field(description = "The Proof generated by the Issuer for issued Credentials", name = "proof")
    val proof: LdProofDto? = null
)

@Serializable
data class VerifiableCredentialParameters(
    @Param(type = ParamType.QUERY)
    @Field(description = "The ID of the Credential as String (URI compatible)", name = "id")
    val id: String,
    @Param(type = ParamType.QUERY)
    @Field(description = "The list of credential types", name = "type")
    val type: List<String>,
    @Param(type = ParamType.QUERY)
    @Field(description = "The DID or BPN of Issuer as String", name = "issuerIdentifier")
    val issuerIdentifier: String,
    @Param(type = ParamType.QUERY)
    @Field(description = "The DID or BPN of holder as String", name = "holderIdentifier")
    val holderIdentifier: String
)

@Serializable
data class IssuedVerifiableCredentialRequestDto(
    @Field(description = "The ID of credential as String (URI compatible)", name = "id")
    val id: String? = null,
    @JsonProperty("@context") @SerialName("@context")
    @Field(description = "List of Contexts", name = "@context")
    val context: List<String>,
    @Field(description = "List of Types", name = "type")
    val type: List<String>,
    @Field(description = "The DID of Issuer", name = "issuer")
    val issuer: String,
    @Field(description = "The Issuance Date as String in Rfc3339 format", name = "issuanceDate")
    var issuanceDate: String, // In Rfc3339
    @Field(description = "The Expiration Date as String in Rfc3339 format", name = "expirationDate")
    val expirationDate: String? = null, // In Rfc3339
    @Field(description = "The Credential Subject including the DID of the Subject", name = "credentialSubject")
    val credentialSubject: Map<String, Any>,
    @Field(description = "The Credential Status including the revocation related Information", name = "CredentialStatus")
    val credentialStatus: CredentialStatus?,
    @Field(description = "The Proof generated by the Issuer", name = "proof")
    val proof: LdProofDto
)

@Serializable
data class VerifiableCredentialRequestDto(
    @Field(description = "The ID of credential as String (URI compatible)", name = "id")
    val id: String? = null,
    @JsonProperty("@context") @SerialName("@context")
    @Field(description = "List of Contexts", name = "@context")
    val context: List<String>,
    @Field(description = "List of Credential Types", name = "type")
    val type: List<String>,
    @Field(description = "The DID or BPN of Issuer", name = "issuerIdentifier")
    val issuerIdentifier: String,
    @Field(description = "The Issuance Date as String in Rfc3339 format, if null, the current time is used", name = "issuanceDate")
    var issuanceDate: String?, // In Rfc3339
    @Field(description = "The Expiration Date as String in Rfc3339 format", name = "expirationDate")
    val expirationDate: String? = null, // In Rfc3339
    @Field(description = "The Credential Subject representing the payload", name = "credentialSubject")
    val credentialSubject: Map<String, Any>,
    @Field(description = "The Identifier of the holder, It Should be a DID or BPN. " +
            "This value will be ignored if the credential subject has an id, " +
            "otherwise it will be set as the id attribute in the credential subject",
        name = "holderIdentifier")
    val holderIdentifier: String?= null,
    @Field(description = "Flag whether credential is revocable or not. Default true", name = "isRevocable")
    val isRevocable: Boolean = true
)

@Serializable
data class VerifiableCredentialRequestWithoutIssuerDto(
    @Field(description = "The ID of credential as String (URI compatible)", name = "id")
    val id: String? = null,
    @JsonProperty("@context") @SerialName("@context")
    @Field(description = "List of Contexts", name = "@context")
    val context: List<String>,
    @Field(description = "List of Credential Types", name = "type")
    val type: List<String>,
    @Field(description = "The Issuance Date as String in Rfc3339 format, if null, the current time is used", name = "issuanceDate")
    var issuanceDate: String?, // In Rfc3339
    @Field(description = "The Expiration Date as String in Rfc3339 format", name = "expirationDate")
    val expirationDate: String? = null, // In Rfc3339
    @Field(description = "The Credential Subject representing the payload", name = "credentialSubject")
    val credentialSubject: Map<String, Any>,
    @Field(description = "The DID or BPN of holder, the DID will be automatically set as the id attribute in the credential subject",
        name = "holderIdentifier")
    val holderIdentifier: String,
    @Field(description = "Flag whether credential is revocable or not. Default true", name = "isRevocable")
    val isRevocable: Boolean = true,
    @Field(description = "url to be used as webhook. currently only supported for self managed wallet", name = "webhookUrl")
    val webhookUrl: String? = null
)

@Serializable
data class CredentialStatus (
    @SerialName("id") @JsonProperty("id") var statusId: String,
    @SerialName("type") @JsonProperty("type")  var credentialType: String = "StatusList2021Entry",
    @SerialName("statusPurpose") @JsonProperty("statusPurpose") var statusPurpose: String = "revocation",
    @SerialName("statusListIndex") @JsonProperty("statusListIndex") var index: String,
    @SerialName("statusListCredential") @JsonProperty("statusListCredential") var listUrl: String,
) {
    companion object {
        const val CREDENTIAL_TYPE = "StatusList2021Entry"
        const val STATUS_PURPOSE = "revocation"
    }
}

@Serializable
data class ListCredentialRequestData (
    @SerialName("id") @JsonProperty("id") var listId: String? = null,
    var subject: ListCredentialSubject
)

@Serializable
data class ListCredentialSubject (
    @SerialName("id") @JsonProperty("id") var credentialId: String,
    @SerialName("type") @JsonProperty("type") var credentialType: String = "StatusList2021",
    var statusPurpose: String = "revocation",
    var encodedList: String
) {
    companion object {
        const val CREDENTIAL_TYPE_KEY = "credentialType"
        const val CREDENTIAL_TYPE = "StatusList2021"
        const val STATUS_PURPOSE_KEY = "statusPurpose"
        const val STATUS_PURPOSE = "revocation"
        const val ENCODED_LIST_KEY = "encodedList"
    }
}

@Serializable
data class ListNameParameter(
    @Param(type = ParamType.QUERY)
    @Field(description = "The revocation listName", name = "listName")
    val listName: String,
)

@Serializable
data class StatusListRefreshParameters(
    @Param(type = ParamType.QUERY)
    @Field(description = "The DID or BPN of the wallet whose revocation list should be refreshed", name = "identifier")
    val identifier: String? = null,
    @Param(type = ParamType.QUERY)
    @Field(description = "Force an update. Default is false", name = "force")
    val force: Boolean? = false,
)
