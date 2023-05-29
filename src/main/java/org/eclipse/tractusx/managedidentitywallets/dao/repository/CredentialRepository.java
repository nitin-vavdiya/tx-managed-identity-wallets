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

package org.eclipse.tractusx.managedidentitywallets.dao.repository;

import org.eclipse.tractusx.managedidentitywallets.dao.entity.Credential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * The interface Credential repository.
 */
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    /**
     * Gets by holder.
     *
     * @param id the id
     * @return the by holder
     */
    List<Credential> getByHolder(Long id);

    /**
     * Gets credentials by holder.
     *
     * @param holder the holder
     * @return the credentials by holder
     */
    @Query("select data from Credential where holder=:holder")
    List<VerifiableCredential> getCredentialsByHolder(@Param("holder") Long holder);

    /**
     * Gets by holder and type.
     *
     * @param holderWalletId the holder wallet id
     * @param type           the type
     * @return the by holder and type
     */
    Credential getByHolderAndType(Long holderWalletId, String type);

    /**
     * Exists by holder and type boolean.
     *
     * @param holderWalletId the holder wallet id
     * @param type           the type
     * @return the boolean
     */
    boolean existsByHolderAndType(Long holderWalletId, String type);
}