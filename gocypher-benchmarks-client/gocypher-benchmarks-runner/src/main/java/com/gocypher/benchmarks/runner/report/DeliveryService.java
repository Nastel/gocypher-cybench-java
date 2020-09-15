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
            LOG.info("-->Will send report to storage...");
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
            LOG.info("Report sent successfully for external storing.");
            LOG.info("Check report online in a few minutes via URL {}", result);
            return result;
        }catch (Exception e){
            LOG.error("Report was not sent for storing because of",e.getMessage());
        }
        return "";
    }

}
