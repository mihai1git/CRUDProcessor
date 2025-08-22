package com.amazonaws.lambda.mihai.crudprocessor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.amazonaws.lambda.mihai.crudprocessor.model.DynamoTable;
import com.amazonaws.lambda.mihai.crudprocessor.test.data.DynamoData;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;


/**
 * A test that ensure correct mocking of the DynamoDB client, that works without any DynamoDB database.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DynamoDataTest {
	
	private DynamoDB dynamoClient = Mockito.mock(DynamoDB.class);
	private AmazonDynamoDB dynamoDBClient = Mockito.mock(AmazonDynamoDB.class);
	
	public DynamoDataTest () {
		//AWS SDK JAVA 1 will be deprecated, needs to be replaced with AWS SDK JAVA 2 in future versions 
    	System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
	}

    @BeforeEach
    public void setUp() throws IOException {
       
    	DynamoData.resetDynamoData(dynamoClient, dynamoDBClient);
    }
    
    @Test
    @DisplayName("Ensure correct interaction with Dynamo Client, Table.getItem")
    public void testDynamoMockGetPrimaryKey() throws IOException {
    	
    	Map<String, String> jsonItemMap = DynamoData.getBloodPressurePrototypeItem ();
    	
    	DynamoData.addBloodPressureItem(jsonItemMap);
    	
    	DynamoTable tableDetails = new DynamoTable();
    	tableDetails.setTableName(DynamoData.TAB_NAME_BLOOD_PRESSURE);
    	tableDetails.setTablePKName("person");
    	tableDetails.setTableSKName("dt");
    	tableDetails.setTablePKValue("Mihai");
    	tableDetails.setTableSKValue("2024-10-11 12:24:25");
    	
        Table table = getDynamoClient().getTable(tableDetails.getTableName());
        
        PrimaryKey itemKey = new PrimaryKey();
        itemKey.addComponent(tableDetails.getTablePKName(), tableDetails.getTablePKValue());
        itemKey.addComponent(tableDetails.getTableSKName(), tableDetails.getTableSKValue());
        		
        Item item = table.getItem(itemKey);
        
        if (item != null) {
            Map nodes = item.asMap();
            
        	System.out.println("got item: " + Arrays.toString(nodes.entrySet().toArray()));
        	
        	assertEquals("Mihai", nodes.get("person"), "was added item for person Mihai");
        }
    }
    
    @Test
    @DisplayName("Ensure correct interaction with Dynamo Client, Table.query")
    public void testDynamoMockQuery() throws IOException {
    	
    	Map<String, String> jsonItemMap = DynamoData.getBloodPressurePrototypeItem ();
    	
    	DynamoData.addBloodPressureItem(jsonItemMap);
    	jsonItemMap.put("dt", "2024-10-11 12:24:28");
    	DynamoData.addBloodPressureItem(jsonItemMap);
    	
    	DynamoTable tableDetails = new DynamoTable();
    	tableDetails.setTableName(DynamoData.TAB_NAME_BLOOD_PRESSURE);
    	tableDetails.setTablePKName("person");
    	tableDetails.setTablePKValue("Mihai");
    	
    	Table table = getDynamoClient().getTable(tableDetails.getTableName());

    	QuerySpec spec = new QuerySpec()
    			.withHashKey(tableDetails.getTablePKName(), tableDetails.getTablePKValue());

    	ItemCollection<QueryOutcome> items = table.query(spec);
    	
    	if (items.firstPage().hasNextPage()) throw new RuntimeException("multiple pages NOT implemented !!!");
    	
    	List<Item> itemsList = items.firstPage().getLowLevelResult().getItems();
    	    	
    	System.out.println("queried items: " + Arrays.toString(itemsList.toArray()));
    	
    	assertEquals(2, itemsList.size(), "above were added 2 items in DynamoData");
    }

	public DynamoDB getDynamoClient() {
		return dynamoClient;
	}

	public void setDynamoClient(DynamoDB dynamoClient) {
		this.dynamoClient = dynamoClient;
	}
}
