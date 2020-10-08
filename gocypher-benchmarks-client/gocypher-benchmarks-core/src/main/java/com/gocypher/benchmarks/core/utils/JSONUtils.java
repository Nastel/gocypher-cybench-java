package com.gocypher.benchmarks.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtils {
    private static Logger LOG = LoggerFactory.getLogger(JSONUtils.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static Map<?, ?> parseJsonIntoMap(String jsonString) {
        try {
            return mapper.readValue(jsonString, HashMap.class);
        } catch (Exception e) {
            LOG.error("Error on parsing json into map", e);
            return new HashMap<>();
        }
    }
    public static List<?> parseJsonIntoList(String jsonString) {
        try {
            return mapper.readValue(jsonString, ArrayList.class);
        } catch (Exception e) {
            LOG.error("Error on parsing json into map", e);
            return new ArrayList<>();
        }
    }
    public static String marshalToJson(Object item) {
        try {
            return mapper.writeValueAsString(item);
        } catch (Exception e) {
            LOG.error ("Error on marshaling to json",e) ;
            return "";
        }
    }
    public static String marshalToPrettyJson(Object item) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);
        } catch (Exception e) {
           e.printStackTrace();
           LOG.error ("Error on storing results",e) ;
           return "";
        }
    }
}
