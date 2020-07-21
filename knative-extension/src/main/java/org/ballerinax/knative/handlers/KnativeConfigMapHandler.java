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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.knative.exceptions.KnativePluginException;
import org.ballerinax.knative.models.ConfigMapModel;
import org.ballerinax.knative.models.EnvVarValueModel;
import org.ballerinax.knative.models.ServiceModel;
import org.ballerinax.knative.utils.KnativeUtils;

import java.io.IOException;
import java.util.Collection;

import static org.ballerinax.knative.KnativeConstants.BALLERINA_CONF_FILE_NAME;
import static org.ballerinax.knative.utils.KnativeUtils.isBlank;

/**
 * Generates kubernetes Config Map.
 */
public class KnativeConfigMapHandler extends KnativeAbstractArtifactHandler {

    private void generate(ConfigMapModel configMapModel) throws KnativePluginException {
        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapModel.getName())
                .withNamespace(knativeDataHolder.getNamespace())
                .endMetadata()
                .withData(configMapModel.getData())
                .build();
        try {
            String configMapContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(configMap);
            KnativeUtils.writeToFile(configMapContent);
        } catch (IOException e) {
            String errorMessage = "Error while parsing yaml file for config map: " + configMapModel.getName();
            throw new KnativePluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KnativePluginException {
        //configMap
        int count = 0;
        Collection<ConfigMapModel> configMapModels = knativeDataHolder.getConfigMapModelSet();
        if (configMapModels.size() > 0) {
            OUT.println();
        }
        for (ConfigMapModel configMapModel : configMapModels) {
            count++;
            if (!isBlank(configMapModel.getBallerinaConf())) {
                if (configMapModel.getData().size() != 1) {
                    throw new KnativePluginException("there can be only 1 ballerina config file");
                }
                ServiceModel serviceModel = knativeDataHolder.getServiceModel();
                serviceModel.setCommandArgs(" --b7a.config.file=${CONFIG_FILE}");
                EnvVarValueModel envVarValueModel = new EnvVarValueModel(configMapModel.getMountPath() +
                        BALLERINA_CONF_FILE_NAME);
                serviceModel.addEnv("CONFIG_FILE", envVarValueModel);
                knativeDataHolder.setServiceModel(serviceModel);
            }
            generate(configMapModel);
            OUT.print("\t@knative:ConfigMap \t\t\t - complete " + count + "/" + configMapModels.size() + "\r");
        }
    }
}
