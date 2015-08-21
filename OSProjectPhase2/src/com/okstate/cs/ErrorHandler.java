package com.okstate.cs;

import java.util.ArrayList;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
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
	private final String error106 = "ERROR(106): IO Error.";
	private final String error107 = "ERROR(107): Insufficient momory to load program.";
	private final String error108 = "ERROR(108): File Not Found.";
	private final String error109 = "ERROR(109): Invalid HEX value in file.";
	private final String error110 = "ERROR(110): Suspected Infinite Loop. Program terminated.";
	private final String error112 = "ERROR(112): Attempt to divide by zero.";
	private final String error113 = "ERROR(113): Invalid Op Code.";
	private final String error114 = "ERROR(114): Invalid Hex value for registers in file.";
	private final String error115 = "ERROR(115): Mismatch in given input size v/s actual input size.";
	private final String error116 = "ERROR(116): Invalid Address lookup on DISK.";
	private final String error117 = "ERROR(117): Invalid Segment creation request.";
	private final String error118 = "ERROR(118): Page does not exist on DISK.";
	private final String error119 = "ERROR(119): Error while replacing the page.";
	private final String error120 = "ERROR(120): Invalid loader format. Missing **JOB.";
	private final String error121 = "ERROR(121): Invalid loader format. More than on **INPUT.";
	private final String error122 = "ERROR(122): Invalid loader format. Missing **INPUT.";
	private final String error123 = "ERROR(123): Invalid loader format. Missing **FIN.";
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
		OperatingSystem.systemClock+=15;
		OperatingSystem.errorHandlingTime += 15;
		
		switch(code){
			case 100:
				terminationErrorMessage = error100;
				break;
			case 101:
				terminationErrorMessage = error101;
				break;
			case 102:
				terminationErrorMessage = error102;
				break;
			case 103:
				terminationErrorMessage = error103;
				break;
			case 104:
				terminationErrorMessage = error104;
				break;
			case 105:
				terminationErrorMessage = error105;
				break;
			case 106:
				terminationErrorMessage = error106;
				break;
			case 107:
				terminationErrorMessage = error107;
				break;
			case 108:
				terminationErrorMessage = error108;
				break;
			case 109:
				terminationErrorMessage = error109;
				break;
			case 110:
				terminationErrorMessage = error110;
				break;
			case 112:
				terminationErrorMessage = error112;
				break;
			case 113:
				terminationErrorMessage = error113;
				break;
			case 114:
				terminationErrorMessage = error114;
				break;
			case 115:
				terminationErrorMessage = error115;
				break;
			case 116:
				terminationErrorMessage = error116;
				break;
			case 117:
				terminationErrorMessage = error117;
				break;
			case 118:
				terminationErrorMessage = error118;
				break;
			case 119:
				terminationErrorMessage = error119;
				break;
			case 120:
				terminationErrorMessage = error120;
				break;
			case 121:
				terminationErrorMessage = error121;
				break;
			case 122:
				terminationErrorMessage = error122;
				break;
			case 123:
				terminationErrorMessage = error123;
				break;
			default:
				terminationErrorMessage = errordefault;
		}
	}
}
