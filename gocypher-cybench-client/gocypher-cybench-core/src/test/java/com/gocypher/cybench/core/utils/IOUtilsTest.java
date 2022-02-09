/*
 * Copyright (C) 2020-2022, K2N.IO.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package com.gocypher.cybench.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IOUtilsTest {

    @Test
    public void testJavaClassVersion() throws Exception {
        String clsVersion = IOUtils.getJavaClassFileVersion(com.google.common.cache.Cache.class);
        assertEquals("52.0", clsVersion);
        assertEquals("8", IOUtils.getJavaVersionByClassVersion(clsVersion));

        clsVersion = IOUtils.getJavaClassFileVersion("com.gocypher.cybench.core.utils.IOUtils");
        assertEquals("52.0", clsVersion);
        assertEquals("8", IOUtils.getJavaVersionByClassVersion(clsVersion));

        clsVersion = IOUtils.getJavaClassFileVersion(org.apache.commons.codec.CharEncoding.class);
        assertEquals("50.0", clsVersion);
        assertEquals("6", IOUtils.getJavaVersionByClassVersion(clsVersion));

        clsVersion = IOUtils.getJavaClassFileVersion("org.apache.commons.logging.LogFactory");
        assertEquals("46.0", clsVersion);
        assertEquals("1.2", IOUtils.getJavaVersionByClassVersion(clsVersion));
    }
}
