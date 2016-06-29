/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hummingbird.template.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.hummingbird.template.model.TemplateModelTest.BasicTypeModel;
import com.vaadin.hummingbird.template.model.TemplateModelTest.BeanModel;
import com.vaadin.hummingbird.template.model.TemplateModelTest.ListBeanModel;
import com.vaadin.hummingbird.template.model.TemplateModelTest.NotSupportedModel;
import com.vaadin.hummingbird.template.model.TemplateModelTest.SubBeansModel;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithExclude.ModelWithExclude;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithExcludeAndInclude.ModelWithExcludeAndInclude;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithExcludeAndIncludeSubclassOverrides.ModelWithExcludeAndIncludeSubclass;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithExcludeForSubBean.ModelWithExcludeForSubBean;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithExcludeOnList.ModelWithExcludeOnList;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithInclude.ModelWithInclude;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithIncludeForSubBean.ModelWithIncludeForSubBean;
import com.vaadin.hummingbird.template.model.TemplateModelTest.TemplateWithIncludeOnList.ModelWithIncludeOnList;

public class ModelDescriptorTest {

    // All properties in Bean.java
    private static final Collection<String> beanProperties = Collections
            .unmodifiableCollection(Arrays.asList("booleanValue",
                    "booleanObject", "intValue", "intObject", "doubleValue",
                    "doubleObject", "string"));

    @Test
    public void primitiveProperties() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(BasicTypeModel.class);

        Map<String, ModelType> expectedTypes = new HashMap<>();
        expectedTypes.put("boolean", BasicModelType.get(Boolean.class).get());
        expectedTypes.put("booleanPrimitive",
                BasicModelType.get(boolean.class).get());
        expectedTypes.put("string", BasicModelType.get(String.class).get());
        expectedTypes.put("double", BasicModelType.get(Double.class).get());
        expectedTypes.put("doublePrimitive",
                BasicModelType.get(double.class).get());
        expectedTypes.put("int", BasicModelType.get(int.class).get());
        expectedTypes.put("integer", BasicModelType.get(Integer.class).get());

        Set<String> propertyNames = descriptor.getPropertyNames()
                .collect(Collectors.toSet());
        Assert.assertEquals(expectedTypes.keySet(), propertyNames);

        expectedTypes.forEach((propertyName, expectedType) -> Assert.assertSame(
                expectedType, descriptor.getPropertyType(propertyName)));
    }

    @Test
    public void missingProperty() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(BasicTypeModel.class);
        Assert.assertFalse(descriptor.hasProperty("foo"));
    }

    @Test(expected = InvalidTemplateModelException.class)
    public void unsupported() {
        ModelDescriptor.get(NotSupportedModel.class);
    }

    @Test
    public void beanProperty() {
        ModelDescriptor<?> descriptor = ModelDescriptor.get(BeanModel.class);

        Assert.assertEquals(1, descriptor.getPropertyNames().count());

        BeanModelType<?> beanPropertyType = (BeanModelType<?>) descriptor
                .getPropertyType("bean");

        Assert.assertSame(Bean.class, beanPropertyType.getProxyType());
    }

    @Test
    public void listProperty() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ListBeanModel.class);

        Assert.assertEquals(1, descriptor.getPropertyNames().count());

        ListModelType<?> listPropertyType = (ListModelType<?>) descriptor
                .getPropertyType("beans");

        Assert.assertSame(Bean.class,
                listPropertyType.getItemType().getProxyType());
    }

    @Test
    public void subBeans() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(SubBeansModel.class);

        // Check that we discovered properties from an interface "bean"
        BeanModelType<?> beanType = (BeanModelType<?>) descriptor
                .getPropertyType("bean");
        Assert.assertEquals(
                Stream.of("beanClass", "value", "bean")
                        .collect(Collectors.toSet()),
                beanType.getPropertyNames().collect(Collectors.toSet()));

        // Check that we discovered properties both from SubBean and SuperBean
        BeanModelType<?> subBeanType = (BeanModelType<?>) descriptor
                .getPropertyType("beanClass");
        Assert.assertTrue(subBeanType.hasProperty("visible"));
        Assert.assertTrue(subBeanType.hasProperty("subBean"));
    }

    @Test
    public void exclude() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithExclude.class);

        BeanModelType<?> beanType = (BeanModelType<?>) descriptor
                .getPropertyType("bean");

        Set<String> expectedProperties = new HashSet<>(beanProperties);
        expectedProperties.remove("doubleValue");
        expectedProperties.remove("booleanObject");

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

    @Test
    public void include() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithInclude.class);

        BeanModelType<?> beanType = (BeanModelType<?>) descriptor
                .getPropertyType("bean");

        Set<String> expectedProperties = new HashSet<>(
                Arrays.asList("doubleValue", "booleanObject"));

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

    @Test
    public void excludeAndInclude() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithExcludeAndInclude.class);

        BeanModelType<?> beanType = (BeanModelType<?>) descriptor
                .getPropertyType("bean");

        Set<String> expectedProperties = Collections.singleton("booleanObject");

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

    @Test
    public void excludeAndIncludeSubclass() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithExcludeAndIncludeSubclass.class);

        BeanModelType<?> beanType = (BeanModelType<?>) descriptor
                .getPropertyType("bean");

        Set<String> expectedProperties = Collections.singleton("doubleValue");

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

    @Test
    public void excludeForSubBean() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithExcludeForSubBean.class);

        BeanModelType<?> containerType = (BeanModelType<?>) descriptor
                .getPropertyType("beanContainingBeans");

        BeanModelType<?> beanType = (BeanModelType<?>) containerType
                .getPropertyType("bean1");

        Assert.assertFalse(containerType.hasProperty("bean2"));

        Set<String> expectedProperties = new HashSet<>(beanProperties);
        expectedProperties.remove("booleanObject");

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

    @Test
    public void includeForSubBean() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithIncludeForSubBean.class);

        BeanModelType<?> containerType = (BeanModelType<?>) descriptor
                .getPropertyType("beanContainingBeans");

        BeanModelType<?> beanType = (BeanModelType<?>) containerType
                .getPropertyType("bean1");

        Assert.assertFalse(containerType.hasProperty("bean2"));

        Set<String> expectedProperties = Collections.singleton("booleanObject");

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

    @Test
    public void excludeOnList() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithExcludeOnList.class);

        ListModelType<?> beansType = (ListModelType<?>) descriptor
                .getPropertyType("beans");
        BeanModelType<?> beanType = beansType.getItemType();

        Set<String> expectedProperties = new HashSet<>(beanProperties);
        expectedProperties.remove("intValue");

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

    @Test
    public void includeOnList() {
        ModelDescriptor<?> descriptor = ModelDescriptor
                .get(ModelWithIncludeOnList.class);

        ListModelType<?> beansType = (ListModelType<?>) descriptor
                .getPropertyType("beans");
        BeanModelType<?> beanType = beansType.getItemType();

        Set<String> expectedProperties = Collections.singleton("intValue");

        Assert.assertEquals(expectedProperties,
                beanType.getPropertyNames().collect(Collectors.toSet()));
    }

}