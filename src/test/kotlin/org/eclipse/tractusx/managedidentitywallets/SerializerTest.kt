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

package org.eclipse.tractusx.managedidentitywallets

import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.eclipse.tractusx.managedidentitywallets.models.NameResponse
import org.eclipse.tractusx.managedidentitywallets.models.TypeKeyNameDto
import org.eclipse.tractusx.managedidentitywallets.models.TypeKeyNameUrlDto
import org.eclipse.tractusx.managedidentitywallets.models.ssi.CredentialStatus
import org.eclipse.tractusx.managedidentitywallets.models.ssi.LdProofDto
import org.eclipse.tractusx.managedidentitywallets.models.ssi.VerifiableCredentialDto
import org.eclipse.tractusx.managedidentitywallets.plugins.VerifiableCredentialDtoSerializer
import org.eclipse.tractusx.managedidentitywallets.plugins.configureSerialization
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@kotlinx.serialization.ExperimentalSerializationApi
class SerializerTest {

    private val server = TestServer().initServer()

    @BeforeTest
    fun setup() {
        server.start()
    }

    @AfterTest
    fun tearDown() {
        SingletonTestData.cleanSingletonTestData()
        server.stop(1000, 10000)
    }

    @Test
    fun testSerializer() {
        withTestApplication({
            EnvironmentTestSetup.setupEnvironment(environment)
            configureSerialization()
        }) {

            val vcString = """
                {
                "id":null,
                "@context":["https://www.w3.org/2018/credentials/v1"],
                "type":["VerifiableCredential"],
                "issuer":"did:sov:issuer",
                "issuanceDate":"2022-01-01T12:00:00Z",
                "expirationDate":"2026-01-01T12:00:00Z",
                "credentialSubject":{"name":"John Doe","degree":"Bachelor of Science"},
                "credentialStatus":{
                    "id": "d",
                    "type":"t",
                    "statusPurpose":"revocation",
                    "statusListIndex":"c",
                    "statusListCredential":"f"
                },
                  "proof": {
                    "type": "Ed25519Signature2018",
                    "verificationMethod": "did:key:z6MkjqLvFvF9kdKt1798NxRUbPoH1t3iEUQxjjUz8cQ6R927#z6MkjqLvFvF9kdKt1798NxRUbPoH1t3iEUQxjjUz8cQ6R927",
                    "created": "2023-02-03T12:05:56.218624+00:00",
                    "proofPurpose": "tt",
                    "proofValue": null,
                    "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..Z_hc80gPEOW40yAa58PrzH9q-KGzbhdVPjD6-zkh9xnkPi05d_SPxkM-cKUS0ql2xnOabfjaYSAWHiD7TQ5GBQ"
                  },
                "keyString":"TestString",
                "keyObject":{"bpn":"s","did":"s","isSelfManaged":false,"name":"ss","pendingMembershipIssuance":true,"revocationListName":"e","vcs":[]},
                "keyList":["TestElement1","TestElement2"]
                }
            """.trimIndent()

            val json = Json {
                prettyPrint = false
                isLenient = false
                encodeDefaults = true
                classDiscriminator = "class"
                serializersModule = KompendiumSerializersModule.module
                explicitNulls = false
                ignoreUnknownKeys = true
            }
            val decodedCredential: VerifiableCredentialDto = json.decodeFromString(VerifiableCredentialDtoSerializer, vcString)
            assertEquals(decodedCredential.id, "null")
            assertEquals((decodedCredential.properties?.get("keyList") as List<String>).size, 2)

            val verifiableCredentialDto = VerifiableCredentialDto(
                "credential-id", listOf("https://www.w3.org/2018/credentials/v1"),
                listOf("VerifiableCredential", "TestTypeCredential"),
                "did:sov:issuer",
                "2022-01-01T12:00:00Z",
                "2026-01-01T12:00:00Z",
                mapOf("name" to "John Doe","degree" to "Bachelor of Science"),
                CredentialStatus("id", "t", "revocation", "c", "f"),
                LdProofDto(
                    type = "Ed25519Signature2018",
                    created = "2023-02-03T12:05:56.218624+00:00",
                    verificationMethod= "did:key:z6MkjqLvFvF9kdKt1798NxRUbPoH1t3iEUQxjjUz8cQ6R927#z6MkjqLvFvF9kdKt1798NxRUbPoH1t3iEUQxjjUz8cQ6R927",
                    proofPurpose="assertionMethod",
                    jws="eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0",
                    proofValue = null
                ),
                mapOf(
                   "keyObject" to NameResponse("value1", null,
                       TypeKeyNameUrlDto("key", "name", null),
                       TypeKeyNameDto("key2", "name2")
                   ),
                    "keyString" to "value2",
                    "keyList" to listOf("OK", "MOK")
                )
            )

            val encodedString: String = json.encodeToString(VerifiableCredentialDtoSerializer, verifiableCredentialDto)
            println( "GGGGGG")
            println(encodedString)
            println( "GGGGGG")
            assertTrue { encodedString.trimIndent().contains("""
                 "id":"credential-id"
            """.trimIndent()) }
            assertTrue { encodedString.trimIndent().contains("""
                 "@context":["https://www.w3.org/2018/credentials/v1"]
            """.trimIndent()) }
            assertTrue { encodedString.trimIndent().contains("""
                 "type":["VerifiableCredential","TestTypeCredential"]
            """.trimIndent()) }
            assertTrue { encodedString.trimIndent().contains("""
                 "keyList":["OK","MOK"]
            """.trimIndent()) }
        }
    }

}
