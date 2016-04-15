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
package com.vaadin.hummingbird;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.hummingbird.dom.Element;
import com.vaadin.hummingbird.dom.ElementFactory;
import com.vaadin.hummingbird.namespace.ElementChildrenNamespace;
import com.vaadin.hummingbird.util.JsonUtil;
import com.vaadin.ui.UI;

import elemental.json.Json;
import elemental.json.JsonBoolean;
import elemental.json.JsonNull;
import elemental.json.JsonNumber;
import elemental.json.JsonString;
import elemental.json.JsonValue;

public class JsonCodecTest {
    private static List<Object> withTypeInfoUnsupportedValues = Arrays.asList(
            new Object(), new StateNode(), new Date(), new String[0],
            new ArrayList<>(), new HashSet<>(), new HashMap<>());

    @Test
    public void encodeWithoutTypeInfo_supportedTypes() {
        assertJsonEquals(Json.create(true),
                JsonCodec.encodeWithoutTypeInfo(Boolean.TRUE));
        assertJsonEquals(Json.create("string"),
                JsonCodec.encodeWithoutTypeInfo("string"));
        assertJsonEquals(Json.create(3.14),
                JsonCodec.encodeWithoutTypeInfo(Double.valueOf(3.14)));
        assertJsonEquals(Json.create(42),
                JsonCodec.encodeWithoutTypeInfo(Integer.valueOf(42)));
        assertJsonEquals(Json.createNull(),
                JsonCodec.encodeWithoutTypeInfo(null));

        assertJsonEquals(Json.createNull(), Json.createNull());
        assertJsonEquals(Json.create(false), Json.create(false));
        assertJsonEquals(Json.create(234), Json.create(234));
        assertJsonEquals(Json.create("string"), Json.create("string"));
        assertJsonEquals(Json.createObject(), Json.createObject());
        assertJsonEquals(Json.createArray(), Json.createArray());
    }

    @Test
    public void encodeWithoutTypeInfo_unsupportedTypes() {
        List<Object> unsupported = new ArrayList<>(
                withTypeInfoUnsupportedValues);
        unsupported.add(ElementFactory.createDiv());

        for (Object value : unsupported) {
            try {
                JsonCodec.encodeWithoutTypeInfo(value);

                Assert.fail("Should throw for " + value.getClass());
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    @Test
    public void encodeWithTypeInfo_basicTypes() {
        assertJsonEquals(Json.create(true),
                JsonCodec.encodeWithTypeInfo(Boolean.TRUE));
        assertJsonEquals(Json.createNull(), JsonCodec.encodeWithTypeInfo(null));

        assertJsonEquals(Json.create(234),
                JsonCodec.encodeWithTypeInfo(Json.create(234)));
        assertJsonEquals(Json.create("string"),
                JsonCodec.encodeWithTypeInfo(Json.create("string")));
        assertJsonEquals(Json.createObject(),
                JsonCodec.encodeWithTypeInfo(Json.createObject()));

        // Array is escaped
        assertJsonEquals(
                JsonUtil.createArray(Json.create(JsonCodec.ARRAY_TYPE),
                        Json.createArray()),
                JsonCodec.encodeWithTypeInfo(Json.createArray()));
    }

    @Test
    public void encodeWithTypeInfo_attachedElement() {
        Element element = ElementFactory.createDiv();

        StateTree tree = new StateTree(new UI(),
                ElementChildrenNamespace.class);
        tree.getRootNode().getNamespace(ElementChildrenNamespace.class).add(0,
                element.getNode());

        JsonValue json = JsonCodec.encodeWithTypeInfo(element);

        assertJsonEquals(
                JsonUtil.createArray(Json.create(JsonCodec.ELEMENT_TYPE),
                        Json.create(element.getNode().getId())),
                json);
    }

    @Test
    public void encodeWithTypeInfo_detachedElement() {
        Element element = ElementFactory.createDiv();

        JsonValue json = JsonCodec.encodeWithTypeInfo(element);

        assertJsonEquals(Json.createNull(), json);
    }

    @Test
    public void encodeWithTypeInfo_unsupportedTypes() {
        for (Object value : withTypeInfoUnsupportedValues) {
            try {
                JsonCodec.encodeWithTypeInfo(value);

                Assert.fail("Should throw for " + value.getClass());
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    private static void assertJsonEquals(JsonValue expected, JsonValue actual) {
        Assert.assertTrue(
                actual.toJson() + " does not equal " + expected.toJson(),
                JsonUtil.jsonEquals(expected, actual));
    }

    @Test
    public void decodeAs_booleanJson() {
        JsonBoolean json = Json.create(true);
        Assert.assertTrue(JsonCodec.decodeAs(json, Boolean.class));
        Assert.assertEquals("true", JsonCodec.decodeAs(json, String.class));
        Assert.assertEquals(Integer.valueOf(1),
                JsonCodec.decodeAs(json, Integer.class));
        Assert.assertEquals(Double.valueOf(1.0),
                JsonCodec.decodeAs(json, Double.class));
    }

    @Test
    public void decodeAs_stringJson() {
        JsonString json = Json.create("Test123 String\n !%");
        Assert.assertTrue(JsonCodec.decodeAs(json, Boolean.class));
        Assert.assertEquals("Test123 String\n !%",
                JsonCodec.decodeAs(json, String.class));
        Assert.assertEquals(Integer.valueOf(0),
                JsonCodec.decodeAs(json, Integer.class));
        Assert.assertTrue(JsonCodec.decodeAs(json, Double.class).isNaN());
    }

    @Test
    public void decodeAs_numberJson() {
        JsonNumber json = Json.create(15.7);
        Assert.assertTrue(JsonCodec.decodeAs(json, Boolean.class));
        Assert.assertEquals("15.7", JsonCodec.decodeAs(json, String.class));
        Assert.assertEquals(Integer.valueOf(15),
                JsonCodec.decodeAs(json, Integer.class));
        Assert.assertEquals(Double.valueOf(15.7),
                JsonCodec.decodeAs(json, Double.class));
    }

    @Test
    public void decodeAs_nullJson() {
        JsonNull json = Json.createNull();
        Assert.assertNull(JsonCodec.decodeAs(json, Boolean.class));
        Assert.assertNull(JsonCodec.decodeAs(json, String.class));
        Assert.assertNull(JsonCodec.decodeAs(json, Integer.class));
        Assert.assertNull(JsonCodec.decodeAs(json, Double.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeAs_unsupportedType() {
        Assert.assertNull(
                JsonCodec.decodeAs(Json.create("foo"), boolean.class));
    }
}
