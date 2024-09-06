package com.amazonaws.lambda.mihai.crudprocessor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.aspectj.lang.Aspects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.amazonaws.lambda.mihai.crudprocessor.aspect.TracingAspect;
import com.amazonaws.lambda.mihai.crudprocessor.handler.LambdaFunctionHandler;
import com.amazonaws.lambda.mihai.crudprocessor.service.DynamoService;
import com.amazonaws.lambda.mihai.crudprocessor.test.data.DynamoData;
import com.amazonaws.lambda.mihai.crudprocessor.test.utils.TestContext;
import com.amazonaws.lambda.mihai.crudprocessor.test.utils.TestUtils;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LambdaFunctionHandlerTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	
	private LambdaFunctionHandler handler;

    private DynamoDB dynamoClient = Mockito.mock(DynamoDB.class);
    
    private DynamoService dynamoSrv = new DynamoService();
        
    public LambdaFunctionHandlerTest () {
    	handler = new LambdaFunctionHandler(dynamoSrv);
    	dynamoSrv.setDynamoClient(dynamoClient);
    	
    	//AWS SDK JAVA 1 will be deprecated, needs to be replaced with AWS SDK JAVA 2 in future versions 
    	System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
    }

    @BeforeEach
    public void setUp() throws IOException {
       
    	DynamoData.resetDynamoData(dynamoClient);
    	
    	//TracingAspect aspect = Aspects.aspectOf(TracingAspect.class);
    	
    }

    private Context createContext() {
        
    	TestContext ctx = new TestContext();
        ctx.setFunctionName("crudprocessor.handler.LambdaFunctionHandler");

        return ctx;
    }
    
    @Test
    @DisplayName("Ensure correct PrimaryKey HTTP GET")
    public void testLambdaFunctionGetPrimaryKey() throws IOException {
    	
    	APIGatewayV2HTTPEvent event = TestUtils.parse("/api-gateway.event.get.json", APIGatewayV2HTTPEvent.class);
    	//System.out.println(event);
    	
    	Map<String, String> jsonItemMap = DynamoData.getBloodPressurePrototypeItem ();
	    
	    DynamoData.addBloodPressureItem(jsonItemMap);
    	    	
    	APIGatewayV2HTTPResponse response = handler.handleRequest(event, createContext());
    	
    	System.out.println("TEST RESP: " + response);
    	
    	assertEquals(6, DynamoService.getKeysInJson(new String(response.getBody().getBytes())).entrySet().size(), "For BloodPressure table there are 6 fields");
    	
    	Map<String, String> queryParams = event.getQueryStringParameters();
    	queryParams.put(queryParams.get("pk"), "Mike");
    	    	
    	response = handler.handleRequest(event, createContext());
    	System.out.println("TEST RESP: " + response);
    	
    	assertEquals(1, DynamoService.getKeysInJson(new String(response.getBody().getBytes())).entrySet().size(), "there is no such item");
    	
    }
    
    @Test
    @DisplayName("Ensure correct PartitionKey HTTP GET")
    public void testLambdaFunctionGetPartitionKey () throws IOException {
    	
    	APIGatewayV2HTTPEvent event = TestUtils.parse("/api-gateway.event.get.json", APIGatewayV2HTTPEvent.class);
    	Map<String, String> jsonItemMap = DynamoData.getBloodPressurePrototypeItem ();
    	
    	DynamoData.addBloodPressureItem(jsonItemMap);
    	
    	jsonItemMap.put("dt", "2024-10-11 12:24:28");
    	DynamoData.addBloodPressureItem(jsonItemMap);
    	
    	jsonItemMap.put("dt", "2024-10-11 12:24:30");
    	DynamoData.addBloodPressureItem(jsonItemMap);
    	
    	event.getQueryStringParameters().remove("dt");
    	
    	APIGatewayV2HTTPResponse response = handler.handleRequest(event, createContext());
    	
    	System.out.println("TEST RESP: " + response);
    	
    	JsonNode jsonNode = OBJECT_MAPPER.readTree(new String(response.getBody().getBytes()));
    	
    	assertEquals(3, jsonNode.size(), "3 items were added");
    }
    
    @Test
    @DisplayName("Ensure correct HTTP PUT")
    public void testLambdaFunctionPut () throws IOException {
    	
    	APIGatewayV2HTTPEvent eventPut = TestUtils.parse("/api-gateway.event.put.json", APIGatewayV2HTTPEvent.class);
    	assertEquals("2024-10-11 12:24:25", DynamoService.getKeysInJson(eventPut.getBody()).get("dt"));
    	    	
    	APIGatewayV2HTTPResponse response = handler.handleRequest(eventPut, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("success create", DynamoService.getKeysInJson(response.getBody()).get("result"));

    	APIGatewayV2HTTPEvent eventGet = TestUtils.parse("/api-gateway.event.get.json", APIGatewayV2HTTPEvent.class);
    	response = handler.handleRequest(eventGet, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("2024-10-11 12:24:25", DynamoService.getKeysInJson(eventPut.getBody()).get("dt"));
    	
    	//check duplicate exception on create    	
    	response = handler.handleRequest(eventPut, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("record was not created", DynamoService.getKeysInJson(response.getBody()).get("result"));
    }
    
    @Test
    @DisplayName("Ensure correct HTTP POST")
    public void testLambdaFunctionPost () throws IOException {
    	
    	APIGatewayV2HTTPEvent eventPut = TestUtils.parse("/api-gateway.event.put.json", APIGatewayV2HTTPEvent.class);
    	assertEquals("Mihai", DynamoService.getKeysInJson(eventPut.getBody()).get("person"));
    	assertEquals("2024-10-11 12:24:25", DynamoService.getKeysInJson(eventPut.getBody()).get("dt"));
    	    	
    	APIGatewayV2HTTPResponse response = handler.handleRequest(eventPut, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("success create", DynamoService.getKeysInJson(response.getBody()).get("result"));

    	APIGatewayV2HTTPEvent eventPost = TestUtils.parse("/api-gateway.event.post.json", APIGatewayV2HTTPEvent.class);
    	assertEquals("Mihai", DynamoService.getKeysInJson(eventPost.getBody()).get("person"));
    	assertEquals("2024-10-11 12:24:25", DynamoService.getKeysInJson(eventPost.getBody()).get("dt"));
    	
    	response = handler.handleRequest(eventPost, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("success update", DynamoService.getKeysInJson(response.getBody()).get("result"));
    }

    @Test
    @DisplayName("Ensure correct HTTP DELETE")
    public void testLambdaFunctionDelete () throws IOException {
    	Map<String, String> jsonItemMap = DynamoData.getBloodPressurePrototypeItem ();
	    
	    DynamoData.addBloodPressureItem(jsonItemMap);
	    
    	APIGatewayV2HTTPEvent eventGet = TestUtils.parse("/api-gateway.event.get.json", APIGatewayV2HTTPEvent.class);
    	assertEquals("Mihai", DynamoService.getKeysInJson(eventGet.getBody()).get("person"));
    	assertEquals("2024-10-11 12:24:25", DynamoService.getKeysInJson(eventGet.getBody()).get("dt"));
    	
    	APIGatewayV2HTTPResponse response = handler.handleRequest(eventGet, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("2024-10-11 12:24:25", DynamoService.getKeysInJson(response.getBody()).get("dt"));
    	
    	APIGatewayV2HTTPEvent eventDelete = TestUtils.parse("/api-gateway.event.delete.json", APIGatewayV2HTTPEvent.class);
    	assertEquals("Mihai", DynamoService.getKeysInJson(eventDelete.getBody()).get("person"));
    	assertEquals("2024-10-11 12:24:25", DynamoService.getKeysInJson(eventDelete.getBody()).get("dt"));
    	
    	response = handler.handleRequest(eventDelete, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("success delete", DynamoService.getKeysInJson(response.getBody()).get("result"));
    	    	
    	response = handler.handleRequest(eventGet, createContext());
    	System.out.println("TEST RESP: " + response);
    	assertEquals("there is no such item", DynamoService.getKeysInJson(response.getBody()).get("result"));
    }
}
