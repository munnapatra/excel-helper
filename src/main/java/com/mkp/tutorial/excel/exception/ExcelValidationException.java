package com.mkp.tutorial.excel.exception;

/**
 * 
 * @author munna
 *
 */
public class ExcelValidationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExcelValidationException() {
		super();
	}

	public ExcelValidationException(String message) {
		super(message);
	}
	
	public ExcelValidationException(Throwable e) {
		super(e);
	}
	
	public ExcelValidationException(String message,Throwable e) {
		super(message, e);
	}

}
