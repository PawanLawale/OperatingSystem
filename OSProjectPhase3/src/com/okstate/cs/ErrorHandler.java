package com.okstate.cs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
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
 *			c. terminationErrorMessage:
 *					Contains the error message which cased the termination.
 *			d. warningMessages:
 *					Contains all the warning messages, which came up during program execution.
 *			
 */

public class ErrorHandler extends Exception {

	private static final long serialVersionUID = 1L;
	private int errorCode = 0;
	public static String terminationErrorMessage = "NONE";
	public static TreeMap<String,StringBuffer> warningMessages = new TreeMap<String,StringBuffer>();
	public static TreeMap<String,String> jobErrors = new TreeMap<String,String>(); 
	
	/*
	 * Variables for Error messages
	 */
	
	private final static String error100 = "ERROR(100): Illegal Memory Access.";
	private final static String error101 = "ERROR(101): Invalid Instruction.";
	private final static String error102 = "ERROR(102): Invalid Input Value.";
	private final static String error103 = "ERROR(103): Invalid Loader Format.";
	private final static String error104 = "ERROR(104): Illegal Stack Access.";
	private final static String error105 = "ERROR(105): Input Value Out of Range.";
	private final static String error106 = "ERROR(106): IO Error.";
	private final static String error107 = "ERROR(107): Insufficient momory to load program.";
	private final static String error108 = "ERROR(108): Batch File Not Found.";
	private final static String error109 = "ERROR(109): Input/Output sizes not in Hex.";
	private final static String error110 = "ERROR(110): Suspected Infinite Loop. Program terminated.";
	private final static String error112 = "ERROR(112): Attempt to divide by zero.";
	private final static String error113 = "ERROR(113): Invalid Op Code.";
	private final static String error114 = "ERROR(114): Invalid Hex value for registers in file.";
	private final static String error115 = "ERROR(115): Mismatch in given input size v/s actual input size.";
	private final static String error116 = "ERROR(116): Invalid Address lookup on DISK.";
	private final static String error117 = "ERROR(117): Invalid Segment creation request.";
	private final static String error118 = "ERROR(118): Page does not exist on DISK.";
	private final static String error119 = "ERROR(119): Error while replacing the page.";
	private final static String error120 = "ERROR(120): Invalid loader format. Missing **JOB.";
	private final static String error121 = "ERROR(121): Invalid loader format. More than one **INPUT.";
	private final static String error122 = "ERROR(122): Invalid loader format. Missing **INPUT.";
	private final static String error123 = "ERROR(123): Invalid loader format. Missing **FIN.";
	private final static String error124 = "ERROR(124): Invalid **JOB format.";
	private final static String error125 = "ERROR(125): Job parameters not in Hex.";
	private final static String error126 = "ERROR(126): Invalid loader format.Missing INPUT segment.";
	private final static String errordefault = "ERROR(111): Some Illegal Activity.";
	
	
	public ErrorHandler(){
		super();
	}
	public ErrorHandler(int code){
		this.errorCode = code;
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
	public void throwExceptionMessage(int code){
		OperatingSystem.systemClock+=15;
		switch(code){
			case 106:
				terminationErrorMessage = error106;
				break;
			case 108:
				terminationErrorMessage = error108;
				break;
			default:
				terminationErrorMessage = errordefault;
		}
	}
	
	/*
	 * This method is returns the error message corresponding to the given error code.
	 */
	public static String recordError(int code){
		String errorMsg = null;
		OperatingSystem.systemClock+=15;
		switch(code){
			case 100:
				errorMsg = error100;
				break;
			case 101:
				errorMsg = error101;
				break;
			case 102:
				errorMsg = error102;
				break;
			case 103:
				errorMsg = error103;
				break;
			case 104:
				errorMsg = error104;
				break;
			case 105:
				errorMsg = error105;
				break;
			case 107:
				errorMsg = error107;
				break;
			case 109:
				errorMsg = error109;
				break;
			case 110:
				errorMsg = error110;
				break;
			case 112:
				errorMsg = error112;
				break;
			case 113:
				errorMsg = error113;
				break;
			case 114:
				errorMsg = error114;
				break;
			case 115:
				errorMsg = error115;
				break;
			case 116:
				errorMsg = error116;
				break;
			case 117:
				errorMsg = error117;
				break;
			case 118:
				errorMsg = error118;
				break;
			case 119:
				errorMsg = error119;
				break;
			case 120:
				errorMsg = error120;
				break;
			case 121:
				errorMsg = error121;
				break;
			case 122:
				errorMsg = error122;
				break;
			case 123:
				errorMsg = error123;
				break;
			case 124:
				errorMsg = error124;
				break;
			case 125:
				errorMsg = error125;
				break;
			case 126:
				errorMsg = error126;
				break;
		}
		return errorMsg;
	}
	
	/*
	 * This method is used to write the errors occurred during the spooling process.
	 */
	public static void writeErrors(String jobId, ArrayList<String> errors){
		File outputFile = new File("execution_profile_"+InputSpooler.batchName+".txt");
		try{
			BufferedWriter fileWriter = null;
			if(outputFile.exists()){
				fileWriter = new BufferedWriter(new FileWriter(outputFile,true));
			}else{
				fileWriter = new BufferedWriter(new FileWriter(outputFile));
				outputFile.createNewFile();
			}
			fileWriter.write("JobId: "+ jobId + "\n");
			for(String str:errors){
				fileWriter.write(str+"\n");
			}
			fileWriter.write("\n");
			fileWriter.flush();
			fileWriter.close();
		}catch(Exception e){
			ErrorHandler.recordError(106);
		}
		
	}
}
