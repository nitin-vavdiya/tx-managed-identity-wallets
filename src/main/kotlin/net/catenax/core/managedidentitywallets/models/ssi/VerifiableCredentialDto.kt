@file:UseSerializers(AnySerializer::class)

package net.catenax.core.managedidentitywallets.models.ssi

import com.fasterxml.jackson.annotation.JsonProperty
import io.bkbn.kompendium.annotations.Field
import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.catenax.core.managedidentitywallets.plugins.AnySerializer

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
    @Field(description = "The DID or BPN of holder, the DID will be automatically set as the id attribute in the credential subject",
        name = "holderIdentifier")
    val holderIdentifier: String,
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
)