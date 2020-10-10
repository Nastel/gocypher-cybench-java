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

package com.gocypher.benchmarks.runner.report;

import com.gocypher.benchmarks.runner.utils.Constants;
import com.gocypher.benchmarks.runner.utils.JSONUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeliveryService {
    private static final Logger LOG = LoggerFactory.getLogger(DeliveryService.class) ;
    private static DeliveryService instance ;

    private static final String serviceUrl = "https://www.gocypher.com/gocypher-benchmarks-reports/services/v1/reports/report" ;
    //private static final String serviceUrl = "http://localhost:8080/gocypher-benchmarks-reports/services/v1/reports/report" ;
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    private DeliveryService (){

    }
    public static DeliveryService getInstance (){
        if (instance == null){
            instance = new DeliveryService () ;
        }
        return instance ;
    }


    public String sendReportForStoring (String reportJSON){
        try {
            LOG.info("-->Sending benchmark report to URL {}", serviceUrl);
            HttpPost request = new HttpPost(serviceUrl);
            //request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            request.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
            request.setHeader(HttpHeaders.ACCEPT,"application/json");
            StringEntity se = new StringEntity(reportJSON) ;
            request.setEntity(se);

            CloseableHttpResponse response = httpClient.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            response.close();

            //LOG.info("Storing result: {}",result);
            result = JSONUtils.parseJsonIntoMap(result).get(Constants.URL_LINK_TO_GOCYPHER_REPORT).toString();
            LOG.info("Benchmark report submitted successfully to {}", Constants.URL_LINK_TO_GOCYPHER_REPORT);
            LOG.info("Your report is available at {}", result);
            LOG.info("NOTE: It may take a few minutes for your report to appear online");
            return result;
        }catch (Exception e){
            LOG.error("Failed to submit report to URL {}", serviceUrl, e);
        }
        return "";
    }

}
