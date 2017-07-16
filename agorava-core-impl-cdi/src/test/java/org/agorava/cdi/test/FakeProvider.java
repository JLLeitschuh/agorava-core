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

package org.agorava.cdi.test;

import org.agorava.api.oauth.Token;
import org.agorava.spi.ProviderConfigOauth10a;

/**
 * @author Antoine Sabot-Durand
 */
@FakeService
public class FakeProvider extends ProviderConfigOauth10a {
    @Override
    public String getProviderName() {
        return "Twitter";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAccessTokenEndpoint() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
