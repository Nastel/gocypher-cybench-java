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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;

/**
 * @author slb
 * @version 1.0
 */
public class BenchmarkingContext {

    private long startTime;
    private boolean foundBenchmarks = false;
    private OptionsBuilder optBuilder;
    private SecurityBuilder securityBuilder;
    private Options options;

    private final Collection<RunResult> results = new ArrayList<>();

    private HardwareProperties hwProperties;
    private JVMProperties jvmProperties;

    private Map<String, Map<String, String>> defaultBenchmarksMetadata;

    private final Map<String, String> generatedFingerprints = new HashMap<>();
    private final Map<String, String> manualFingerprints = new HashMap<>();
    private final Map<String, String> classFingerprints = new HashMap<>();

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isFoundBenchmarks() {
        return foundBenchmarks;
    }

    public void setFoundBenchmarks(boolean foundBenchmarks) {
        this.foundBenchmarks = foundBenchmarks;
    }

    public OptionsBuilder getOptBuilder() {
        return optBuilder;
    }

    public void setOptBuilder(OptionsBuilder optBuilder) {
        this.optBuilder = optBuilder;
    }

    public SecurityBuilder getSecurityBuilder() {
        return securityBuilder;
    }

    public void setSecurityBuilder(SecurityBuilder securityBuilder) {
        this.securityBuilder = securityBuilder;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public Collection<RunResult> getResults() {
        return results;
    }

    public HardwareProperties getHWProperties() {
        return hwProperties;
    }

    public void setHWProperties(HardwareProperties hwProperties) {
        this.hwProperties = hwProperties;
    }

    public JVMProperties getJVMProperties() {
        return jvmProperties;
    }

    public void setJVMProperties(JVMProperties jvmProperties) {
        this.jvmProperties = jvmProperties;
    }

    public Map<String, Map<String, String>> getDefaultBenchmarksMetadata() {
        return defaultBenchmarksMetadata;
    }

    public void setDefaultBenchmarksMetadata(Map<String, Map<String, String>> defaultBenchmarksMetadata) {
        this.defaultBenchmarksMetadata = defaultBenchmarksMetadata;
    }

    public Map<String, String> getGeneratedFingerprints() {
        return generatedFingerprints;
    }

    public Map<String, String> getManualFingerprints() {
        return manualFingerprints;
    }

    public Map<String, String> getClassFingerprints() {
        return classFingerprints;
    }
}
