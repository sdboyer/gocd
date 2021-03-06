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

package com.thoughtworks.go.domain;

import com.thoughtworks.go.config.AgentConfig;
import com.thoughtworks.go.config.Agents;
import com.thoughtworks.go.config.Approval;
import com.thoughtworks.go.config.EnvironmentVariableConfig;
import com.thoughtworks.go.config.EnvironmentVariablesConfig;
import com.thoughtworks.go.config.Resources;
import com.thoughtworks.go.config.StageConfig;
import com.thoughtworks.go.helper.StageConfigMother;
import org.junit.Test;

import static com.thoughtworks.go.config.Resource.resources;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DefaultSchedulingContextTest {

    @Test
    public void shouldFindNoAgentsIfNoneExist() throws Exception {
        DefaultSchedulingContext context = new DefaultSchedulingContext("approved", new Agents());
        assertThat(context.findAgentsMatching(new Resources()), is(new Agents()));
    }

    @Test public void shouldFindAllAgentsIfNoResourcesAreSpecified() throws Exception {
        AgentConfig linux = agent("uuid1", "linux");
        AgentConfig windows = agent("uuid2", "windows");
        Agents matchingAgents = new Agents(linux, windows);
        DefaultSchedulingContext context = new DefaultSchedulingContext("approved", matchingAgents);
        assertThat(context.findAgentsMatching(new Resources()), is(matchingAgents));
    }
    
    @Test public void shouldOnlyFindAgentsThatMatchResourcesSpecified() throws Exception {
        AgentConfig linux = agent("uuid1", "linux");
        AgentConfig windows = agent("uuid2", "windows");
        Agents matchingAgents = new Agents(linux, windows);
        DefaultSchedulingContext context = new DefaultSchedulingContext("approved", matchingAgents);
        assertThat(context.findAgentsMatching(resources("linux")), is(new Agents(linux)));
    }

    @Test public void shouldFindNoAgentsIfNoneMatch() throws Exception {
        AgentConfig linux = agent("uuid1", "linux");
        AgentConfig windows = agent("uuid2", "windows");
        Agents matchingAgents = new Agents(linux, windows);
        DefaultSchedulingContext context = new DefaultSchedulingContext("approved", matchingAgents);
        assertThat(context.findAgentsMatching(resources("macosx")), is(new Agents()));
    }

    @Test public void shouldNotMatchDeniedAgents() throws Exception {
        AgentConfig linux = agent("uuid1", "linux");
        AgentConfig windows = agent("uuid2", "windows");
        windows.disable();
        Agents matchingAgents = new Agents(linux, windows);
        DefaultSchedulingContext context = new DefaultSchedulingContext("approved", matchingAgents);
        assertThat(context.findAgentsMatching(resources()), is(new Agents(linux)));
    }

    @Test
    public void shouldSetEnvironmentVariablesOnSchedulingContext() throws Exception {
        EnvironmentVariablesConfig existing = new EnvironmentVariablesConfig();
        existing.add("firstVar", "firstVal");
        existing.add("overriddenVar", "originalVal");

        SchedulingContext schedulingContext = new DefaultSchedulingContext();
        schedulingContext = schedulingContext.overrideEnvironmentVariables(existing);

        EnvironmentVariablesConfig stageLevel = new EnvironmentVariablesConfig();
        stageLevel.add("stageVar", "stageVal");
        stageLevel.add("overriddenVar", "overriddenVal");

        StageConfig config = StageConfigMother.custom("test", Approval.automaticApproval());
        config.setVariables(stageLevel);

        SchedulingContext context = schedulingContext.overrideEnvironmentVariables(config.getVariables());

        EnvironmentVariablesConfig environmentVariablesUsed = context.getEnvironmentVariablesConfig();
        assertThat(environmentVariablesUsed.size(), is(3));
        assertThat(environmentVariablesUsed, hasItem(new EnvironmentVariableConfig("firstVar", "firstVal")));
        assertThat(environmentVariablesUsed, hasItem(new EnvironmentVariableConfig("overriddenVar", "overriddenVal")));
        assertThat(environmentVariablesUsed, hasItem(new EnvironmentVariableConfig("stageVar", "stageVal")));
    }

    private AgentConfig agent(String uuid, String... names) {
        return new AgentConfig(uuid, "localhost", "127.0.0.1", resources(names));
    }

}
