/*
 * *******************************************************************************
 *  Copyright (c) 2021,2023 Contributors to the Eclipse Foundation
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ******************************************************************************
 */

package org.eclipse.tractusx.managedidentitywallets.utils;

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.dao.entity.HoldersCredential;
import org.eclipse.tractusx.ssi.lib.crypt.IPrivateKey;
import org.eclipse.tractusx.ssi.lib.model.did.DidDocument;
import org.eclipse.tractusx.ssi.lib.model.proof.jws.JWSSignature2020;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofGenerator;
import org.eclipse.tractusx.ssi.lib.proof.SignatureType;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;

/**
 * The type Common utils.
 */
public class CommonUtils {

    private CommonUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets identifier type.
     *
     * @param identifier the identifier
     * @return the identifier type
     */
    public static String getIdentifierType(String identifier) {
        if (identifier.startsWith("did:web")) {
            return StringPool.DID;
        } else {
            return StringPool.BPN;
        }
    }


    /**
     * Gets credential.
     *
     * @param subject    the subject
     * @param types      the types
     * @param issuerDoc  the issuer doc
     * @param privateKey the private key bytes
     * @param holderDid  the holder did
     * @return the credential
     */
    public static HoldersCredential getHoldersCredential(Map<String, Object> subject, List<String> types, DidDocument issuerDoc,
                                                         IPrivateKey privateKey, String holderDid, List<String> contexts, Date expiryDate, boolean selfIssued) {
        //VC Subject
        VerifiableCredentialSubject verifiableCredentialSubject =
                new VerifiableCredentialSubject(subject);


        List<String> cloneTypes = new ArrayList<>(types);

        // Create VC
        VerifiableCredential verifiableCredential = createVerifiableCredential(issuerDoc, types,
                verifiableCredentialSubject, privateKey, contexts, expiryDate);

            cloneTypes.remove(VerifiableCredentialType.VERIFIABLE_CREDENTIAL);

        // Create Credential
        return HoldersCredential.builder()
                .holderDid(holderDid)
                .issuerDid(URLDecoder.decode(issuerDoc.getId().toString(), Charset.defaultCharset()))
                .type(String.join(",", cloneTypes))
                .credentialId(verifiableCredential.getId().toString())
                .data(verifiableCredential)
                .selfIssued(selfIssued)
                .build();
    }


    @SneakyThrows
    private static VerifiableCredential createVerifiableCredential(DidDocument issuerDoc, List<String> verifiableCredentialType,
                                                                   VerifiableCredentialSubject verifiableCredentialSubject,
                                                                   IPrivateKey privateKey, List<String> contexts, Date expiryDate) {
        //VC Builder
        VerifiableCredentialBuilder builder =
                new VerifiableCredentialBuilder()
                        .context(contexts)
                        .id(URI.create(UUID.randomUUID().toString()))
                        .type(verifiableCredentialType)
                        .issuer(issuerDoc.getId())
                        .expirationDate(expiryDate.toInstant())
                        .issuanceDate(Instant.now())
                        .credentialSubject(verifiableCredentialSubject);


        LinkedDataProofGenerator generator = LinkedDataProofGenerator.newInstance(SignatureType.JWS);
        URI verificationMethod = issuerDoc.getVerificationMethods().get(0).getId();

        JWSSignature2020 proof =
                (JWSSignature2020) generator.createProof(
                        builder.build(), verificationMethod, privateKey);


        //Adding Proof to VC
        builder.proof(proof);

        //Create Credential
        return builder.build();
    }
}
