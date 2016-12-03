package io.runidle.testing.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;

public class Xml2Properties {
    private final static Logger log = LoggerFactory.getLogger(Xml2Properties.class);
    private final static SAXParserFactory saxFactory = saxFactory();

    private synchronized static SAXParserFactory saxFactory() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        return factory;
    }

    public static Properties load(String fileName) throws IOException {
        return load(fileName, "includeRoot");
    }

    public static Properties load(String fileName, String includeRootAttr) throws IOException {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
        return load(inputStream, includeRootAttr);
    }

    public static Properties load(InputStream inputStream) throws IOException {
        return load(inputStream, "includeRoot");
    }

    public static Properties load(InputStream inputStream, String includeRootAttr) throws IOException {
        try {
            SAXParser parser = saxFactory.newSAXParser();
            Xml2PropertiesHandler handler = new Xml2PropertiesHandler(includeRootAttr);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            parser.parse(inputStream, handler);
            return handler.properties();
        } catch (ParserConfigurationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }

    static class Xml2PropertiesHandler extends DefaultHandler2 {
        private PropertyObject properties;
        private Stack<PropertyObject> values = new Stack<>();
        private String includeRootAttr;

        public Xml2PropertiesHandler(String includeRootAttr) {
            this.includeRootAttr = includeRootAttr;
        }

        public Properties properties() {
            return this.properties.properties();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            PropertyObject propertyObject;
            if (this.properties == null) {
                this.properties = new PropertyObject(qName, this.includeRootAttr);
                propertyObject = this.properties;
            } else {
                propertyObject = this.values.peek().newProperty(qName);
            }
            this.values.push(propertyObject);

            IntStream.range(0, attributes.getLength()).forEach(i -> {
                String attrName = attributes.getQName(i);
                String attrValue = attributes.getValue(i);
                propertyObject.setAttribute(attrName, attrValue);
            });
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            values.peek().setValue(new String(ch, start, length));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            values.pop();
        }

        @Override
        public void error(SAXParseException e) throws SAXException {

        }
    }

    private static class PropertyPrefix {
        private PropertyPrefix propertyPrefix;
        private String value;

        public PropertyPrefix(PropertyPrefix propertyPrefix, String value) {
            this.propertyPrefix = propertyPrefix;
            this.value = value;
        }

        public String fullValue() {
            if (this.propertyPrefix == null) return this.value == null ? "" : this.value + ".";
            return this.propertyPrefix + "." + this.value + ".";
        }

        public PropertyPrefix setPrefix(PropertyPrefix prefix) {
            this.propertyPrefix = prefix;
            return this;
        }
    }

    private static class PropertyObject {
        private String name;
        private PropertyObject parent;
        private Map<String, PropertyObject> mapObjects = new HashMap<>();
        private Map<String, List<PropertyObject>> listObjects = new HashMap<>();
        private Map<String, String> attributes = new HashMap<>();
        private String value;
        private boolean isRoot = false;
        private String rootAttribute;
        private int index = -1;

        public PropertyObject(String name, String rootAttribute) {
            this.name = name;
            if (rootAttribute != null) {
                this.rootAttribute = rootAttribute;
                this.isRoot = true;
            }
        }

        private PropertyObject(String name, PropertyObject parent) {
            this(name, (String) null);
            this.parent = parent;
        }

        public PropertyObject setValue(String value) {
            this.value = value;
            return this;
        }

        private PropertyObject setIndex(int index) {
            this.index = index;
            return this;
        }

        public PropertyObject newProperty(String name) {
            PropertyObject propertyObject = mapObjects.get(name);
            List<PropertyObject> objectList = this.listObjects.get(name);
            if (propertyObject == null && objectList == null) {
                PropertyObject newProperty = new PropertyObject(name, this);
                this.mapObjects.put(name, newProperty);
                return newProperty;
            }

            if (objectList == null) {
                objectList = new ArrayList<>();
                this.listObjects.put(name, objectList);
            }
            if (propertyObject != null) {
                mapObjects.remove(name);
                objectList.add(objectList.size(), propertyObject.setIndex(objectList.size()));
            }
            PropertyObject newProperty = new PropertyObject(name, this);
            objectList.add(objectList.size(), newProperty.setIndex(objectList.size()));
            return newProperty;
        }

        public PropertyObject setAttribute(String attributeName, String value) {
            this.attributes.put(attributeName, value);
            return this;
        }

        private String makeKey(String parentPath, String subKey) {
            if (StringUtils.isEmpty(parentPath)) return subKey;
            return parentPath + "." + subKey;
        }

        private void setIncludeRoot() {
            String includeRoot = this.attributes.remove(this.rootAttribute);
            if (includeRoot != null) {
                try {
                    if (Boolean.valueOf(includeRoot)) {
                        return;
                    }
                } catch (Exception ex) {

                }
            }
            this.name = null;
            this.value = null;
        }

        private String path() {
            if (this.name == null) return "";
            if (this.parent == null) {
                if (this.index == -1)
                    return this.name;
                return this.name + "[" + this.index + "]";
            }
            if (this.index == -1)
                return this.makeKey(this.parent.path(), this.name);
            return this.parent.path() + "." + this.name + "[" + this.index + "]";
        }

        public Properties properties() {
            if (this.isRoot) {
                this.setIncludeRoot();
            }
            Properties properties = new Properties();
            String thisPath = this.path();
            if (this.mapObjects.isEmpty() && this.listObjects.isEmpty())
                if (this.value != null && !StringUtils.isEmpty(this.value.trim()))
                    properties.setProperty(thisPath, this.value);
            this.attributes.forEach((k, v) -> properties.setProperty(this.makeKey(thisPath, k), v));

            this.mapObjects.forEach((s, propertyObject) ->
                    propertyObject.properties().forEach((o, o2) ->
                            properties.setProperty((String) o, (String) o2)));

            this.listObjects.forEach((s, propertyObjects) ->
                    propertyObjects.forEach(propertyObject ->
                            propertyObject.properties().forEach((o, o2) ->
                                    properties.setProperty((String) o, (String) o2))));

            Properties newProperties=new Properties();
            newProperties.setProperty("config1.config2[1].name","a");
            newProperties.setProperty("config1.config2[0].name","a");
            newProperties.setProperty("config3.config11[0].name","a");
            newProperties.setProperty("config3.config11[1].name","a");

            return properties;
        }
    }
}
