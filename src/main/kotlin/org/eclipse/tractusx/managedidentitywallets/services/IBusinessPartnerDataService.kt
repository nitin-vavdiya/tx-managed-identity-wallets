/********************************************************************************
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.managedidentitywallets.services

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import kotlinx.coroutines.Deferred
import org.eclipse.tractusx.managedidentitywallets.models.BPDMConfig
import org.eclipse.tractusx.managedidentitywallets.models.WalletDto
import org.slf4j.LoggerFactory

interface IBusinessPartnerDataService {

    suspend fun pullDataAndUpdateBaseWalletCredentialsAsync(identifier: String? = null): Deferred<Boolean>

    suspend fun issueAndStoreBaseWalletCredentialsAsync(
        walletHolderDto: WalletDto,
        type: String,
        data: Any? = null
    ): Deferred<Boolean>

    suspend fun issueAndSendBaseWalletCredentialsForSelfManagedWalletsAsync(
        targetWallet: WalletDto,
        connectionId: String,
        webhookUrl: String? = null,
        type: String,
        data: Any? = null
    ): Deferred<Boolean>

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)

        fun createBusinessPartnerDataService(
            walletService: IWalletService,
            bpdmConfig: BPDMConfig,
            membershipOrganisation: String
        ): IBusinessPartnerDataService {
            return BusinessPartnerDataServiceImpl(
                walletService,
                bpdmConfig,
                membershipOrganisation,
                HttpClient {
                    expectSuccess = false // must be false to handle thrown error if the access token has expired
                    install(ResponseObserver) {
                        onResponse { response ->
                            log.debug("HTTP status: ${response.status.value}")
                            log.debug("HTTP description: ${response.status.description}")
                        }
                    }
                    install(HttpTimeout) {
                        requestTimeoutMillis = 30000
                        connectTimeoutMillis = 30000
                        socketTimeoutMillis = 30000
                    }
                    install(Logging) {
                        logger = Logger.DEFAULT
                        level = LogLevel.BODY
                    }
                })
        }
    }
}
