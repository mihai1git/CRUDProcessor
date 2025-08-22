package com.amazonaws.lambda.mihai.crudprocessor.model;

/**
 * class that holds metadata for a DynamoDB table
 * each table could holds two concepts: 
 * - child concept: main purpose of the table, identified by PK+SK
 * - parent concept: secondary purpose of the table, if exists, identified by PK only
 * @author mike
 *
 */
public class DynamoTable {

	private String tableName;
	
	private String tablePKName;
	
	private String tablePKValue;
	
	private String tableSKName;
	
	private String tableSKValue;
	
	/**
	 * FALSE if PrimaryKey constraint (PK+SK) should be used when communicating with DynamoDB 
	 * by default there are no constrains on DynamoDB PrimaryKey - the previously inserted data will be overwritten
	 */
	private Boolean primaryKeyEnabled = Boolean.FALSE;
	
	public String toString() {
		return
				"DynamoTable -> " + tableName + " "
				+ "with PK: " + tablePKName + " -> " + tablePKValue + " "
				+ "with SK: " + tableSKName + " -> " + tableSKValue + " "
				+ "with primaryKeyEnabled -> " + primaryKeyEnabled;
	}
	
	

	public Boolean getPrimaryKeyEnabled() {
		return primaryKeyEnabled;
	}



	public void setPrimaryKeyEnabled(Boolean primaryKeyEnabled) {
		this.primaryKeyEnabled = primaryKeyEnabled;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTablePKName() {
		return tablePKName;
	}

	public void setTablePKName(String tablePKName) {
		this.tablePKName = tablePKName;
	}

	public String getTablePKValue() {
		return tablePKValue;
	}

	public void setTablePKValue(String tablePKValue) {
		this.tablePKValue = tablePKValue;
	}

	public String getTableSKName() {
		return tableSKName;
	}

	public void setTableSKName(String tableSKName) {
		this.tableSKName = tableSKName;
	}

	public String getTableSKValue() {
		return tableSKValue;
	}

	public void setTableSKValue(String tableSKValue) {
		this.tableSKValue = tableSKValue;
	}
	
	
}
