/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.knative.handlers;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.knative.exceptions.KnativePluginException;
import org.ballerinax.knative.models.SecretModel;
import org.ballerinax.knative.utils.KnativeUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * Generates knative secret.
 */
public class KnativeSecretHandler extends KnativeAbstractArtifactHandler {

    private void generate(SecretModel secretModel) throws KnativePluginException {
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withNamespace(knativeDataHolder.getNamespace())
                .withName(secretModel.getName())
                .endMetadata()
                .withData(secretModel.getData())
                .build();
        try {
            String secretContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(secret);
            KnativeUtils.writeToFile(secretContent);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for secret: " + secretModel.getName();
            throw new KnativePluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KnativePluginException {
        //secret
        int count = 0;
        Collection<SecretModel> secretModels = knativeDataHolder.getSecretModelSet();
        if (secretModels.size() > 0) {
            OUT.println();
        }
        for (SecretModel secretModel : secretModels) {
            count++;
            generate(secretModel);
            OUT.print("\t@kubernetes:Secret \t\t\t - complete " + count + "/" + secretModels.size() + "\r");
        }
    }
}
