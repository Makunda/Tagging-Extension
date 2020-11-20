/*
 * Copyright (C) 2020  Hugo JOBY
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License v3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public v3
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.castsoftware.demeter.config;

import com.castsoftware.demeter.exceptions.file.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton in charge of the communication with the configuration file
 */
public class Configuration {

    private static Properties properties = loadConfiguration();

    private static Properties loadConfiguration() {
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream("procedure.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                throw new FileNotFoundException("No file 'procedure.properties' was found.", "resources/procedure.properties", "CONFxLOAD1");
            }

            //load a properties file from class path, inside static method
            prop.load(input);
            return prop;
        } catch (IOException | FileNotFoundException ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }

        return null;
    }

    /**
     * Get the corresponding value for the specified key as a String
     * @param key
     * @see this.getAsObject to get the value as an object
     * @return <code>String</code> value for the key as a String
     */
    public static String get(String key) {
        return properties.get(key).toString();
    }

    /**
     * Get the corresponding value for the specified key as an object
     * @param key
     * @return <Object>String</code> value for the key as a string
     */
    public static Object getAsObject(String key) {
        return properties.get(key);
    }


    /**
     * Set the corresponding value for the specified key
     * @param key
     * @param value
     */
    public static void set(String key, String value){
        properties.setProperty(key, value);
    }

}