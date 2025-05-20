package com.habbashx.config;


/**
 * Utility class that provides functionality to parse property values into their
 * appropriate data types.
 *
 * The `PropertyParser` class contains a static method to convert string
 * representations of property values into specific data types such as
 * Boolean, Integer, Double, or String based on the content and format
 * of the value.
 *
 * This class is typically used in scenarios where application configuration
 * properties or settings are stored as strings and need to be converted into
 * appropriate types for further processing.
 */
class PropertyParser {

    /**
     * Parses the given property value into its appropriate data type.
     * The method attempts to convert the input string into a Boolean,
     * Integer, or Double based on its content. If the input does not match
     * any of these types, it is returned as a String.
     *
     * @param propertyValue the string representation of the property value to parse
     * @return an Object representing the parsed value; may be a Boolean, Integer,
     *         Double, or String depending on the input
     */
    public static Object parsePropertyValue(String propertyValue){
        if (propertyValue.equalsIgnoreCase("true") || propertyValue.equalsIgnoreCase("false")){
            return Boolean.parseBoolean(propertyValue);
        } else if (propertyValue.matches("-?\\d+")){
            return Integer.parseInt(propertyValue);
        } else if (propertyValue.matches("-?\\d*\\.\\d+")){
            return Double.parseDouble(propertyValue);
        }

        return propertyValue; // string value
    }
}
