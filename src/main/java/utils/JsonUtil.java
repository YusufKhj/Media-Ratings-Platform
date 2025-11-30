package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            System.out.println("JsonUtil: Parsing JSON: " + json);
            System.out.println("JsonUtil: Target class: " + clazz.getName());
            T result = mapper.readValue(json, clazz);
            System.out.println("JsonUtil: Erfolgreich geparst!");
            return result;
        } catch (Exception e) {
            System.out.println("!!!!!!!!!! JSON PARSE FEHLER !!!!!!!!!!");
            System.out.println("JSON: " + json);
            System.out.println("Class: " + clazz.getName());
            e.printStackTrace(System.out);
            throw new RuntimeException("JSON Parse Error: " + e.getMessage(), e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            System.out.println("!!!!!!!!!! JSON WRITE FEHLER !!!!!!!!!!");
            e.printStackTrace(System.out);
            throw new RuntimeException("JSON Write Error: " + e.getMessage(), e);
        }
    }
}