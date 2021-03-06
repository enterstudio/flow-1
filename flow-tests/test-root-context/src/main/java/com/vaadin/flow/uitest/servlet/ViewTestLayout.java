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
package com.vaadin.flow.uitest.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.HasChildView;
import com.vaadin.flow.router.LocationChangeEvent;
import com.vaadin.flow.router.View;
import com.vaadin.ui.UI;

public class ViewTestLayout implements HasChildView {

    private Element element = ElementFactory.createDiv();
    private Element viewContainer = ElementFactory.createDiv();
    private Element viewSelect = ElementFactory.createSelect();

    public ViewTestLayout() {
        element.setAttribute("id", "main-layout");

        List<Class<? extends View>> classes = new ArrayList<>(
                ViewTestServlet.getViewLocator().getAllViewClasses());
        Comparator<Class<? extends View>> comparator = Comparator
                .comparing(Class::getName);
        Collections.sort(classes, comparator);

        String lastPackage = "";
        viewSelect.appendChild(ElementFactory.createOption());
        Element optionGroup = null;
        for (Class<? extends View> c : classes) {
            if (!c.getPackage().getName().equals(lastPackage)) {
                lastPackage = c.getPackage().getName();
                optionGroup = new Element("optgroup");
                optionGroup.setAttribute("label",
                        c.getPackage().getName().replaceAll("^.*\\.", ""));
                viewSelect.appendChild(optionGroup);
            }
            Element option = ElementFactory.createOption(c.getSimpleName())
                    .setAttribute("value", c.getName());
            option.setAttribute("id", c.getSimpleName());

            optionGroup.appendChild(option);
        }

        viewSelect.synchronizeProperty("value", "change");
        viewSelect.addEventListener("change", e -> {
            UI ui = UI.getCurrent();
            ui.navigateTo(viewSelect.getProperty("value"));
        });

        element.appendChild(viewSelect, ElementFactory.createHr(),
                viewContainer);
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public void setChildView(View subView) {
        viewContainer.setChild(0, subView.getElement());

    }

    @Override
    public void onLocationChange(LocationChangeEvent event) {
        // Defer value setting until all option elements have been attached
        UI.getCurrent().getPage().executeJavaScript(
                "setTimeout(function() {$0.value = $1}, 0)", viewSelect,
                event.getLocation().getPath());
    }

}
