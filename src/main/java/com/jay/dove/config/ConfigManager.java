package com.jay.dove.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 *  Config Manager.
 *  Holds all configs from dove.properties file.
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 14:34
 */
public class ConfigManager {
    private static final Properties PROPERTIES = new Properties();
    static{
        try(InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream("dove.properties")){
            PROPERTIES.load(inputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Boolean getBoolean(String name){
        String property = get(name);
        return property == null ? null : Boolean.parseBoolean(property);
    }

    public static Integer getInteger(String name){
        String property = get(name);
        return property == null ? null : Integer.parseInt(property);
    }

    public static String get(String name){
        return PROPERTIES.getProperty(name);
    }

    public static void set(String name, String value){
        PROPERTIES.setProperty(name, value);
    }
}
