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

import java.lang.reflect.Field;

import com.thoughtworks.go.config.CruiseConfig;
import com.thoughtworks.go.config.JobConfig;
import com.thoughtworks.go.config.PipelineConfig;
import com.thoughtworks.go.config.Resource;
import com.thoughtworks.go.config.TemplatesConfig;
import com.thoughtworks.go.config.ValidationContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;

public class ResourceTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldAllowValidResourceNameForAgentResources() throws Exception {
        Resource resource = resource("- foo|bar baz.quux");
        resource.validate(ValidationContext.forChain(new CruiseConfig()));
        assertThat(resource.errors().isEmpty(), is(true));
    }

    @Test
    public void shouldAllowParamsInsideResourceNameWhenInsideTemplates() throws Exception {
        Resource resource = resource("#{PARAMS}");
        ValidationContext context = ValidationContext.forChain(new CruiseConfig(), new TemplatesConfig());
        resource.validate(context);
        assertThat(resource.errors().isEmpty(), is(true));
    }

    @Test // Note : At the Resource class level there is no way of accurately validating Parameters. This will only be invalidated when template gets used.
    public void validate_shouldAllowAnyCombinationOfHashesAndCurlyBraces() throws Exception {
        Resource resource = resource("}#PARAMS{");
        ValidationContext context = ValidationContext.forChain(new CruiseConfig(), new TemplatesConfig());
        resource.validate(context);
        assertThat(resource.errors().isEmpty(), is(true));
    }

    @Test
    public void shouldNotAllowInvalidResourceNamesWhenInsideTemplates() throws Exception {
        Resource resource = resource("#?{45}");
        ValidationContext context = ValidationContext.forChain(new CruiseConfig(), new TemplatesConfig());
        resource.validate(context);
        assertThat(resource.errors().isEmpty(), is(false));
        assertThat(resource.errors().on(JobConfig.RESOURCES), is(String.format("Resource name '#?{45}' is not valid. Valid names can contain valid parameter syntax or valid alphanumeric with hyphens,dots or pipes")));
    }

    @Test
    public void shouldNotAllowParamsInsideResourceNameWhenOutsideTemplates() throws Exception {
        Resource resource = resource("#{PARAMS}");
        ValidationContext context = ValidationContext.forChain(new CruiseConfig(), new PipelineConfig());
        resource.validate(context);
        assertThat(resource.errors().isEmpty(), is(false));
        assertThat(resource.errors().on(JobConfig.RESOURCES), is(String.format("Resource name '#{PARAMS}' is not valid. Valid names much match '%s'", Resource.VALID_REGEX)));
    }

    @Test
    public void shouldNotAllowInvalidResourceNameForAgentResources() throws Exception {
        Resource resource = resource("foo$bar");
        resource.validate(ValidationContext.forChain(new CruiseConfig()));
        ConfigErrors configErrors = resource.errors();
        assertThat(configErrors.isEmpty(), is(false));
        assertThat(configErrors.on(JobConfig.RESOURCES), is(String.format("Resource name 'foo$bar' is not valid. Valid names much match '%s'", Resource.VALID_REGEX)));
    }

    private Resource resource(String name) {
        Resource resource = new Resource();
        resource.setName(name);
        return resource;
    }

    @Test
    public void shouldBeEqualBasedOnContentAndNotID() {
        assertThat(new Resource("resource1"), is(new Resource("resource1")));
        assertThat(new Resource("resource1"), not(new Resource("resource2")));
        Resource resource = new Resource("resource1");
        resource.setId(999);
        assertThat(resource, is(new Resource("resource1")));
    }

    @Test
    //This is to work around a bug caused by MagicalCruiseConfigLoader,
    // since it uses direct field access
    public void shouldUseTrimmedNameInEquals() throws NoSuchFieldException, IllegalAccessException {
        Resource resource = new Resource();
        Field field = resource.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(resource, "resource1   ");
        assertThat(new Resource("resource1"), is(resource));
    }

    @Test
    public void shouldCompareBasedOnName() {
        Resource resourceA = new Resource("aaa");
        Resource resourceB = new Resource("bbb");
        assertThat(resourceA.compareTo(resourceB), is(lessThan(0)));
        assertThat(resourceB.compareTo(resourceA), is(greaterThan(0)));
        assertThat(resourceA.compareTo(resourceA), is(0));        
    }


}