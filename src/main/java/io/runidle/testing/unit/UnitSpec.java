package io.runidle.testing.unit;

import io.runidle.testing.utils.XmlPropertySourceLoader;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;
import java.io.Reader;

public interface UnitSpec {
    Logger log = LoggerFactory.getLogger(UnitSpec.class);

    YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
    PropertiesPropertySourceLoader propertiesPropertySourceLoader = new PropertiesPropertySourceLoader();
    XmlPropertySourceLoader xmlPropertySourceLoader = new XmlPropertySourceLoader();

    default boolean exists(String confFile) {
        ClassPathResource classPathResource = new ClassPathResource(confFile);
        return classPathResource.exists();
    }

    static MutablePropertySources propertySources(String... confFiles) {
        MutablePropertySources propertySources = new MutablePropertySources();
        for (String confFile : confFiles) {
            ClassPathResource classPathResource = new ClassPathResource(confFile);
            if (!classPathResource.exists()) {
                log.info("No configuration file in classpath for file name: " + confFile);
                continue;
            }

            try {
                PropertySource<?> propertySource = propertySourceLoader(confFile)
                        .load(confFile, classPathResource, null);
                if (propertySource != null)
                    propertySources.addFirst(propertySource);
            } catch (Exception ex) {
                log.info("Failed to load configuration file: {}, ex: {}", confFile, ex.getMessage());
            }
        }
        return propertySources;
    }

    default Reader readFile(String fileName) {
        return new InputStreamReader(new ByteBufInputStream(
                Vertx.vertx().fileSystem().readFileBlocking(fileName).getByteBuf()));
    }

    default ByteBuf readFileB(String fileName) {
        return Vertx.vertx().fileSystem().readFileBlocking(fileName).getByteBuf();
    }

    default byte[] readBytes(String fileName) {
        return Vertx.vertx().fileSystem().readFileBlocking(fileName).getBytes();
    }

    default String readString(String fileName) {
        return new String(Vertx.vertx().fileSystem().readFileBlocking(fileName).getBytes());
    }

    static PropertySourceLoader propertySourceLoader(String confFile) {
        if (StringUtils.endsWithIgnoreCase(confFile, ".xml"))
            return xmlPropertySourceLoader;
        if (StringUtils.endsWithIgnoreCase(confFile, ".yml"))
            return yamlPropertySourceLoader;
        if (StringUtils.endsWithIgnoreCase(confFile, ".yaml"))
            return yamlPropertySourceLoader;
        return propertiesPropertySourceLoader;
    }


    default <T> T loadConfiguration(Class<T> tClass, String... confFiles) {
        if (confFiles == null) return null;
        Object target;
        try {
            target = tClass.newInstance();
            PropertiesConfigurationFactory<Object> factory = new PropertiesConfigurationFactory<>(
                    target);
            factory.setIgnoreInvalidFields(false);
            factory.setIgnoreUnknownFields(false);
            factory.setExceptionIfInvalid(true);
            MutablePropertySources propertySources = propertySources(confFiles);
            factory.setPropertySources(propertySources);
            factory.bindPropertiesToTarget();
        } catch (Exception ex) {
            log.info("Failed to load configuration files: {}, ex: {}", confFiles, ex);
            return null;
        }

        return (T) target;
    }

    /**
     * for the unit test static beforeClass
     *
     * @param setup
     */
    static void setup(Setup setup) {
        setup.doSetup(new UnitSpec() {
        });
    }

    interface Setup {
        void doSetup(UnitSpec unitSpec);
    }

    default String toString(Object obj) {
        return toSString(obj);
    }

    ObjectMapper mapper1 = new ObjectMapper();
    ObjectMapper mapper = mapper1.setVisibility(mapper1.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY));


    static String toSString(Object obj) {
        if (obj instanceof String) return (String) obj;
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception ex) {
            log.warn("Failed to format object of class: {}, ex: {}", obj.getClass(), ex.getMessage());
            return "";
        }
//        return JSON.toJSONString(obj,
//                (ValueFilter) (o, s, v) -> {
//                    if (v == null)
//                        return "null";
//                    return v;
//                },
//                SerializerFeature.PrettyFormat,
//                SerializerFeature.SortField,
//                SerializerFeature.WriteNullStringAsEmpty,
//                SerializerFeature.WriteNonStringKeyAsString,
//                SerializerFeature.WriteMapNullValue);
    }
}
