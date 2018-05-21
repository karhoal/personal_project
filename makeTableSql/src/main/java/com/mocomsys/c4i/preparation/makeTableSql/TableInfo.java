package com.mocomsys.c4i.preparation.makeTableSql;

import java.util.Map;

public class TableInfo {
	
	private String systemName;
	private String tableId;
	private String tableName;
	private String tableSort;
	private String tableDescription;
	private int rowLength;
	private String author;
	
	private Map<String, ColumnInfo> mColumnInfo;

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableSort() {
		return tableSort;
	}

	public void setTableSort(String tableSort) {
		this.tableSort = tableSort;
	}

	public String getTableDescription() {
		return tableDescription;
	}

	public void setTableDescription(String tableDescription) {
		this.tableDescription = tableDescription;
	}

	public int getRowLength() {
		return rowLength;
	}

	public void setRowLength(int rowLength) {
		this.rowLength = rowLength;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Map<String, ColumnInfo> getmColumnInfo() {
		return mColumnInfo;
	}

	public void setmColumnInfo(Map<String, ColumnInfo> mColumnInfo) {
		this.mColumnInfo = mColumnInfo;
	}

	@Override
	public String toString() {
		return "TableInfo [systemName=" + systemName + ", tableId=" + tableId + ", tableName=" + tableName
				+ ", tableSort=" + tableSort + ", tableDescription=" + tableDescription + ", rowLength=" + rowLength
				+ ", author=" + author + "]";
	}	

}
