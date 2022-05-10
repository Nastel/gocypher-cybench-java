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

package com.gocypher.cybench.launcher.report;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.launcher.utils.Constants;

public class DeliveryService {
    private static final Logger LOG = LoggerFactory.getLogger(DeliveryService.class);

    private static DeliveryService instance;

    private static final String serviceUrl = System.getProperty(Constants.SEND_REPORT_URL,
            Constants.APP_HOST + "/gocypher-benchmarks-reports/services/v1/reports/report");

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private DeliveryService() {
    }

    public static DeliveryService getInstance() {
        if (instance == null) {
            instance = new DeliveryService();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public String sendReportForStoring(String reportJSON, String benchToken, String queryToken) {
        try {
            LOG.info("--> Sending benchmark report to URL {}", serviceUrl);
            // Setting content type plain text, since report json is encoded in Base64
            StringEntity se = new StringEntity(reportJSON, ContentType.TEXT_PLAIN);

            HttpPost request = new HttpPost(serviceUrl);
            request.setEntity(se);

            request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            request.setHeader("x-api-key", benchToken);
            if (StringUtils.isNotEmpty(queryToken)) {
                request.setHeader("x-api-query-key", queryToken);
            }

            LOG.debug("---> Benchmark report: {} ({})", request.getEntity().getContentType(),
                    request.getEntity().getContentLength());

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String result = EntityUtils.toString(response.getEntity());
                EntityUtils.consume(response.getEntity());
                LOG.debug("<--- Transmission response: {} ({})", response.getEntity().getContentType(),
                        response.getEntity().getContentLength());

                return result;
            }
        } catch (Throwable e) {
            LOG.error("Failed to submit report to URL {}", serviceUrl, e);
        } finally {
            LOG.info("<-- Ended transmission of benchmark report to URL {}", serviceUrl);
        }
        return "";
    }

    public void close() {
        try {
            httpClient.close();
        } catch (IOException exc) {
        }
    }
}
