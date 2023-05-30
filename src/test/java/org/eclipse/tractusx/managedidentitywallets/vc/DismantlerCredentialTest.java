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

package org.eclipse.tractusx.managedidentitywallets.vc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.managedidentitywallets.ManagedIdentityWalletsApplication;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.managedidentitywallets.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.dao.entity.Credential;
import org.eclipse.tractusx.managedidentitywallets.dao.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.dao.repository.CredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.dao.repository.WalletKeyRepository;
import org.eclipse.tractusx.managedidentitywallets.dao.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.dto.IssueDismantlerCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.dto.IssueMembershipCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.utils.AuthenticationUtils;
import org.eclipse.tractusx.managedidentitywallets.utils.TestUtils;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ActiveProfiles("test")
@ContextConfiguration(initializers = {TestContextInitializer.class})
public class DismantlerCredentialTest {
    @Autowired
    private CredentialRepository credentialRepository;
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletKeyRepository walletKeyRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MIWSettings miwSettings;


    @Test
    void issueDismantlerCredentialTest403() {
        String bpn = UUID.randomUUID().toString();

        String did = "did:web:localhost:" + bpn;
        HttpHeaders headers = AuthenticationUtils.getInvalidUserHttpHeaders();

        IssueMembershipCredentialRequest request = IssueMembershipCredentialRequest.builder().bpn(bpn).build();

        HttpEntity<IssueMembershipCredentialRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<VerifiableCredential> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, VerifiableCredential.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }


    @Test
    void issueDismantlerCredentialTest201() throws JsonProcessingException, JSONException {

        String bpn = UUID.randomUUID().toString();
        String did = "did:web:localhost:"+bpn;

        Wallet wallet = TestUtils.createWallet(bpn, did, walletRepository);
        ResponseEntity<String> response = issueDismantlerCredential(bpn, did);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(response.getBody(), Map.class);
        VerifiableCredential verifiableCredential = new VerifiableCredential(map);
        Assertions.assertTrue(verifiableCredential.getTypes().contains(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL_CX));

        TestUtils.checkVC(verifiableCredential, miwSettings);


        Assertions.assertTrue(verifiableCredential.getCredentialSubject().get(0).get("activityType").toString().equals("vehicleDismantle"));

        Credential credential = credentialRepository.getByHolderAndType(wallet.getId(), MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL_CX);
        Assertions.assertNotNull(credential);
        TestUtils.checkVC(credential.getData(), miwSettings);


        VerifiableCredential data = credential.getData();

        Assertions.assertTrue(data.getCredentialSubject().get(0).get("activityType").toString().equals("vehicleDismantle"));

    }

    @Test
    void issueDismantlerCredentialWithDuplicateBpn409() {

        String bpn = UUID.randomUUID().toString();

        String did = "did:web:localhost:" + bpn;

        //create entry
        Wallet wallet = TestUtils.createWallet(bpn, did, walletRepository);
        ResponseEntity<String> response = issueDismantlerCredential(bpn, did);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        //issue duplicate
        ResponseEntity<String> duplicateResponse = issueDismantlerCredential(bpn, did);
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), duplicateResponse.getStatusCode().value());
    }



    private ResponseEntity<String> issueDismantlerCredential(String bpn, String did){


        HttpHeaders headers = AuthenticationUtils.getValidUserHttpHeaders();

        IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder()
                .activityType("vehicleDismantle")
                .bpn(bpn)
                .allowedVehicleBrands(Set.of("BMW"))
                .build();


        HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);

        return  restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, String.class);
    }

}