package com.mkp.tutorial.excel.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import com.mkp.tutorial.excel.domain.ReturnInventoryBoxMapping;
import com.mkp.tutorial.excel.exception.ExcelValidationException;
import com.mkp.tutorial.excel.util.ExcelUtil;
import com.mkp.tutorial.excel.util.ExcelUtilConstatns;


/**
 * 
 * @author munna
 *
 */

public class ExcelInputSQLOutput {
	static boolean fileCreated = false;

	public static void main(String[] args) throws Exception {
		String excelPath = "src/main/resources/ReturnInventoryBoxMapping.xlsx";

		Map<String, List<Object>> map = ExcelUtil.getInstance().convertExcelFileToMap(excelPath);
		List<Object> returnInventoryBoxMapping = ExcelUtil.getInstance().createRequiredObject(ExcelUtil.getInstance()
				.validateHeadersAndInstantiatedRequiredObject(ReturnInventoryBoxMapping.class, map), map);
		
		for (Object object : returnInventoryBoxMapping) {
			System.out.println(object);
		}

		// validate mandatory fields of ReturnInventoryBoxMapping object
		validateMandatoryFields(returnInventoryBoxMapping);

		// create insert query for table tbl_return_inventory_box_mapping and
		// write to a file
		createQuery(returnInventoryBoxMapping);
	}

	private static void createQuery(List<Object> objectList) throws IOException {
		String query = ExcelUtilConstatns.EMPTY_STRING;
		File file = new File("src/main/resources/query.sql");
		if (file.exists()) {
			fileCreated = true;
		} else {
			fileCreated = file.createNewFile();
		}

		if (fileCreated) {
			try (FileOutputStream fos = new FileOutputStream(file);
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos))) {
				for (Object object : objectList) {
					ReturnInventoryBoxMapping obj1 = (ReturnInventoryBoxMapping) object;
					query = "INSERT INTO `tbl_return_inventory_box_mapping` (`BoxId`, `StoreNumericId`, `AccountId`, `ManufacturerID`,`ManufacturerDivisionId`,`ExpiryRange`, `CreatedBy`, `DateCreated`)"
							+ " VALUES (" + obj1.getBoxId() + "," + obj1.getStoreNumericId() + ","
							+ obj1.getManufacturerID() + "," + obj1.getManufacturerDivisionId() + ","
							+ obj1.getExpiryRange() + ", 'Administrator', now());";

					bufferedWriter.write(query);
					bufferedWriter.newLine();
				}

			} catch (IOException e) {
				throw new ExcelValidationException(e);
			}

		}

	}

	private static void validateMandatoryFields(List<Object> returnInventoryBoxMapping) {
		returnInventoryBoxMapping.stream().forEach(object -> {
			ReturnInventoryBoxMapping obj = (ReturnInventoryBoxMapping) object;
			if (obj.getBoxId() == null) {
				throw new ExcelValidationException("BoxId should not empty");
			}
			if (obj.getStoreNumericId() == null) {
				throw new ExcelValidationException("StoreNumericId should not empty");
			}
			if (obj.getAccountId() == null) {
				throw new ExcelValidationException("AccountId should not empty");
			}
			if (obj.getManufacturerID() == null) {
				throw new ExcelValidationException("ManufacturerID should not empty");
			}
			if (obj.getExpiryRange() == null) {
				throw new ExcelValidationException("ExpiryRange should not empty");
			}
		});
	}

}