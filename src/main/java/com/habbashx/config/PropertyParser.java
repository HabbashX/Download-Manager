package com.habbashx.config;


class PropertyParser {

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
