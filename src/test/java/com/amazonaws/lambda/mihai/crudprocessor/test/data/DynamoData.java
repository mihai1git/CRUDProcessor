package com.amazonaws.lambda.mihai.crudprocessor.test.data;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;

import com.amazonaws.lambda.mihai.crudprocessor.model.DynamoTable;
import com.amazonaws.lambda.mihai.crudprocessor.service.DynamoService;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

public class DynamoData {
	
	public static String TAB_NAME_BLOOD_PRESSURE = "BloodPressure";
	public static String TAB_NAME_BLOOD_TEMPERATURE = "Temperature";

	public static Table tabBloodPressure;
	public static List<Item> tabBloodPressureItemsList;
	public static Collection<Map<String, AttributeValue>> tabBloodPressureItemsCollection;
	public static Table tabTemperature;
	
	public static void resetDynamoData (DynamoDB dynamoClient) {
		tabBloodPressure = Mockito.mock(Table.class);
		tabBloodPressureItemsList = new ArrayList<Item>();
		tabBloodPressureItemsCollection = new ArrayList<Map<String, AttributeValue>>();
		tabTemperature = Mockito.mock(Table.class);
		
		when(dynamoClient.getTable(DynamoData.TAB_NAME_BLOOD_PRESSURE)).thenReturn(tabBloodPressure);
		when(dynamoClient.getTable(DynamoData.TAB_NAME_BLOOD_TEMPERATURE)).thenReturn(tabTemperature);
		
	}
	
	public static void addBloodPressureItem (Map<String, String> jsonItemMap) {
    	
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
