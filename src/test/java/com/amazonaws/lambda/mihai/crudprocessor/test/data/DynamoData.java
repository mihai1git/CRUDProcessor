package com.amazonaws.lambda.mihai.crudprocessor.test.data;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.amazonaws.lambda.mihai.crudprocessor.handler.LambdaFunctionHandler;
import com.amazonaws.lambda.mihai.crudprocessor.model.DynamoTable;
import com.amazonaws.lambda.mihai.crudprocessor.service.DynamoService;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

public class DynamoData {
	
	private static Logger logger = LogManager.getLogger(DynamoData.class);
	
	public static final String TAB_NAME_BLOOD_PRESSURE = "BloodPressure";
	public static final String TAB_NAME_BLOOD_TEMPERATURE = "Temperature";

	public static Table tabBloodPressure;
	public static List<Item> tabBloodPressureItemsList;
	public static Collection<Map<String, AttributeValue>> tabBloodPressureItemsCollection;
	public static Table tabTemperature;
	
	public static void resetDynamoData (DynamoDB dynamoClient) {
		logger.debug("resetDynamoData");
		tabBloodPressure = Mockito.mock(Table.class);
		tabBloodPressureItemsList = new ArrayList<Item>();
		tabBloodPressureItemsCollection = new ArrayList<Map<String, AttributeValue>>();
		tabTemperature = Mockito.mock(Table.class);
		
		when(dynamoClient.getTable(DynamoData.TAB_NAME_BLOOD_PRESSURE)).thenReturn(tabBloodPressure);
		when(dynamoClient.getTable(DynamoData.TAB_NAME_BLOOD_TEMPERATURE)).thenReturn(tabTemperature);
		
		when(tabBloodPressure.putItem(Mockito.any(Item.class))).thenAnswer(new Answer<PutItemOutcome>() {
			
		     public PutItemOutcome answer(InvocationOnMock invocation) throws Throwable {
		    	 
		    	 Map<String, String> jsonItemMap = new HashMap<String, String>();
		    	((Item)invocation.getArguments()[0]).asMap().entrySet().forEach(entry -> jsonItemMap.put(entry.getKey(), (String)entry.getValue()));
		    			    	 
		    	 addBloodPressureItem (jsonItemMap);
		    	 
		    	 return new PutItemOutcome(new PutItemResult());
		     }
		 });
		
		when(tabBloodPressure.putItem(Mockito.any(PutItemSpec.class))).thenAnswer(new Answer<PutItemOutcome>() {
			
		     public PutItemOutcome answer(InvocationOnMock invocation) throws Throwable {
		    	 
		    	 Map<String, String> jsonItemMap = new HashMap<String, String>();		    			 
		    	((PutItemSpec)invocation.getArguments()[0]).getItem().asMap().entrySet().forEach(entry -> jsonItemMap.put(entry.getKey(), (String)entry.getValue()));
		    	
		    	String conditionExpr = ((PutItemSpec)invocation.getArguments()[0]).getConditionExpression();
		    	if (conditionExpr.contains("attribute_not_exists")) {
		        	
		        	if (getBloodPressureItem(jsonItemMap) != null) throw new ConditionalCheckFailedException("Item already exists: " + Arrays.toString(jsonItemMap.entrySet().toArray())); 
		    	}
		    	
		    	 addBloodPressureItem (jsonItemMap);
		    	 
		    	 return new PutItemOutcome(new PutItemResult());
		     }
		 });
		
		when(tabBloodPressure.deleteItem(Mockito.any(PrimaryKey.class))).thenAnswer(new Answer<DeleteItemOutcome>() {
			
		     public DeleteItemOutcome answer(InvocationOnMock invocation) throws Throwable {
		    	 
		    	 Map<String, String> jsonItemMap = new HashMap<String, String>();		    			 
		    	((PrimaryKey)invocation.getArguments()[0]).getComponents().forEach(entry -> jsonItemMap.put(entry.getName(), (String)entry.getValue()));
		    	
		    	deleteBloodPressureItem(jsonItemMap);
		    	
		    	return new DeleteItemOutcome(new DeleteItemResult());
		     }
		 });
	}
	
	public static Item getBloodPressureItem(Map<String, String> jsonItemMap) {
		
		DynamoTable tableDetails = new DynamoTable();
    	tableDetails.setTableName(TAB_NAME_BLOOD_PRESSURE);
    	tableDetails.setTablePKName("person");
    	tableDetails.setTablePKValue(jsonItemMap.get("person"));
    	tableDetails.setTableSKName("dt");
    	tableDetails.setTableSKValue(jsonItemMap.get("dt"));
    	
		Item itm = null;
    	PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
        itemKey.addComponent(tableDetails.getTableSKName(), tableDetails.getTableSKValue());
        
        itm = tabBloodPressure.getItem(itemKey);
        
        return itm;
	}
	
	public static void deleteBloodPressureItem(Map<String, String> jsonItemMap) {
		
		DynamoTable tableDetails = new DynamoTable();
    	tableDetails.setTableName(TAB_NAME_BLOOD_PRESSURE);
    	tableDetails.setTablePKName("person");
    	tableDetails.setTablePKValue(jsonItemMap.get("person"));
    	tableDetails.setTableSKName("dt");
    	tableDetails.setTableSKValue(jsonItemMap.get("dt"));
    	
    	PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
        itemKey.addComponent(tableDetails.getTableSKName(), tableDetails.getTableSKValue());
        
    	when(tabBloodPressure.getItem(itemKey)).thenReturn(null);
    	Iterator<Item> lIter = tabBloodPressureItemsList.listIterator();
    	while(lIter.hasNext()) {
    		Item itm = lIter.next();
    		if (tableDetails.getTablePKValue().equals(itm.get(tableDetails.getTablePKName())) 
    				&& tableDetails.getTablePKValue().equals(itm.get(tableDetails.getTableSKName()))) {
    			lIter.remove();
    		}
    	}

    	//TODO delete also item from query mock
	}
	
	public static void addBloodPressureItem (Map<String, String> jsonItemMap) {
		
		logger.debug("START addBloodPressureItem");
    	
    	DynamoTable tableDetails = new DynamoTable();
    	tableDetails.setTableName(TAB_NAME_BLOOD_PRESSURE);
    	tableDetails.setTablePKName("person");
    	tableDetails.setTablePKValue(jsonItemMap.get("person"));
    	tableDetails.setTableSKName("dt");
    	tableDetails.setTableSKValue(jsonItemMap.get("dt"));
    	
    	
    	// mock getItem
    	Item itm = Mockito.mock(Item.class);
    	Map<String, Object> item = new HashMap<String, Object>();
    	
    	jsonItemMap.entrySet().forEach(entry -> item.put(entry.getKey(), entry.getValue()));
//    	System.out.println(Arrays.toString(item.entrySet().toArray()));
    	PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
        itemKey.addComponent(tableDetails.getTableSKName(), tableDetails.getTableSKValue());
    	when(tabBloodPressure.getItem(itemKey)).thenReturn(itm);
    	when(itm.asMap()).thenReturn(item);
    	
    	//mock query
    	
    	tabBloodPressureItemsList.add(itm);
    	
    	
    	Map<String, AttributeValue> itemAV = new HashMap<String, AttributeValue>();
    	jsonItemMap.entrySet().forEach(entry -> itemAV.put(entry.getKey(), new AttributeValue(entry.getValue()) ));
    	tabBloodPressureItemsCollection.add(itemAV);
    	
    	KeyAttribute key = new KeyAttribute(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
    	    	
    	//System.out.println("tabBloodPressure key: " + key + " collection: " + Arrays.toString(tabBloodPressureItemsCollection.toArray()));
    	
//    	if (tabBloodPressure.query(new QuerySpec().withHashKey(key)) == null) {
    		
    		
        	QueryOutcome res = new QueryOutcome(new QueryResult());
        	res.getQueryResult().setItems(tabBloodPressureItemsCollection);
            
        	Page<Item, QueryOutcome> page = new Page(tabBloodPressureItemsList, res) {
    			
    			@Override
    			public Page<Item, QueryOutcome> nextPage() {
    				// TODO Auto-generated method stub
    				return null;
    			}
    			
    			@Override
    			public boolean hasNextPage() {
    				// TODO Auto-generated method stub
    				return false;
    			}
    		};
    		ItemCollection<QueryOutcome> items = Mockito.mock(ItemCollection.class);
        	when(items.firstPage()).thenReturn(page);
        	
        	        	
        	when(tabBloodPressure.query(new QuerySpec()
        			.withHashKey(
        					MockitoHamcrest.argThat( Matchers.hasProperty("hashKey", Matchers.equalTo(key))
        			)))).thenReturn(items);

//    	}
        	
        	logger.debug("END addBloodPressureItem");
    }
    
	public static void addTemperatureItem (Map<String, String> jsonItemMap) {
    	
    	DynamoTable tableDetails = new DynamoTable();
    	tableDetails.setTableName(TAB_NAME_BLOOD_TEMPERATURE);
    	tableDetails.setTablePKName("person");
    	tableDetails.setTablePKValue(jsonItemMap.get("person"));
    	tableDetails.setTableSKName("dt");
    	tableDetails.setTableSKValue(jsonItemMap.get("dt"));
    	
    	Item itm = Mockito.mock(Item.class);
    	Map<String, Object> item = new HashMap<String, Object>();
    	
    	jsonItemMap.entrySet().forEach(entry -> item.put(entry.getKey(), entry.getValue()));
//    	System.out.println(Arrays.toString(item.entrySet().toArray()));
    	PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
        itemKey.addComponent(tableDetails.getTableSKName(), tableDetails.getTableSKValue());
    	when(tabTemperature.getItem(itemKey)).thenReturn(itm);
    	when(itm.asMap()).thenReturn(item);
    }
	
	public static Map<String, String> getBloodPressurePrototypeItem () throws IOException {
    	File initialFile = new File("src/test/resources/blood-pressure.item.json");
		FileInputStream file = new FileInputStream(initialFile);
	    String jsonItem = new String(file.readAllBytes());
	    file.close();	   
	    Map<String, String> jsonItemMap = DynamoService.getKeysInJson(jsonItem);
	    //System.out.println(jsonItem);
	    
	    return jsonItemMap;
	}

}
