/*
 * Copyright 2014-2016 Agorava
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

package org.agorava.api.oauth.application;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Configuration settings needed to access to an OAuth 1.0a and 2.0 service tier.
 * Used by {@link org.agorava.api.oauth.OAuthService} to setup
 * connection to Social Media
 *
 * @author Antoine Sabot-Durand
 * @author Werner Keil
 */
public interface OAuthAppSettings extends Serializable {
    /**
     * @return the key consumer key for the OAuth service
     */
    String getApiKey();

    /**
     * @return the consumer secret key for the OAuth service
     */
    String getApiSecret();

    /**
     * @return the call back URL for the OAuth service
     */
    String getCallback();

    /**
     * @return the scope requested
     */
    String getScope();

    /**
     * @return the name of the service
     */
    String getSocialMediaName();

    /**
     * @return the qualifier associated to the service
     */
    Annotation getQualifier();

    /**
     * @return true if this OAuthAppSettings contains a scope
     */
    boolean hasScope();
    
    /**
     * @return true if this service is enabled
     */
    boolean isEnabled();
}
