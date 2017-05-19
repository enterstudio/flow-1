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

package com.vaadin.flow.event;

import java.io.Serializable;

import com.vaadin.flow.dom.EventRegistrationHandle;

/**
 * A listener for property change events.
 *
 * @see PropertyChangeEvent
 * @see EventRegistrationHandle
 */
@FunctionalInterface
public interface PropertyChangeListener extends Serializable {
    /**
     * Invoked when this listener receives a property change event from an event
     * source to which it has been added.
     *
     * @param event
     *            the received event, not null
     */
    public void propertyChange(PropertyChangeEvent event);
}