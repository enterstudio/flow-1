/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.uitest.ui.webcomponent;

import com.vaadin.ui.common.HasElement;
import com.vaadin.ui.common.PropertyDescriptor;
import com.vaadin.ui.common.PropertyDescriptors;

/**
 * Defines setters and getters for a "value" property with a default of 0.
 *
 * @author Vaadin Ltd
 */
public interface HasValue extends HasElement {

    class Descriptors {
        // Hides descriptors
        private static final PropertyDescriptor<Integer, Integer> valueProperty = PropertyDescriptors
                .propertyWithDefault("value", 0);
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    default int getValue() {
        return Descriptors.valueProperty.get(this);
    }

    /**
     * Sets the value.
     *
     * @param value
     *            the value to set
     */
    default void setValue(int value) {
        Descriptors.valueProperty.set(this, value);
    }
}
