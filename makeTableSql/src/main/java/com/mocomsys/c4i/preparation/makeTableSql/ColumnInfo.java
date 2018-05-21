package com.mocomsys.c4i.preparation.makeTableSql;

public class ColumnInfo {
	private String seq;
	private String columnId;
	private String columnName;
	private String dataType;
	private int length;
	private String PK;
	private String NullChk;
	private String defaultData;
	private String columnDescrition;
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	public String getColumnId() {
		return columnId;
	}
	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getPK() {
		return PK;
	}
	public void setPK(String pK) {
		PK = pK;
	}
	public String getNullChk() {
		return NullChk;
	}
	public void setNullChk(String nullChk) {
		NullChk = nullChk;
	}
	public String getDefaultData() {
		return defaultData;
	}
	public void setDefaultData(String defaultData) {
		this.defaultData = defaultData;
	}
	public String getColumnDescrition() {
		return columnDescrition;
	}
	public void setColumnDescrition(String columnDescrition) {
		this.columnDescrition = columnDescrition;
	}
	@Override
	public String toString() {
		return "ColumnInfo [seq=" + seq + ", columnId=" + columnId + ", columnName=" + columnName + ", dataType="
				+ dataType + ", length=" + length + ", PK=" + PK + ", NullChk=" + NullChk + ", defaultData="
				+ defaultData + ", columnDescrition=" + columnDescrition + "]";
	}
	
	
}
