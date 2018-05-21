package com.mocomsys.c4i.preparation.makeTableSql;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;

public class MakeC4ITableMain{

	private POIFSFileSystem fs = null;
	private XSSFWorkbook workbook = null;
	private String excelFile = System.getProperty("excelFilePath");

	private int MIAdapterDataSheetNum = -1;
	private int interfaceDataSheetNum = -1;
	private int moniteringDataSheetNum = -1;
	private int convertModuleDataSheetNum = -1;

	private static Logger logger = Logger.getLogger(MakeC4ITableMain.class.getName());

	public static void main(String[] args) {
		MakeC4ITableMain mc4i = new MakeC4ITableMain();
	}

	public MakeC4ITableMain() {
		prepareExcelData();
		makeTableSql(workbook, MIAdapterDataSheetNum);
		makeTableSql(workbook, interfaceDataSheetNum);
		// makeTableSql(workbook, moniteringDataSheetNum);
		// makeTableSql(workbook, convertModuleDataSheetNum);
	}

	public void prepareExcelData() {

		try {
			workbook = new XSSFWorkbook(new FileInputStream(excelFile));

			int sheetNum = workbook.getNumberOfSheets();

			for (int i = 0; i < sheetNum; i++) {
				String sheetName = workbook.getSheetName(i);

				if (sheetName.equalsIgnoreCase("MI 어댑터 기본 설정정보")) {
					MIAdapterDataSheetNum = i;
				} else if (sheetName.equalsIgnoreCase("인터페이스 정보")) {
					interfaceDataSheetNum = i;
				} else if (sheetName.equalsIgnoreCase("모니터링")) {
					moniteringDataSheetNum = i;
				} else if (sheetName.equalsIgnoreCase("RVRD, NCross 변환모듈 기본 설정정보")) {
					convertModuleDataSheetNum = i;
				}

				if (MIAdapterDataSheetNum != -1 && interfaceDataSheetNum != -1 && moniteringDataSheetNum != -1
						&& convertModuleDataSheetNum != -1) {
					break;
				}
			}

			logger.info(" MIAdapterDataSheetNum = " + MIAdapterDataSheetNum + ", intergaceDataSheetNum = "
					+ interfaceDataSheetNum + ", miniteringDataSheetNum = " + moniteringDataSheetNum
					+ ", convertModuleDataSheetNum = " + convertModuleDataSheetNum);

			if (MIAdapterDataSheetNum == -1 || interfaceDataSheetNum == -1 || moniteringDataSheetNum == -1
					|| convertModuleDataSheetNum == -1) {
				logger.error("MI 어댑터 기본 설정정보/인터페이스 정보/모니터링/RVRD, NCross 변환모듈 기본 설정정보 시트가 없습니다.");
				System.exit(0);
			}

		} catch (Exception e) {
			e.getMessage();
		}

	}

	public void makeTableSql(XSSFWorkbook workbook, int sheetNum) {

		XSSFSheet sheet = workbook.getSheetAt(sheetNum);
		int sheetLastRowNum = sheet.getLastRowNum();
		List<Integer> tableInfoStart = new ArrayList();

		for (int i = 0; i < sheetLastRowNum; i++) {
			if (sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null) {
				if (sheet.getRow(i).getCell(0).getCellTypeEnum() == CellType.STRING) {
					String ACell = sheet.getRow(i).getCell(0).getStringCellValue();
					if (ACell.equalsIgnoreCase("시스템")) {
						tableInfoStart.add(i);
					}
				}
			}
		}

		getTableInfo(sheet, tableInfoStart);

		logger.info(sheet.getSheetName() + " : " + tableInfoStart.toString());

	}

	public void getTableInfo(XSSFSheet sheet, List<Integer> tableInfoStart) {
		TableInfo tableInfo = new TableInfo();

		for (int tableCell : tableInfoStart) {

			logger.info("#####################################################################");
			logger.info("  테이블 이름 : " + sheet.getRow(tableCell).getCell(4).getStringCellValue());
			logger.info("#####################################################################");

			try {
				tableInfo.setSystemName(sheet.getRow(tableCell).getCell(2).getStringCellValue());
				tableInfo.setTableId(sheet.getRow(tableCell).getCell(4).getStringCellValue());
				tableInfo.setTableName(sheet.getRow(tableCell).getCell(7).getStringCellValue());
				tableInfo.setTableSort(sheet.getRow(tableCell).getCell(10).getStringCellValue());
				tableInfo.setTableDescription(sheet.getRow(tableCell + 1).getCell(2).getStringCellValue());
				tableInfo.setRowLength((int) sheet.getRow(tableCell + 1).getCell(7).getNumericCellValue());
				tableInfo.setAuthor(sheet.getRow(tableCell + 1).getCell(10).getStringCellValue());

				int columnNum = tableCell + 4;

				Map<String, ColumnInfo> mColumnInfo = new LinkedHashMap<String, ColumnInfo>();

				while (sheet.getRow(columnNum).getCell(0) != null) {
					mColumnInfo = getColumnInfo(sheet, columnNum, mColumnInfo);
					columnNum++;
				}

				tableInfo.setmColumnInfo(mColumnInfo);

				// ----tableInfo로 sql 만들 부분
				makeSqlFile(tableInfo);
				
				
			} catch (NullPointerException e) {

			}

			// logger.info(tableInfo.toString());

		}	

	}

	public Map<String, ColumnInfo> getColumnInfo(XSSFSheet sheet, int columnNum, Map<String, ColumnInfo> mColumnInfo) {

		ColumnInfo columnInfo = new ColumnInfo();

		try {
			columnInfo.setSeq("" + (int) sheet.getRow(columnNum).getCell(0).getNumericCellValue());
			columnInfo.setColumnId(sheet.getRow(columnNum).getCell(1).getStringCellValue());
			columnInfo.setColumnName(sheet.getRow(columnNum).getCell(3).getStringCellValue());
			columnInfo.setDataType(sheet.getRow(columnNum).getCell(5).getStringCellValue());
			columnInfo.setLength((int) sheet.getRow(columnNum).getCell(6).getNumericCellValue());
			columnInfo.setPK(sheet.getRow(columnNum).getCell(7).getStringCellValue());
			columnInfo.setNullChk(sheet.getRow(columnNum).getCell(8).getStringCellValue());

			switch (sheet.getRow(columnNum).getCell(9).getCellTypeEnum()) {
			case STRING:
				columnInfo.setDefaultData(sheet.getRow(columnNum).getCell(9).getStringCellValue());
				break;
			case NUMERIC:
				columnInfo.setDefaultData("" + (int) sheet.getRow(columnNum).getCell(9).getNumericCellValue());
				break;
			}

			switch (sheet.getRow(columnNum).getCell(10).getCellTypeEnum()) {
			case STRING:
				columnInfo.setColumnDescrition(sheet.getRow(columnNum).getCell(10).getStringCellValue());
				break;
			case NUMERIC:
				columnInfo.setColumnDescrition("" + (int) sheet.getRow(columnNum).getCell(10).getNumericCellValue());
				break;
			}

		} catch (NullPointerException e) {

		}

		// logger.info(columnInfo.toString());

		System.out.println(columnInfo.getColumnName());

		mColumnInfo.put(columnInfo.getSeq(), columnInfo);

		return mColumnInfo;

	}

	public void makeSqlFile(TableInfo tableInfo) {

		List<String> PKList = new ArrayList<String>();

		String createSqlStatement = "DROP TABLE " + tableInfo.getTableId() + "\n/\n\nCREATE TABLE "
				+ tableInfo.getTableId() + "(\n";

		for (Map.Entry<String, ColumnInfo> entry : tableInfo.getmColumnInfo().entrySet()) {
			ColumnInfo columnInfo = entry.getValue();
			createSqlStatement += "\t" + columnInfo.getColumnId();


				if (columnInfo.getDataType()!=null && columnInfo.getDataType().equalsIgnoreCase("DATE")) {
					createSqlStatement += "\t" + columnInfo.getDataType();
				} else {
					createSqlStatement += "\t" + columnInfo.getDataType() + "(" + columnInfo.getLength() + ")";
				}

				if (columnInfo.getDefaultData() != null) {
					createSqlStatement += "\tDEFAULT " + columnInfo.getDefaultData();
				}

				if (columnInfo.getNullChk() != null && columnInfo.getNullChk().equalsIgnoreCase("N")) {
					createSqlStatement += "\tNOT NULL";
				}

				createSqlStatement += ",\n";

				if (columnInfo.getPK() != null && columnInfo.getPK().equalsIgnoreCase("Y")) {
					PKList.add(columnInfo.getColumnId());
				}

		}

		switch (PKList.size()) {
		case 0:
			break;
		case 1:
			createSqlStatement += "\tCONSTRAINT PK_" + tableInfo.getTableId() + " PRIMARY KEY (" + PKList.get(0)
					+ ")\n";
			break;
		default:
			createSqlStatement += "\tCONSTRAINT PK_" + tableInfo.getTableId() + " PRIMARY KEY (";
			for (String PK : PKList) {
				createSqlStatement += PK + ",";
			}
			createSqlStatement = createSqlStatement.substring(0, createSqlStatement.length() - 1) + ")\n";
			break;
		}

		createSqlStatement += ")";

		System.out.println(createSqlStatement);

	}

}
