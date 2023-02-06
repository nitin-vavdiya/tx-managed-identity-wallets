/********************************************************************************
 * Copyright (c) 2021,2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.managedidentitywallets.plugins

import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.descriptors.setSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.eclipse.tractusx.managedidentitywallets.models.ssi.CredentialStatus
import org.eclipse.tractusx.managedidentitywallets.models.ssi.IssuedVerifiableCredentialRequestDto
import org.eclipse.tractusx.managedidentitywallets.models.ssi.LdProofDto
import org.eclipse.tractusx.managedidentitywallets.models.ssi.VerifiableCredentialDto
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties


object LocalDateTimeAsStringSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDateTime = LocalDateTime.parse(decoder.decodeString())
}

// https://github.com/Kotlin/kotlinx.serialization/issues/746#issuecomment-779549456
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any")
    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as JsonEncoder
        val jsonElement = serializeAny(value)
        jsonEncoder.encodeJsonElement(jsonElement)
    }

    private fun serializeAny(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        is Map<*, *> -> {
            val mapContents = value.entries.associate { mapEntry ->
                mapEntry.key.toString() to serializeAny(mapEntry.value)
            }
            JsonObject(mapContents)
        }
        is List<*> -> {
            val arrayContents = value.map { listEntry -> serializeAny(listEntry) }
            JsonArray(arrayContents)
        }
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is String ->  JsonPrimitive(value)
        else -> {
            val contents = value::class.memberProperties.associate { property ->
                property.name to serializeAny(property.getter.call(value))
            }
            JsonObject(contents)
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()

        return deserializeJsonElement(element)
    }

    fun deserializeJsonElement(element: JsonElement): Any = when (element) {
        is JsonObject -> {
            element.mapValues { deserializeJsonElement(it.value) }
        }
        is JsonArray -> {
            element.map { deserializeJsonElement(it) }
        }
        is JsonPrimitive  -> {
            if (element.isString) {
               element.content
           } else {
               try {
                    element.boolean
                } catch (e: Throwable) {
                    try {
                        element.long
                    } catch (e: Throwable) {
                        element.double
                    }
                }
            }
        }
    }
}

@Serializer(forClass = VerifiableCredentialDto::class)
object VerifiableCredentialDtoSerializer : KSerializer<VerifiableCredentialDto> {
    private val stringToJsonElementSerializer = MapSerializer(String.serializer(), JsonElement.serializer())
    override val descriptor: SerialDescriptor = stringToJsonElementSerializer.descriptor

    override fun serialize(encoder: Encoder, obj: VerifiableCredentialDto) {
        val descriptorX: SerialDescriptor =
            buildClassSerialDescriptor("VerifiableCredentialDtoSerializer") {
                element("id", String.serializer().descriptor, isOptional = true)
                element("@context", ListSerializer(String.serializer()).descriptor)
                element("type", ListSerializer(String.serializer()).descriptor)
                element("issuer", String.serializer().descriptor)
                element("issuanceDate", String.serializer().descriptor)
                element("expirationDate", String.serializer().descriptor, isOptional = true)
                element("credentialSubject", MapSerializer(String.serializer(), AnySerializer).descriptor)
                element("credentialStatus", CredentialStatus.serializer().descriptor, isOptional = true)
                element("proof", LdProofDto.serializer().descriptor, isOptional = true)
                obj.properties?.forEach {(k, v) ->
                    element(k, LdProofDto.serializer().descriptor, isOptional = true)
                }
            }
        val out = encoder.beginStructure(descriptorX)
        obj.id?.let { out.encodeStringElement(descriptorX, 0, it) }
        out.encodeSerializableElement(descriptorX, 1, ListSerializer(String.serializer()), obj.context)
        out.encodeSerializableElement(descriptorX, 2, ListSerializer(String.serializer()), obj.type)
        out.encodeStringElement(descriptorX, 3, obj.issuer)
        out.encodeStringElement(descriptorX, 4, obj.issuanceDate)
        obj.expirationDate?.let { out.encodeStringElement(descriptorX, 5, it) }
        out.encodeSerializableElement(
            descriptorX, 6, MapSerializer(String.serializer(), AnySerializer), obj.credentialSubject
        )
        obj.credentialStatus?.let { out.encodeSerializableElement(descriptorX, 7, CredentialStatus.serializer(), it) }
        obj.proof?.let { out.encodeSerializableElement(descriptorX, 8, LdProofDto.serializer(), it) }
        var ind = 0
        obj.properties?.forEach {(k, v) ->
            out.encodeSerializableElement(descriptorX, 9 + ind, AnySerializer , v)
            ind++
        }
        out.endStructure(descriptorX)
    }

    override fun deserialize(decoder: Decoder): VerifiableCredentialDto {

        var id: String? = null
        var context: List<String> = emptyList()
        var type: List<String> = emptyList()
        var issuer = ""
        var issuanceDate = ""
        var expirationDate: String? = null
        var credentialSubject: Map<String, Any> = emptyMap()
        var credentialStatus: CredentialStatus? = null
        var proof: LdProofDto? = null
        var properties: MutableMap<String, Any> = mutableMapOf()

        // Decoder -> JsonInput
        require(decoder is JsonDecoder) // this class can be decoded only by Json
        val json = decoder.json
        val filtersMap = decoder.decodeJsonElement().jsonObject

        filtersMap.entries.forEach {
            when (it.key) {
                "id" -> id = it.value.jsonPrimitive.content
                "@context" ->  context = AnySerializer.deserializeJsonElement(it.value) as List<String>
                "type" -> type = AnySerializer.deserializeJsonElement(it.value) as List<String>
                "issuer" -> issuer =  it.value.jsonPrimitive.content
                "issuanceDate" -> issuanceDate = it.value.jsonPrimitive.content
                "expirationDate" ->  expirationDate = it.value.jsonPrimitive.content
                "credentialSubject" ->  credentialSubject = AnySerializer.deserializeJsonElement(it.value) as Map<String, Any>
                "credentialStatus" -> credentialStatus = convertJsonObjectToCredentialStatus(it.value)
                "proof" ->  proof = convertJsonObjectToLDProof(it.value)
                else -> {
                    val s = AnySerializer.deserializeJsonElement(it.value)
                    properties[it.key] = s
                    println( it.key +  " "  + it.value.toString())
                }
            }
        }
        val propertiesToAdd: MutableMap<String, Any>? = if (properties.isNullOrEmpty()) {
            null
        } else {
            properties
        }
        return VerifiableCredentialDto(
            id,
            context,
            type,
            issuer,
            issuanceDate,
            expirationDate,
            credentialSubject,
            credentialStatus,
            proof,
            propertiesToAdd
        )
    }
}

@Serializer(forClass = IssuedVerifiableCredentialRequestDto::class)
object IssuedVerifiableCredentialRequestDtoSerializer : KSerializer<IssuedVerifiableCredentialRequestDto> {
    override val descriptor: SerialDescriptor = MapSerializer(String.serializer(), JsonElement.serializer()).descriptor
    override fun serialize(encoder: Encoder, obj: IssuedVerifiableCredentialRequestDto) {
        val descriptorX: SerialDescriptor =
            buildClassSerialDescriptor("IssuedVerifiableCredentialDtoSerializer") {
                element("id", String.serializer().descriptor, isOptional = true)
                element("@context", ListSerializer(String.serializer()).descriptor)
                element("type", ListSerializer(String.serializer()).descriptor)
                element("issuer", String.serializer().descriptor)
                element("issuanceDate", String.serializer().descriptor)
                element("expirationDate", String.serializer().descriptor, isOptional = true)
                element("credentialSubject", MapSerializer(String.serializer(), AnySerializer).descriptor)
                element("credentialStatus", CredentialStatus.serializer().descriptor, isOptional = true)
                element("proof", LdProofDto.serializer().descriptor, isOptional = false)
                obj.properties?.forEach {(k, v) ->
                    element(k, LdProofDto.serializer().descriptor, isOptional = true)
                }
            }
        val out = encoder.beginStructure(descriptorX)
        obj.id?.let { out.encodeStringElement(descriptorX, 0, it) }
        out.encodeSerializableElement(descriptorX, 1, ListSerializer(String.serializer()), obj.context)
        out.encodeSerializableElement(descriptorX, 2, ListSerializer(String.serializer()), obj.type)
        out.encodeStringElement(descriptorX, 3, obj.issuer)
        out.encodeStringElement(descriptorX, 4, obj.issuanceDate)
        obj.expirationDate?.let { out.encodeStringElement(descriptorX, 5, it) }
        out.encodeSerializableElement(
            descriptorX, 6, MapSerializer(String.serializer(), AnySerializer), obj.credentialSubject
        )
        obj.credentialStatus?.let { out.encodeSerializableElement(descriptorX, 7, CredentialStatus.serializer(), it) }
        out.encodeSerializableElement(descriptorX, 8, LdProofDto.serializer(), obj.proof)
        var ind = 0
        obj.properties?.forEach {(k, v) ->
            out.encodeSerializableElement(descriptorX, 9 + ind, AnySerializer , v)
            ind++
        }
        out.endStructure(descriptorX)
    }

    override fun deserialize(decoder: Decoder): IssuedVerifiableCredentialRequestDto {

        var id: String? = null
        var context: List<String> = emptyList()
        var type: List<String> = emptyList()
        var issuer = ""
        var issuanceDate = ""
        var expirationDate: String? = null
        var credentialSubject: Map<String, Any> = emptyMap()
        var credentialStatus: CredentialStatus? = null
        var proof: LdProofDto = LdProofDto("", "", "", "")
        var properties: MutableMap<String, Any> = mutableMapOf()

        // Decoder -> JsonInput
        require(decoder is JsonDecoder) // this class can be decoded only by Json
        val json = decoder.json
        val filtersMap = decoder.decodeJsonElement().jsonObject

        filtersMap.entries.forEach {
            when (it.key) {
                "id" -> id = it.value.jsonPrimitive.content
                "@context" ->  context = AnySerializer.deserializeJsonElement(it.value) as List<String>
                "type" -> type = AnySerializer.deserializeJsonElement(it.value) as List<String>
                "issuer" -> issuer =  it.value.jsonPrimitive.content
                "issuanceDate" -> issuanceDate = it.value.jsonPrimitive.content
                "expirationDate" ->  expirationDate = it.value.jsonPrimitive.content
                "credentialSubject" ->  credentialSubject = AnySerializer.deserializeJsonElement(it.value) as Map<String, Any>
                "credentialStatus" -> credentialStatus = convertJsonObjectToCredentialStatus(it.value)
                "proof" ->  proof = convertJsonObjectToLDProof(it.value)
                else -> {
                    val s = AnySerializer.deserializeJsonElement(it.value)
                    properties[it.key] = s
                    println( it.key +  " "  + it.value.toString())
                }
            }
        }
        val propertiesToAdd: MutableMap<String, Any>? = if (properties.isNullOrEmpty()) {
            null
        } else {
            properties
        }
        return IssuedVerifiableCredentialRequestDto(
            id,
            context,
            type,
            issuer,
            issuanceDate,
            expirationDate,
            credentialSubject,
            credentialStatus,
            proof,
            propertiesToAdd
        )
    }
}

fun convertJsonObjectToCredentialStatus(json: JsonElement): CredentialStatus {
    var statusId = ""
    var credentialType = ""
    var statusPurpose = ""
    var index = ""
    var listUrl = ""
    json.jsonObject.entries.forEach {
        println("OK!" + it.key)
        when(it.key) {
            "id" -> statusId = it.value.jsonPrimitive.content
            "type" -> credentialType = it.value.jsonPrimitive.content
            "statusPurpose" -> statusPurpose = it.value.jsonPrimitive.content
            "statusListIndex" -> index = it.value.jsonPrimitive.content
            "statusListCredential" -> listUrl = it.value.jsonPrimitive.content
        }
    }
    return CredentialStatus(
        statusId, credentialType, statusPurpose, index, listUrl
    )
}

fun convertJsonObjectToLDProof(json: JsonElement): LdProofDto {
    var type = ""
    var created = ""
    var proofPurpose = ""
    var verificationMethod = ""
    var jws: String? = null
    var proofValue: String?  = null
    var creator: String? = null
    var domain: String? = null
    var challenge: String? = null
    var nonce: String? =   null
    json.jsonObject.entries.forEach {
        when(it.key) {
            "type" -> type = it.value.jsonPrimitive.content
            "created" -> created = it.value.jsonPrimitive.content
            "proofPurpose" -> proofPurpose = it.value.jsonPrimitive.content
            "verificationMethod" -> verificationMethod = it.value.jsonPrimitive.content
            "jws" -> jws = it.value.jsonPrimitive.content
            "proofValue" -> proofValue = it.value.jsonPrimitive.content
            "creator" -> creator = it.value.jsonPrimitive.content
            "domain" -> domain = it.value.jsonPrimitive.content
            "challenge" -> challenge = it.value.jsonPrimitive.content
            "nonce" -> nonce = it.value.jsonPrimitive.content
        }
    }
    return LdProofDto(
          type ,created ,proofPurpose ,verificationMethod, jws, proofValue,creator, domain , challenge, nonce
    )
}

@ExperimentalSerializationApi
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = false
            encodeDefaults = true

            // useArrayPolymorphism  = true
            classDiscriminator = "class"
            serializersModule = KompendiumSerializersModule.module
            explicitNulls = false

            // currently needed for business partner data update
            ignoreUnknownKeys = true
        })
    }
}
