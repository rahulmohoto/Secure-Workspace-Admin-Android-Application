package com.example.secure_workspace;

import java.util.HashMap;

public class GattAttributes {

    private static HashMap<String, String> attributes = new HashMap();
    public static String Read = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    static {
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");

        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed-Indicate");

        attributes.put("6e400001-b5a3-f393-e0a9-e50e24dcca9e", "Custom Characteristics-Service");
        attributes.put("6e400003-b5a3-f393-e0a9-e50e24dcca9e", "Custom Characteristics-Notify");
        attributes.put("6e400002-b5a3-f393-e0a9-e50e24dcca9e", "Custom Characteristics-Write");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
