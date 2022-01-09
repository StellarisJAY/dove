package com.jay.dove.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 14:34
 */
public class ConfigManager {
    private static Properties properties;
    static{
        try(InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream("dove.properties")){
            properties.load(inputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Boolean getBoolean(String name){
        String property = get(name);
        return property == null ? null : Boolean.parseBoolean(property);
    }

    public static Integer getInteger(String name){
        return Integer.parseInt(properties.getProperty(name));
    }

    public static String get(String name){
        return properties.getProperty(name);
    }
}
