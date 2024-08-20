package com.amazonaws.lambda.mihai.crudprocessor.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.lambda.mihai.crudprocessor.model.DynamoTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DynamoService {

	private Logger logger = LogManager.getLogger(DynamoService.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private DynamoDB dynamoClient;
        
    public DynamoService() {}
    
    public static DynamoService build() {
    	
    	DynamoService srv = new DynamoService();
        
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-2").build();
        DynamoDB docClient = new DynamoDB(client);

        
    	srv.setDynamoClient(docClient);
    	
    	return srv;
    }

    /**
     * Crud
     * @param tableDetails
     * @param jsonData
     * @throws Exception
     */
    public void createRecord(DynamoTable tableDetails, String jsonData) throws Exception {
    	logger.debug("table: " + tableDetails);
    	logger.debug("jsonResult: " + jsonData);
    	
    	Map nodes = getKeysInJson(jsonData);
    	logger.debug("nodes: " + Arrays.toString(nodes.entrySet().toArray()));

        Table table = getDynamoClient().getTable(tableDetails.getTableName());
                
        PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), nodes.get(tableDetails.getTablePKName()));
        
        if (tableDetails.getTableSKName() != null) {
        	itemKey.addComponent(tableDetails.getTableSKName(), nodes.get(tableDetails.getTableSKName()));
        }	
        
        Item item = Item.fromMap(nodes)
        		.withPrimaryKey(itemKey);
        
        if (tableDetails.getPrimaryKeyEnabled()) {
        	
        	try {
            	PutItemSpec putItemSpec = new PutItemSpec()
            			.withItem(item)
            			.withConditionExpression("attribute_not_exists(" + tableDetails.getTablePKName() + ") AND attribute_not_exists(" + tableDetails.getTableSKName() + ")");
            	
                PutItemOutcome output = table.putItem(putItemSpec);
                
                logger.debug("conditional put result : " + output);
                
            } catch (ConditionalCheckFailedException  ex) {
            	//PK exception handling: do nothing because record already exists
            	//ex.printStackTrace();
            	logger.debug("ConditionalCheckFailedException: " + ex.getMessage());
            }
        
        } else {
        	PutItemOutcome output = table.putItem(item);
        	logger.debug("put result : " + output);
        }
    }
    
    /**
     * cRud
     * @param tableDetails
     * @return
     * @throws Exception
     */
	public String readRecords(DynamoTable tableDetails) throws Exception {
		
		String jsonString = null;
	    	
    	if (tableDetails.getTableSKValue() == null) {
    		jsonString = readPartitionKeyRecords(tableDetails);
    	} else {
    		jsonString = readPrimaryKeyRecord(tableDetails);
    		
    	}
	    	
    	return jsonString;
	}
    
    
    /**
     * cRud
     * @param tableDetails
     * @return
     * @throws Exception
     */
    private String readPrimaryKeyRecord(DynamoTable tableDetails) throws Exception {
    	
    	logger.debug("readPrimaryKeyRecord table: " + tableDetails);

        Table table = getDynamoClient().getTable(tableDetails.getTableName());
                
        PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
        
        if (tableDetails.getTableSKName() != null)
        	itemKey.addComponent(tableDetails.getTableSKName(), tableDetails.getTableSKValue());
        		
        Item item = table.getItem(itemKey);
        
        logger.debug("item: " + item);
        String jsonString = null;
        
        if (item != null) {
            Map nodes = item.asMap();
            
        	jsonString = getJson(nodes);
        }
    	
    	return jsonString;
    }
    
    /**
     * cRudL
     * @param tableDetails
     * @return
     * @throws Exception
     */
    private String readPartitionKeyRecords(DynamoTable tableDetails) throws Exception {
    	
    	logger.debug("readPartitionKeyRecords table: " + tableDetails);
    	
    	Table table = getDynamoClient().getTable(tableDetails.getTableName());

    	QuerySpec spec = new QuerySpec()
    			.withHashKey(tableDetails.getTablePKName(), tableDetails.getTablePKValue());

    	ItemCollection<QueryOutcome> items = table.query(spec);
    	
    	String jsonString = getKeysAsJson(items, tableDetails);
    	
    	return jsonString;
    }
    
    /**
     * crUd
     * @param tableDetails
     * @param jsonData
     * @throws Exception
     */
    public void updateRecord(DynamoTable tableDetails, String jsonData) throws Exception {
    	createRecord(tableDetails, jsonData);
    }
    
    /**
     * cruD
     * @param tableDetails
     * @throws Exception
     */
    public void deleteRecord(DynamoTable tableDetails) throws Exception {
    	logger.debug("table: " + tableDetails);

        Table table = getDynamoClient().getTable(tableDetails.getTableName());
                
        PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
        itemKey.addComponent(tableDetails.getTableSKName(), tableDetails.getTableSKValue());
        	
        
        DeleteItemOutcome res =  table.deleteItem(itemKey);
        logger.debug("result: " + res.getItem());
    }
    
    /**
     * convert JSON into a map: JSON key -> JSON value
     * @param json
     * @return
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    public static Map<String, String> getKeysInJson(String json) throws JsonMappingException, JsonProcessingException {

        Map<String, String> nodes = new HashMap<String, String>();
        JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
        Iterator<String> iterator = jsonNode.fieldNames();
        iterator.forEachRemaining(e -> {
        	nodes.put(e, jsonNode.at("/" + e).asText());
        	
        });
        return nodes;
    }
    
    /**
     * convert one DynamodDB Item into JSON
     * @param nodes
     * @return
     * @throws JsonProcessingException
     */
    private String getJson (Map nodes) throws JsonProcessingException {
    	String json = OBJECT_MAPPER.writeValueAsString(nodes);
    	    	
    	return json;
    }
    
    /**
     * convert DynamodDB Items list into JSON
     * @param items
     * @return
     * @throws JsonProcessingException
     */
    private String getJson (ItemCollection<QueryOutcome> items) throws JsonProcessingException {
    	Iterator<Item> iterator = items.iterator();
    	Item item = null;
    	String jsonString = null;
    	StringBuffer tmp = new StringBuffer("[");
    	
    	while (iterator.hasNext()) {
    		
    	    item = iterator.next();
    	    logger.debug("item : " + item); 
            if (item != null) {
                Map nodes = item.asMap();
                tmp.append(getJson(nodes)).append(",");
            }
    	}
    	if (tmp.length() > 10) {
    		tmp = tmp.deleteCharAt(tmp.length()-1).append("]");
    		jsonString = tmp.toString();
    	}
    	
    	return jsonString;
    }
    
    /**
     * convert DynamodDB Items list into JSON
     * get only keys from item
     * @param items
     * @param tableDetails
     * @return
     * @throws JsonProcessingException
     */
    private String getKeysAsJson (ItemCollection<QueryOutcome> items, DynamoTable tableDetails) throws JsonProcessingException {
    	
    	if (items.firstPage().hasNextPage()) throw new RuntimeException("multiple pages NOT implemented !!!");
    	
    	Iterator<Item> iterator = items.firstPage().getLowLevelResult().getItems().listIterator();
    	
//    	Iterator<Item> iterator = items.iterator();
    	Item item = null;
    	String jsonString = null;
    	StringBuffer tmp = new StringBuffer("[");
    	
    	while (iterator.hasNext()) {
    		
    	    item = iterator.next();
    	    logger.debug("item : " + item); 
            if (item != null) {
                Map nodes = item.asMap();
                Map<String, Object> keys = new HashMap<String, Object>();
                keys.put(tableDetails.getTablePKName(), nodes.get(tableDetails.getTablePKName()));
                keys.put(tableDetails.getTableSKName(), nodes.get(tableDetails.getTableSKName()));
                tmp.append(getJson(keys)).append(",");
            }
    	}
    	if (tmp.length() > 10) {
    		tmp = tmp.deleteCharAt(tmp.length()-1).append("]");
    		jsonString = tmp.toString();
    	}
    	
    	return jsonString;
    }


	public DynamoDB getDynamoClient() {
		return dynamoClient;
	}

	public void setDynamoClient(DynamoDB dynamoClient) {
		this.dynamoClient = dynamoClient;
	}


}
