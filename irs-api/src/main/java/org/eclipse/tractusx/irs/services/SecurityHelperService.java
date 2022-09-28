/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.services;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Helper methods to retrieve data from Jwt token
 */
public final class SecurityHelperService {

    private static final String UNKNOWN = "Unknown";

    public String getClientIdClaim() {
        return getClaimOrUnknown("clientId", getAuthenticationFromSecurityContext());
    }

    private String getClaimOrUnknown(final String claimName, final Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            final Jwt token = ((JwtAuthenticationToken) authentication).getToken();

            return Optional.ofNullable(token.getClaim(claimName)).map(Object::toString).orElse(UNKNOWN);
        }

        return UNKNOWN;
    }

    private Authentication getAuthenticationFromSecurityContext() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
