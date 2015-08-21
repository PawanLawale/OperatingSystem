package com.okstate.cs;

import java.util.ArrayList;

/**
 * @author : Pawan Lawale
 * @Date : 25-Feb-2014
 * 
 * @Routine : ErrorHandler
 *	1. Description:
 *			This is a Error Handling routine. Any error thrown by the OS is sent to this
 *			routine to catch. It list down all the error messages identified and given a
 *			error number to it. In the OS whenever an error is occurred, it is sends the
 *			error number to this routine. This routine displays the error message
 *			corresponding to that code. 
 *
 *	2. Global Variables:
 *			a. errorCode :
 *					This is the specific error code thrown by OS.
 *			b. terminationStatues:
 *					Indicates the status of termination. Normal/Abnormal
 *			c. terminationErrorMessage:
 *					Contains the error message which cased the termination.
 *			d. warningMessages:
 *					Contains all the warning messages, which came up during program execution.
 *			
 */

public class ErrorHandler extends Exception {

	private static final long serialVersionUID = 1L;
	private int errorCode = 0;
	public static String terminationStatues = "Normal";
	public static String terminationErrorMessage = "NONE";
	public static ArrayList<String> warningMessages = new ArrayList<String>();
	
	/*
	 * Variables for Error messages
	 */
	
	private final String error100 = "ERROR(100): Illegal Memory Access.";
	private final String error101 = "ERROR(101): Invalid Instruction.";
	private final String error102 = "ERROR(102): Invalid Input Value.";
	private final String error103 = "ERROR(103): Invalid Loader Format.";
	private final String error104 = "ERROR(104): Illegal Stack Access.";
	private final String error105 = "ERROR(105): Input Value Out of Range.";
	private final String error106 = "ERROR(106): IO Exception.";
	private final String error107 = "ERROR(107): Insufficient momory to load program.";
	private final String error108 = "ERROR(108): File Not Found.";
	private final String error109 = "ERROR(109): Invalid HEX value in file.";
	private final String error110 = "ERROR(110): Suspected Infinite Loop. Program terminated.";
	private final String error112 = "ERROR(112): Attempt to divide by zero.";
	private final String error113 = "ERROR(113): Invalid Op Code.";
	private final String error114 = "ERROR(114): Invalid Hex value for registers in file.";
	private final String errordefault = "ERROR(111): Some Illegal Activity.";
	
	
	public ErrorHandler(){
		super();
	}
	public ErrorHandler(int code){
		this.errorCode = code;
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
	public void throwErrorMessage(int code){
		terminationStatues = "Abnormal";
		switch(code){
			case 100:
				System.out.println(error100);
				terminationErrorMessage = error100;
				break;
			case 101:
				System.out.println(error101);
				terminationErrorMessage = error101;
				break;
			case 102:
				System.out.println(error102);
				terminationErrorMessage = error102;
				break;
			case 103:
				System.out.println(error103);
				terminationErrorMessage = error103;
				break;
			case 104:
				System.out.println(error104);
				terminationErrorMessage = error104;
				break;
			case 105:
				System.out.println(error105);
				terminationErrorMessage = error105;
				break;
			case 106:
				System.out.println(error106);
				terminationErrorMessage = error106;
				break;
			case 107:
				System.out.println(error107);
				terminationErrorMessage = error107;
				break;
			case 108:
				System.out.println(error108);
				terminationErrorMessage = error108;
				break;
			case 109:
				System.out.println(error109);
				terminationErrorMessage = error109;
				break;
			case 110:
				System.out.println(error110);
				terminationErrorMessage = error110;
				break;
			case 112:
				System.out.println(error112);
				terminationErrorMessage = error112;
				break;
			case 113:
				System.out.println(error113);
				terminationErrorMessage = error113;
				break;
			case 114:
				System.out.println(error114);
				terminationErrorMessage = error114;
				break;
			default:
				System.out.println(errordefault);
				terminationErrorMessage = errordefault;
		}
	}
}
