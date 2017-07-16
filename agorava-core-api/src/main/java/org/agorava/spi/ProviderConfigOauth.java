/*
 * Copyright 2013 Agorava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.agorava.spi;

import org.agorava.api.extractor.TokenExtractor;

import static org.agorava.api.oauth.OAuth.OAuthVersion;

/**
 * Root of all OAuth providers services configuration
 *
 * @author Antoine Sabot-Durand
 */
public abstract class ProviderConfigOauth extends ProviderConfig {


    /**
     * @return the type of the OAuth signature
     */
    public SignaturePlace getSignaturePlace() {
        return SignaturePlace.HEADER;
    }

    /**
     * Returns the access token extractor.
     *
     * @return access token extractor
     */
    public abstract TokenExtractor getAccessTokenExtractor();

    /**
     * @return the OAuth version supported by the tier
     */
    public abstract OAuthVersion getOAuthVersion();


    /**
     * Places where signature could be stored in {@link org.agorava.api.oauth.OAuthRequest} for OAuth 1.0a
     */
    public static enum SignaturePlace {
        /**
         * in Request header
         */
        HEADER,

        /**
         * in request query
         */
        QUERY_STRING,

        /**
         * in body
         */
        BODY
    }
}
