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

package com.gocypher.cybench.launcher.utils;

import com.gocypher.cybench.core.utils.SecurityUtils;

import java.util.HashMap;
import java.util.Map;

public class SecurityBuilder {
    private Map<String,String> mapOfHashedParts ;

    public SecurityBuilder(){
        mapOfHashedParts = new HashMap<>() ;
    }
    public void generateSecurityHashForClasses (Class<?> clazz){
        if (clazz != null) {
            String hash = SecurityUtils.computeClassHash(clazz);
            if (hash != null) {
                mapOfHashedParts.put(clazz.getName(), hash);
            }
        }
    }
    public void generateSecurityHashForReport (String report){
        String hash = SecurityUtils.computeStringHash(report) ;
        if (hash != null){
            mapOfHashedParts.put("report",hash) ;
        }
    }
    public Map<String, Object> buildSignatures (){
        Map<String,Object> map = new HashMap<>() ;
        map.putAll( this.mapOfHashedParts) ;
        return map ;
    }

    public Map<String, String> getMapOfHashedParts() {
        return mapOfHashedParts;
    }
}
