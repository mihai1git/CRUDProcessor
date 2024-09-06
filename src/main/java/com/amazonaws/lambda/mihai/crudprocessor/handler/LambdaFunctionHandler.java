package com.amazonaws.lambda.mihai.crudprocessor.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.lambda.mihai.crudprocessor.model.DynamoTable;
import com.amazonaws.lambda.mihai.crudprocessor.service.DynamoService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import software.amazon.lambda.powertools.logging.Logging;

public class LambdaFunctionHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	
	private Logger logger = LogManager.getLogger(LambdaFunctionHandler.class);

	public DynamoService dynamoService;

    public LambdaFunctionHandler() {
    	dynamoService = DynamoService.build();
    	
    }

    // Test purpose only.
    public LambdaFunctionHandler(DynamoService dynamoSrv) {
    	dynamoService = dynamoSrv;
    }

    @Override
    @Logging(logEvent = true,correlationIdPath = "/headers/x-amzn-trace-id")
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        //logger.debug("Received event: " + event);
        
        try {

        	String routeKey = event.getRouteKey().substring(0, event.getRouteKey().indexOf('/')).trim();
        	logger.debug("Received HTTP method: " + routeKey);
        	        	
        	Map<String, String> queryParams = event.getQueryStringParameters();
        	DynamoTable tableDetails = new DynamoTable();
        	tableDetails.setTableName(queryParams.get("table"));
        	tableDetails.setTablePKName(queryParams.get("pk"));
        	tableDetails.setTableSKName(queryParams.get("sk"));
        	tableDetails.setTablePKValue(queryParams.get(queryParams.get("pk")));
        	tableDetails.setTableSKValue(queryParams.get(queryParams.get("sk")));
        	
        	String response = null;
            
        	// the switch is done on HTTP method type from route, for all API GTW Routes
        	// Lambda should be reused on multiple routes
            switch (routeKey) {
            case "PUT" :
            	tableDetails.setPrimaryKeyEnabled(Boolean.TRUE);
            	try {
            		dynamoService.createRecord(tableDetails, event.getBody());
            		response = "{\"result\":\"success create\"}";
            		
            	} catch (RuntimeException ex) {
            		response = "{\"result\":\"record was not created\"}";
            	}
                break;
            case "GET":
            	response = dynamoService.readRecords(tableDetails);
            	if (response == null) response = "{\"result\":\"there is no such item\"}";
                break;
            case "POST" :
            	dynamoService.updateRecord(tableDetails, event.getBody());
            	response = "{\"result\":\"success update\"}";
            	break;
            case "DELETE":
            	dynamoService.deleteRecord(tableDetails);
            	response = "{\"result\":\"success delete\"}";
                break;
            default:
                throw new RuntimeException("unexpected HTTP method / routeKey");
        }		
        	
        	
        	Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(headers)
                    .withIsBase64Encoded(false)
                    .withBody(response)
                    .build();
                        
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

}