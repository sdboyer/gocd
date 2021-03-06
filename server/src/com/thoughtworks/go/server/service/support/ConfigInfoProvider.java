/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.server.service.support;

import com.thoughtworks.go.config.CruiseConfig;
import com.thoughtworks.go.config.validation.GoConfigValidity;
import com.thoughtworks.go.server.service.GoConfigService;
import com.thoughtworks.go.server.service.GoLicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigInfoProvider implements ServerInfoProvider {

    private GoConfigService service;
    private GoLicenseService licenseService;

    @Autowired
    public ConfigInfoProvider(GoConfigService service, GoLicenseService licenseService) {
        this.service = service;
        this.licenseService = licenseService;
    }

    @Override
    public double priority() {
        return 1.0;
    }

    @Override
    public void appendInformation(InformationStringBuilder infoCollector) {
        infoCollector.addSection("Config Statistics");
        GoConfigValidity goConfigValidity = service.checkConfigFileValid();
        if (!goConfigValidity.isValid()) {
            infoCollector.append(String.format("Config is invalid. Reason: %s", goConfigValidity.errorMessage()));
        } else {
            infoCollector.append("Valid Config.\n");
        }
        infoCollector.append("Number of pipelines: ").append(service.getAllPipelineConfigs().size())
                .append("\n").append("Number of agents: ").append(service.agents().size()).append("\n");

        CruiseConfig currentConfig = service.getCurrentConfig();
        infoCollector.append(String.format("Number of environments: %s\n", currentConfig.getEnvironments().size()));
        infoCollector.append(String.format("Number of unique materials: %s\n", currentConfig.getAllUniqueMaterials().size()));
        infoCollector.append(String.format("Number of schedulable materials: %s\n", service.getSchedulableMaterials().size()));

        boolean ldapEnabled = service.security().ldapConfig().isEnabled();
        boolean passwordEnabled = service.security().passwordFileConfig().isEnabled();

        infoCollector.append(String.format("Security:\nLDAP: %s, Password: %s\n", ldapEnabled, passwordEnabled));

        infoCollector.append(String.format("License: Expire Date: %s, Is Valid: %s, Remote Agents: %s, Number of Users: %s\n",
                licenseService.getExpirationDate(), licenseService.isLicenseValid(),
                licenseService.getNumberOfLicensedRemoteAgents(), licenseService.maximumUsersAllowed()));
    }
}
