package com.acmeplatform.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigReader - Reads configuration from properties files.
 * 
 * Pain points demonstrated:
 * - Manual property file management
 * - No type safety for configuration values
 * - Environment switching requires different property files
 * - Credentials stored in plain text property files
 */
public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static Properties properties;
    private static final String CONFIG_PATH = "src/test/resources/config/";

    public static void loadConfig(String environment) {
        properties = new Properties();
        String configFile = CONFIG_PATH + environment + ".properties";

        try {
            FileInputStream fis = new FileInputStream(configFile);
            properties.load(fis);
            fis.close();
            logger.info("Configuration loaded from: " + configFile);
        } catch (IOException e) {
            logger.error("Failed to load config file: " + configFile);
            // Fallback to default config
            try {
                FileInputStream fis = new FileInputStream(CONFIG_PATH + "config.properties");
                properties.load(fis);
                fis.close();
                logger.info("Loaded default configuration");
            } catch (IOException ex) {
                throw new RuntimeException("Cannot load any configuration file: " + ex.getMessage());
            }
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property not found: " + key);
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer property: " + key + " = " + value);
            }
        }
        return defaultValue;
    }

    public static String getBaseUrl() {
        return getProperty("base.url");
    }

    public static String getUsername() {
        return getProperty("username");
    }

    public static String getPassword() {
        return getProperty("password");
    }

    public static String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public static String getApiKey() {
        return getProperty("api.key");
    }
}
