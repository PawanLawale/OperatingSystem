package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
 * 
 * @Routine : OutputSpooler
 *	1. Description:
 *			This routine is used to spool the output file.
 *			After the execution of job it writes the output
 *			into this file along with the error/warning messages.
 *			
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OutputSpooler {

	public static ArrayList<String> outputSegment = new ArrayList<String>();
	public static ArrayList<String> inputSegment = new ArrayList<String>();
	public static int noOfWordsForMemory = 0;
	public static int noOfWordsForDisk = 0;
	public static int memoryNoOfFrames = 0;
	public static int diskNoOfFrames = 0;
	public static int noOfUnusedWords = 0;
	public static int noOfOutputWords = 0;
	
	/*
	 * This method generates the output file containing below info:
	 * a. Jobid
	 * b. Warnings
	 * c. Error
	 * d. Nature of termination
	 * e. CLOCK
	 * f. Memory Utilization.
	 * g. Disk Utilization.
	 * h. Memory Fragmentation.
	 * i. Disk Fragmentation.
	 */
	public static void generateOutputFile(){
		File outPutFile = new File("OutputFile.txt");
		try{
			if(!outPutFile.exists()){
				outPutFile.createNewFile();
			}
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outPutFile));
			if(OperatingSystem.jobIds.size()>0){
				for(String key:OperatingSystem.jobIds){
					fileWriter.write("Job Id : "+key+"\n");
					
					if(ErrorHandler.warningMessages.size()>0){
						//fileWriter.write("Warnings : \n");
						for(String msg:ErrorHandler.warningMessages){
							fileWriter.write(""+msg+"\n");
						}
					}
					
					//fileWriter.write("Input : \n");
					for(String in : inputSegment){
						fileWriter.write("\t\t"+in + " (DEC)\n");
					}
					//fileWriter.write("Output : \n");
					for(String out : outputSegment){
						fileWriter.write("\t\t"+out + " (DEC)\n");
					}
					
					fileWriter.write("Termination : "+ ErrorHandler.terminationStatues + "\n");
					if(ErrorHandler.terminationStatues.equals("Abnormal")){
						fileWriter.write(ErrorHandler.terminationErrorMessage + "\n");
					}
					
					fileWriter.write("CLOCK (vts) : " + Utility.decimalToHex(OperatingSystem.systemClock, 4).toUpperCase() + " (HEX)\n");
					fileWriter.write("Run Time:\n");
					fileWriter.write("\t\tExecution Time: " + OperatingSystem.systemClock + " (DEC)\n");
					fileWriter.write("\t\tI/O Time: " +OperatingSystem.totalIoTime + " (DEC)\n");
					fileWriter.write("\t\tPage Fault Time: " +OperatingSystem.totalPageFaultTime + " (DEC)\n");
					fileWriter.write("\t\tSegment Fault Time: " +OperatingSystem.totalSegmentFaultTime + " (DEC)\n");
					fileWriter.write("\t\tError Time: " +OperatingSystem.errorHandlingTime + " (DEC)\n");
					
					fileWriter.write("Memory Utilization:\n");
					fileWriter.write("\t\t"+(memoryNoOfFrames * 8)+" of 256 words\n");
					fileWriter.write("\t\t"+memoryNoOfFrames+" of 32 frames\n");
					
					fileWriter.write("Disk Utilization:\n");
					fileWriter.write("\t\t"+(diskNoOfFrames * 8)+" of 2048 words\n");
					fileWriter.write("\t\t"+diskNoOfFrames+" of 256 frames\n");
					
					fileWriter.write("Memory Fragmentation:"+(noOfUnusedWords-noOfOutputWords)+"\n");
					fileWriter.write("Disk Fragmentation:"+(noOfUnusedWords-noOfOutputWords)+"\n");
					
				}
			}else{
				fileWriter.write("Job Id : ---\n");
				//fileWriter.write("Warnings : ---\n");
				fileWriter.write("Termination : "+ErrorHandler.terminationStatues + "\n");
				if(ErrorHandler.terminationStatues.equals("Abnormal")){
					fileWriter.write(ErrorHandler.terminationErrorMessage + "\n");
				}
				//fileWriter.write("Output : ---\n");
				fileWriter.write("CLOCK (vts): "+ Utility.decimalToHex(OperatingSystem.systemClock, 4).toUpperCase() + " (HEX)\n");
				fileWriter.write("Run Time:\n");
				fileWriter.write("\t\tExecution Time: " + OperatingSystem.systemClock + " (DEC)\n");
				fileWriter.write("\t\tInput/Output Time: " +OperatingSystem.totalIoTime + " (DEC)\n");
				fileWriter.write("\t\tPage Fault Time: " +OperatingSystem.totalPageFaultTime + " (DEC)\n");
				fileWriter.write("\t\tSegment Fault Time: " +OperatingSystem.totalSegmentFaultTime + " (DEC)\n");
				fileWriter.write("\t\tError Time: " +OperatingSystem.errorHandlingTime + " (DEC)\n");
			}
			fileWriter.flush();
			fileWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
}
