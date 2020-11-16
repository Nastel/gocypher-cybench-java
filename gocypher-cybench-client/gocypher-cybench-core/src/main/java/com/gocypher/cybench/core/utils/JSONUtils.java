/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.gocypher.cybench.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

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
            LOG.error("Error on marshaling to json", e);
            return "";
        }
    }

    public static String marshalToPrettyJson(Object item) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);
        } catch (Exception e) {
            LOG.error("Error on marshal to pretty json", e);
            return "";
        }
    }

    public static String convertNumToStringByLength(String value) throws NumberFormatException {


        double v = Double.parseDouble(value);
        if (value != null) {
            if (value.indexOf(".") < 1) {
                return value;
            }
            if (Math.abs(v) > 1) {
                return convertNumToStringFrac(v, 2, 2);
            }
            if (Math.abs(v) > 0.1) {
                return convertNumToStringFrac(v, 2, 2);
            }
            if (Math.abs(v) > 0.01) {
                return convertNumToStringFrac(v, 3, 3);
            }
            if (Math.abs(v) > 0.001) {
                return convertNumToStringFrac(v, 4, 4);
            }
            if (Math.abs(v) > 0.0001) {
                return convertNumToStringFrac(v, 5, 5);
            }
            if (Math.abs(v) > 0.00001) {
                return convertNumToStringFrac(v, 6, 6);
            }
            if (v == 0) {
                return convertNumToStringFrac(v, 0, 0);
            }
            return convertNumToStringFrac(v, 6, 8);
        }
        return value;

    }

    private static String convertNumToStringFrac(Object value, int minFractionDigits, int maxFractionDigits) {

        DecimalFormat decimalFormat = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        decimalFormat.setMinimumFractionDigits(minFractionDigits);
        decimalFormat.setMinimumFractionDigits(maxFractionDigits);
        decimalFormat.setDecimalFormatSymbols(symbols);
        return decimalFormat.format(value);
    }

}
