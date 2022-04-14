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

package com.gocypher.cybench.launcher.model;

public class TooManyAnomaliesException extends Exception {
    private static final long serialVersionUID = 8436615965019398870L;

    public TooManyAnomaliesException() {
    }

    public TooManyAnomaliesException(String message) {
        super(message);
    }

    public TooManyAnomaliesException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyAnomaliesException(Throwable cause) {
        super(cause);
    }

    public TooManyAnomaliesException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
