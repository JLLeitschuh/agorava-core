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

package org.agorava.oauth.helpers.extractors;

import org.agorava.api.exception.OAuthParametersMissingException;
import org.agorava.api.extractor.ExtractorType;
import org.agorava.api.extractor.StringExtractor;
import org.agorava.api.oauth.OAuthRequest;
import org.agorava.api.rest.HttpParameters;
import org.agorava.api.service.OAuthEncoder;
import org.agorava.api.service.Preconditions;
import org.agorava.rest.HttpParametersImpl;

import static org.agorava.api.extractor.ExtractorType.Type.OAUTH1_BASE_STRING;

/**
 * Default implementation of {@link org.agorava.api.extractor.StringExtractor}. Conforms to OAuth 1.0a
 *
 * @author Pablo Fernandez
 */
@ExtractorType(OAUTH1_BASE_STRING)
public class BaseStringExtractor implements StringExtractor {

    private static final String AMPERSAND_SEPARATED_STRING = "%s&%s&%s";

    /**
     * {@inheritDoc}
     */
    public String extract(OAuthRequest request) {
        checkPreconditions(request);
        String verb = OAuthEncoder.encode(request.getVerb().name());
        String url = OAuthEncoder.encode(request.getSanitizedUrl());
        String params = getSortedAndEncodedParams(request);
        return String.format(AMPERSAND_SEPARATED_STRING, verb, url, params);
    }

    private String getSortedAndEncodedParams(OAuthRequest request) {
        HttpParameters params = new HttpParametersImpl();
        params.addAll(request.getQueryStringParams());
        params.addAll(request.getBodyParams());
        params.addAll(new HttpParametersImpl(request.getOauthParameters()));
        return params.asOauthBaseString();
    }

    private void checkPreconditions(OAuthRequest request) {
        Preconditions.checkNotNull(request, "Cannot extract base string from null object");

        if (request.getOauthParameters() == null || request.getOauthParameters().size() <= 0) {
            throw new OAuthParametersMissingException(request);
        }
    }
}
