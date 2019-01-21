package com.mkp.tutorial.excel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mkp.tutorial.excel.exception.ExcelValidationException;


/**
 * 
 * @author munna
 *
 */
public final class ExcelUtil {

	private static ExcelUtil excelUtil = null;

	private List<String> headerNames;
	private List<String> fieldNames;

	public List<String> getFieldNames() {
		return this.fieldNames;
	}

	public List<String> getHeaderNames() {
		return this.headerNames;
	}

	public static ExcelUtil getInstance() {
		if (excelUtil == null) {
			excelUtil = new ExcelUtil();
		}
		return excelUtil;
	}

	/**
	 * This method will convert a excel file into map where excel's headers and its index will be set as key and its
	 * corresponding column cell will be as value.
	 * 
	 * @param excelPath
	 * @return
	 * @throws IOException
	 */
	public Map<String, List<Object>> convertExcelFileToMap(String excelPath) throws IOException {
		FileInputStream file = new FileInputStream(new File(excelPath));
		XSSFWorkbook workbook = new XSSFWorkbook(file);
		XSSFSheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		Map<String, List<Object>> map = new LinkedHashMap<>();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0) {
				setHeadersToMapAsKey(map, row);
			} else {
				setCellToMapAsValue(map, row);
			}
		}
		file.close();
		return map;
	}

	private void setHeadersToMapAsKey(Map<String, List<Object>> map, Row row) {
		for (int i = 0; i < row.getLastCellNum(); i++) {
			if (row.getCell(i) != null) {
				map.put(i + row.getCell(i).toString(), new ArrayList<>());
			} else {
				map.put(i + ExcelUtilConstatns.EMPTY_STRING, new ArrayList<>());
			}
		}
	}

	private void setCellToMapAsValue(Map<String, List<Object>> map, Row row) {
		this.headerNames = map.keySet().stream().collect(Collectors.toList());
		for (int i = 0; i < row.getLastCellNum(); i++) {
			for (String header : headerNames) {
				if (header.substring(0, 1).equals((new Integer(i)).toString())) {
					map.get(header).add(row.getCell(i));
				}
			}
		}
	}

	/**
	 * This method will validate all headers with provided class filed as parameter (Class<?>)
	 * 
	 * @param clazz
	 * @param map
	 * @return
	 */
	public List<Object> validateHeadersAndInstantiatedRequiredObject(Class<?> clazz, Map<String, List<Object>> map) {
		setFiledNames(clazz);
		if (headerNames.size() < fieldNames.size()) {
			for (int i = (headerNames.size() + 1); i <= fieldNames.size(); i++) {
				headerNames.add(ExcelUtilConstatns.WHITE_SPACE);
			}
		}
		int counter = 0;
		for (String field : fieldNames) {
			String header = headerNames.get(counter++).substring(1);
			if (!header.isEmpty() && field.equalsIgnoreCase(header)) {
				continue;
			}
			if (!header.isEmpty() && !field.equalsIgnoreCase(header)) {
				throw new ExcelValidationException(
						headerNames.get(--counter).substring(1) + " Header is not valid, It should be: " + field);
			} else {
				throw new ExcelValidationException("Header " + field + " is missing.");
			}
		}

		return instantiatedRequiredObject(clazz, map);
	}

	private void setFiledNames(Class<?> clazz) {
		this.fieldNames = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			this.fieldNames.add(field.getName());
		}
	}

	private List<Object> instantiatedRequiredObject(Class<?> clazz, Map<String, List<Object>> map) {
		List<Object> list = new ArrayList<>();
		int columnLengthWithoutHeader = 0;
		for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
			String key = entry.getKey();
			columnLengthWithoutHeader = map.get(key).size();
			if (columnLengthWithoutHeader != 0 && list.isEmpty()) {
				for (int i = 0; i < columnLengthWithoutHeader; i++) {
					try {
						list.add(clazz.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						throw new ExcelValidationException(e.getMessage());
					}
				}
				break;
			}
		}
		return list;
	}

	/**
	 * This method will set map values into required object and return as list of object.
	 * @param objectList
	 * @param map
	 * @return
	 */
	public List<Object> createRequiredObject(List<Object> objectList, Map<String, List<Object>> map) {
		List<String> headers = map.keySet().stream().collect(Collectors.toList());
		int valueCounter = 0;
		for (int i = 0; i < objectList.size(); i++) {
			int keyCounter = 0;
			for (String field : fieldNames) {
				if (field.equalsIgnoreCase(headers.get(keyCounter).substring(1))) {
					Class<?> filedType = getFieldType(objectList, i, field);
					try {
						String value = getValue(map, valueCounter, headers, keyCounter);
						if (null != value) {
							keyCounter = setShortValue(objectList, i, keyCounter, field, filedType, value);
							keyCounter = setLongValue(objectList, i, keyCounter, field, filedType, value);
							keyCounter = setIntegerValue(objectList, i, keyCounter, field, filedType, value);
							keyCounter = setStringValue(objectList, i, keyCounter, field, filedType, value);
							keyCounter = setDoubleValue(objectList, i, keyCounter, field, filedType, value);
							keyCounter = setFloatValue(objectList, i, keyCounter, field, filedType, value);
						} else {
							keyCounter++;
						}

					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						throw new ExcelValidationException("Exception for Field:" + field, e);
					}
				}
			}
			valueCounter++;
		}
		return objectList;
	}

	private int setFloatValue(List<Object> objectList, int i, int keyCounter, String field, Class<?> filedType,
			String value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (filedType.equals(Float.class)) {
			setFloatTypeValue(objectList, i, field, filedType, value);
			keyCounter++;
		}
		return keyCounter;
	}

	private int setDoubleValue(List<Object> objectList, int i, int keyCounter, String field, Class<?> filedType,
			String value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (filedType.equals(Double.class)) {
			setDoubleTypeValue(objectList, i, field, filedType, value);
			keyCounter++;
		}
		return keyCounter;
	}

	private int setStringValue(List<Object> objectList, int i, int keyCounter, String field, Class<?> filedType,
			String value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (filedType.equals(String.class)) {
			setStringTypeValue(objectList, i, field, filedType, value);
			keyCounter++;
		}
		return keyCounter;
	}

	private int setIntegerValue(List<Object> objectList, int i, int keyCounter, String field, Class<?> filedType,
			String value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (filedType.equals(Integer.class) && value.contains(ExcelUtilConstatns.DOT)) {
			setIntegerTypeValue(objectList, i, field, filedType, getValueBeforeDecimal(value));
			keyCounter++;
		}
		if (filedType.equals(Integer.class) && !value.contains(ExcelUtilConstatns.DOT)) {
			setIntegerTypeValue(objectList, i, field, filedType, value);
			keyCounter++;
		}
		return keyCounter;
	}

	private int setLongValue(List<Object> objectList, int i, int keyCounter, String field, Class<?> filedType,
			String value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (filedType.equals(Long.class) && value.contains(ExcelUtilConstatns.DOT)) {
			setLongTypeValue(objectList, i, field, filedType, getValueBeforeDecimal(value));
			keyCounter++;
		}
		if (filedType.equals(Long.class) && !value.contains(ExcelUtilConstatns.DOT)) {
			setLongTypeValue(objectList, i, field, filedType, value);
			keyCounter++;
		}
		return keyCounter;
	}

	private int setShortValue(List<Object> objectList, int i, int keyCounter, String field, Class<?> filedType,
			String value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (filedType.equals(Short.class) && value.contains(ExcelUtilConstatns.DOT)) {
			setShortTypeValue(objectList, i, field, filedType, getValueBeforeDecimal(value));
			keyCounter++;
		}

		if (filedType.equals(Short.class) && !value.contains(ExcelUtilConstatns.DOT)) {
			setShortTypeValue(objectList, i, field, filedType, value);
			keyCounter++;
		}
		return keyCounter;
	}

	private void setShortTypeValue(List<Object> objectList, int i, String field, Class<?> filedType, String value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		objectList.get(i).getClass().getMethod(getSetterMethod(field), filedType).invoke(objectList.get(i),
				Short.valueOf(value));
	}

	private void setFloatTypeValue(List<Object> objectList, int i, String field, Class<?> filedType, String value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		objectList.get(i).getClass().getMethod(getSetterMethod(field), filedType).invoke(objectList.get(i),
				Float.valueOf(value));

	}

	private void setDoubleTypeValue(List<Object> objectList, int i, String field, Class<?> filedType, String value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		objectList.get(i).getClass().getMethod(getSetterMethod(field), filedType).invoke(objectList.get(i),
				Double.valueOf(value));

	}

	private void setStringTypeValue(List<Object> objectList, int i, String field, Class<?> filedType, String value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		objectList.get(i).getClass().getMethod(getSetterMethod(field), filedType).invoke(objectList.get(i), value);
	}

	private void setIntegerTypeValue(List<Object> objectList, int i, String field, Class<?> filedType, String value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		objectList.get(i).getClass().getMethod(getSetterMethod(field), filedType).invoke(objectList.get(i),
				Integer.valueOf(value));
	}

	private Class<?> getFieldType(List<Object> objectList, int i, String field) {
		Class<?> fieldType = null;
		try {
			fieldType = objectList.get(i).getClass().getDeclaredField(field).getType();
		} catch (NoSuchFieldException | SecurityException e) {
			throw new ExcelValidationException(e.getMessage());
		}
		return fieldType;
	}

	private void setLongTypeValue(List<Object> objectList, int i, String field, Class<?> filedType, String value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		objectList.get(i).getClass().getMethod(getSetterMethod(field), filedType).invoke(objectList.get(i),
				Long.valueOf(value));
	}

	private String getValue(Map<String, List<Object>> map, int valueCounter, List<String> headers, int keyCounter) {
		if (map.get(headers.get(keyCounter)).get(valueCounter) != null) {
			return map.get(headers.get(keyCounter)).get(valueCounter).toString();
		}
		return null;
	}

	private String getValueBeforeDecimal(String value) {
		return value.substring(0, value.indexOf(ExcelUtilConstatns.DOT));
	}

	private String getSetterMethod(String field) {
		return ExcelUtilConstatns.SET + field.toUpperCase().charAt(0) + field.substring(1);
	}
}