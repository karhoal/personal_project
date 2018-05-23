package com.mocomsys.c4i.preparation.makeTableSql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;

public class MakeC4ITableMain {

	private POIFSFileSystem fs = null;
	private XSSFWorkbook workbook = null;
	private static String excelFile = null;
	private static String sqlFilePath = null;

	private TableInfo tableInfo = null; // Table데이터를 저장할 변수 선언
	private Map<String, TableInfo> mTableInfo = null; // 한 시트 내의 Table데이터들을 저장할
														// LinkedHashMap 변수 선언
	private ColumnInfo columnInfo = null; // Column데이터를 저장할 변수 선언
	private Map<String, ColumnInfo> mColumnInfo = null; // 한 Table 내의
														// Column데이터들을 저장할
														// LinkedHashMap 변수 선언

	private int MIAdapterDataSheetNum = -1;
	private int interfaceDataSheetNum = -1;
	private int moniteringDataSheetNum = -1;
	private int convertModuleDataSheetNum = -1;

	private static Logger logger = Logger.getLogger(MakeC4ITableMain.class.getName());

	static {
		if (System.getProperty("excelFilePath") == null || !(new File(System.getProperty("excelFilePath")).isFile())) {
			logger.error("excelFilePath를 확인해주세요");
			System.exit(0);
		} else {
			excelFile = System.getProperty("excelFilePath"); // 읽어올 excel파일 경로
		}

		if (System.getProperty("sqlFilePath") == null || !(new File(System.getProperty("sqlFilePath")).isDirectory())) {
			logger.error("sqlFilePath를 확인해주세요");
			System.exit(0);
		} else {
			sqlFilePath = System.getProperty("sqlFilePath"); // 생성할 sql파일 경로를 나타낼 String 변수 선언
		}
	}

	public static void main(String[] args) {
		MakeC4ITableMain mc4i = new MakeC4ITableMain();
	}

	public MakeC4ITableMain() {
		prepareExcelData();

		makeTableSql(workbook, MIAdapterDataSheetNum);
		makeTableSql(workbook, interfaceDataSheetNum);
		makeTableSql(workbook, moniteringDataSheetNum);
		makeTableSql(workbook, convertModuleDataSheetNum);
	}

	// MI 어댑터 기본 설정정보/인터페이스 정보/모니터링/RVRD, NCross 변환모듈 기본 설정정보 시트가 있는지 찾고 해당 시트 번호 입력
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
			e.printStackTrace();
		}

	}

	public void makeTableSql(XSSFWorkbook workbook, int sheetNum) {

		XSSFSheet sheet = workbook.getSheetAt(sheetNum);

		// 조회할 행 범위를 지정하기 위한 LastRowNum 변수
		int sheetLastRowNum = sheet.getLastRowNum();

		// 테이블설계서 양식에 따라, 양식 첫열 첫행의 "시스템"이 각 시트의 어디에서 나오는지 입력받을 리스트
		List<Integer> tableInfoStart = new ArrayList();

		// "시스템"이 나오는 행 위치를 리스트에 입력 후, 이후 메서드에서 행 위치에 근거한 데이터 처리...
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

		logger.info("=====================================================================");
		logger.info(sheet.getSheetName() + " - 테이블 인덱스 : " + tableInfoStart.toString());

		// 각 테이블 정보를 가져와 mTableInfo에 저장할 메서드 실행
		getTableInfo(sheet, tableInfoStart);

		// mTableInfo의 table정보를 sql파일로 생성할 메서드 실행
		makeSqlFile(mTableInfo);

	}

	public void getTableInfo(XSSFSheet sheet, List<Integer> tableInfoStart) {

		mTableInfo = new LinkedHashMap<String, TableInfo>();

		for (int tableCell : tableInfoStart) {
			tableInfo = new TableInfo();

			XSSFRow tableCellRow = sheet.getRow(tableCell);

			// logger.info("#####################################################################");
			// logger.info(" 테이블 이름 : " +
			// tableCellRow.getCell(4).getStringCellValue());
			// logger.info("#####################################################################");

			try {
				tableInfo.setSystemName(tableCellRow.getCell(2).getStringCellValue());
				tableInfo.setTableId(tableCellRow.getCell(4).getStringCellValue());
				tableInfo.setTableName(tableCellRow.getCell(7).getStringCellValue());
				tableInfo.setTableSort(tableCellRow.getCell(10).getStringCellValue());
				// 다음 행의 정보 입력
				tableInfo.setTableDescription(sheet.getRow(tableCell + 1).getCell(2).getStringCellValue());
				tableInfo.setRowLength((int) sheet.getRow(tableCell + 1).getCell(7).getNumericCellValue());
				tableInfo.setAuthor(sheet.getRow(tableCell + 1).getCell(10).getStringCellValue());

				// Column데이터는 테이블양식서 기준 4행 다음부터 있음
				int columnNum = tableCell + 4;

				mColumnInfo = new LinkedHashMap<String, ColumnInfo>();

				while (sheet.getRow(columnNum) != null && sheet.getRow(columnNum).getCell(0) != null
						&& sheet.getRow(columnNum).getCell(0).getNumericCellValue() != 0) {
					mColumnInfo = getColumnInfo(sheet, columnNum, mColumnInfo);
					columnNum++;
				}

				tableInfo.setmColumnInfo(mColumnInfo);
				mTableInfo.put(tableInfo.getTableId() + "_" + tableInfo.getTableDescription(), tableInfo);

			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, ColumnInfo> getColumnInfo(XSSFSheet sheet, int columnNum, Map<String, ColumnInfo> mColumnInfo) {
		columnInfo = new ColumnInfo();
		XSSFRow columnCellRow = sheet.getRow(columnNum);

		try {
			columnInfo.setSeq("" + (int) columnCellRow.getCell(0).getNumericCellValue());
			columnInfo.setColumnId(columnCellRow.getCell(1).getStringCellValue());
			columnInfo.setColumnName(columnCellRow.getCell(3).getStringCellValue());
			columnInfo.setDataType(columnCellRow.getCell(5).getStringCellValue());
			columnInfo.setLength((int) columnCellRow.getCell(6).getNumericCellValue());
			columnInfo.setPK(columnCellRow.getCell(7).getStringCellValue());
			columnInfo.setNullChk(columnCellRow.getCell(8).getStringCellValue());

			// Default 값과 설명 Column은 String과 Numeric 두가지 경우 모두 가능하므로 타입에 따라 구분
			switch (columnCellRow.getCell(9).getCellTypeEnum()) {
			case STRING:
				columnInfo.setDefaultData(columnCellRow.getCell(9).getStringCellValue());
				break;
			case NUMERIC:
				columnInfo.setDefaultData("" + (int) columnCellRow.getCell(9).getNumericCellValue());
				break;
			}

			switch (columnCellRow.getCell(10).getCellTypeEnum()) {
			case STRING:
				columnInfo.setColumnDescrition(columnCellRow.getCell(10).getStringCellValue());
				break;
			case NUMERIC:
				columnInfo.setColumnDescrition("" + (int) columnCellRow.getCell(10).getNumericCellValue());
				break;
			}

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		// logger.info(columnInfo.toString());

		// System.out.println(columnInfo.getColumnName());

		mColumnInfo.put(columnInfo.getSeq(), columnInfo);

		return mColumnInfo;

	}

	public void makeSqlFile(Map<String, TableInfo> mTableInfo) {

		// 파일 출력에 필요한 변수 선언
		FileWriter fw = null;
		PrintWriter pw = null;
		File file = null;
		String createSqlStatement = null;

		for (Map.Entry<String, TableInfo> entry : mTableInfo.entrySet()) {

			tableInfo = entry.getValue();
			List<String> PKList = new ArrayList<String>();

			// 기존 테이블 재생성을 위한 삭제
			createSqlStatement = "DROP TABLE " + tableInfo.getTableId() + "\n/\n\nCREATE TABLE "
					+ tableInfo.getTableId() + "(\n";

			// Column 이름/타입/크기 등 입력
			for (Map.Entry<String, ColumnInfo> entry1 : tableInfo.getmColumnInfo().entrySet()) {
				ColumnInfo columnInfo = entry1.getValue();
				createSqlStatement += "\t" + columnInfo.getColumnId();

				// DATE인 경우와 크기가 지정되지 않은 NUMBER인 경우 크기 생략 처리
				if (columnInfo.getDataType() != null && columnInfo.getDataType().equalsIgnoreCase("DATE")) {
					createSqlStatement += "\t" + columnInfo.getDataType();
				} else if (columnInfo.getDataType() != null && columnInfo.getDataType().equalsIgnoreCase("NUMBER")
						&& columnInfo.getLength() == 0) {
					createSqlStatement += "\t" + columnInfo.getDataType();
				} else {
					createSqlStatement += "\t" + columnInfo.getDataType() + "(" + columnInfo.getLength() + ")";
				}

				// DEFAULT 값이 존재할 시 값 입력
				if (columnInfo.getDefaultData() != null) {
					createSqlStatement += "\tDEFAULT " + columnInfo.getDefaultData();
				}

				// NullChk가 N인 경우 NOT NULL 옵션 입력
				if (columnInfo.getNullChk() != null && columnInfo.getNullChk().equalsIgnoreCase("N")) {
					createSqlStatement += "\tNOT NULL";
				}

				createSqlStatement += ",\n";

				// PK가 Y인 경우 PKList에 입력했다가 한번에 제약조건 처리
				if (columnInfo.getPK() != null && columnInfo.getPK().equalsIgnoreCase("Y")) {
					PKList.add(columnInfo.getColumnId());
				}
			}

			// PKList에 값이 없거나 있는 경우 PK_tableID로 제약조건 추가
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

			createSqlStatement += ")\n/\n";

			// 각 Column에 COMMENT 입력
			for (Map.Entry<String, ColumnInfo> entry1 : tableInfo.getmColumnInfo().entrySet()) {
				String columnComment = "COMMENT ON COLUMN " + tableInfo.getTableId() + "."
						+ entry1.getValue().getColumnId() + " IS '" + entry1.getValue().getColumnName() + "';\n";
				createSqlStatement += columnComment;
			}

			createSqlStatement += "COMMIT;";

			logger.info("---------------------------------------------------------------------");
			logger.info("	파일 생성 : " + tableInfo.getTableId() + ".sql");
			logger.info("---------------------------------------------------------------------");

			System.out.println(createSqlStatement);

			try {

				String sqlFile = sqlFilePath + "/";

				// 미정 값이 있는 경우 (Chk)표시 추가
				if (createSqlStatement.indexOf("\t(0),") != -1) {
					sqlFile = sqlFile + "(Chk)";
				}

				file = new File(sqlFile + tableInfo.getTableId() + ".sql");

				// 이미 file이 있는 경우 구분 위해 TableName을 파일명에 추가
				if (file.exists()) {
					file = new File(sqlFile + tableInfo.getTableId() + "_" + tableInfo.getTableDescription() + ".sql");
				}

				fw = new FileWriter(file, false);
				pw = new PrintWriter(fw, true);

				pw.println(createSqlStatement);

			} catch (IOException e) {
				e.printStackTrace();
			}

			logger.info("	생성 완료");

		}
	}

}
