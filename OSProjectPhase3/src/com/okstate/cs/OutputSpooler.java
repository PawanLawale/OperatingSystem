 package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : OutputSpooler
 *	1. Description:
 *			This routine is used to spool the execution_profile.
 *			After the execution of job it writes the output
 *			into this file along with the error/warning messages.
 *			
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

public class OutputSpooler {

	public static TreeMap<String,StringBuffer> outputSegment = new TreeMap<String,StringBuffer>();
	public static TreeMap<String,StringBuffer> inputSegment = new TreeMap<String,StringBuffer>();

	/*
	 * This method generates the output file containing below info:
	 * a. Jobid
	 * b. Warnings
	 * c. Error
	 * d. Nature of termination
	 * e. CLOCK
	 * 
	 * This method is called on the termination of each job.
	 */
	public static void generateOutputFile(String jobId){
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		File outPutFile = new File("execution_profile_"+InputSpooler.batchName+".txt");
		BufferedWriter fileWriter = null;
		try{
			if(!outPutFile.exists()){
				outPutFile.createNewFile();
				fileWriter = new BufferedWriter(new FileWriter(outPutFile));
			}else{
				fileWriter = new BufferedWriter(new FileWriter(outPutFile,true));
			}
		
			fileWriter.write("---------------------------------------------------\n");
			
			fileWriter.write("["+OperatingSystem.systemClock+"]\n");
			fileWriter.write("Job Id : "+jobId+"\n");
			
			StringBuffer warning = ErrorHandler.warningMessages.get(jobId);
			if(warning!=null){
				fileWriter.write(""+warning+"\n");
			}
			
			String errors = ErrorHandler.jobErrors.get(jobId);
			if(errors!=null){
				fileWriter.write(""+errors+"\n");
			}
			
		
			fileWriter.write(jobId+" 01:(DEC)\n");
			if(inputSegment.get(jobId) != null){
				fileWriter.write(inputSegment.get(jobId) + "\n");
			}
			
			fileWriter.write(jobId+" 02:(DEC)\n");
			if(outputSegment.get(jobId)!=null){
				fileWriter.write(outputSegment.get(jobId) + "\n");
			}
			
			
			
			
			fileWriter.write("Termination : "+ (errors==null?"Normal":"Abnormal") + "\n");
			
			fileWriter.write("Arrival Time: "+ Utility.decimalToHex(pcb.arrivalTime, 4) + " (HEX)\n");
			fileWriter.write("Departure Time: "+ Utility.decimalToHex(pcb.completionTime, 4)  + " (HEX)\n");
			fileWriter.write("Turn-around Time: "+ (pcb.completionTime - pcb.arrivalTime)  + " (DEC)\n");
			
			fileWriter.write("Page Fault : "+(pcb.pageFaultTime/10)+"\n");
			fileWriter.write("Run Time(vts):\n");
			fileWriter.write("\t\tExecution Time: " + pcb.executionTime + " (DEC)\n");
			fileWriter.write("\t\tI/O Time: " +pcb.totalIoTime + " (DEC)\n");
			fileWriter.write("\t\tPage Fault Time: " +pcb.pageFaultTime + " (DEC)\n");
			fileWriter.write("\t\tSegment Fault Time: " +pcb.segmentFaultTime + " (DEC)\n");
			fileWriter.write("\t\tError Time: " +pcb.errorHandlingTime + " (DEC)\n");
			
					
			fileWriter.write("---------------------------------------------------\n");
			fileWriter.flush();
			fileWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * This method is called when all the jobs in the system are complete.
	 * It writes the System execution status.
	 */
	public static void writeSystemInfo(){
		File outPutFile = new File("execution_profile_"+InputSpooler.batchName+".txt");
		BufferedWriter fileWriter = null;
		try{
			if(!outPutFile.exists()){
				outPutFile.createNewFile();
				fileWriter = new BufferedWriter(new FileWriter(outPutFile));
			}else{
				fileWriter = new BufferedWriter(new FileWriter(outPutFile,true));
			}
		
			int totalJobs = SystemInfo.totalProcessedJobs.size();
			
			fileWriter.write("Clock(vts) : "+OperatingSystem.systemClock+" (DEC)\n");
			fileWriter.write("Jobs Processed : "+totalJobs+"\n");
			
			fileWriter.write("CPU Time (vts): (DEC)\n");
			fileWriter.write("\tMax :"+SystemInfo.maxCPUTime+"\n");
			fileWriter.write("\tMin :"+SystemInfo.minCPUTime+"\n");
			fileWriter.write("\tAvg :"+(SystemInfo.totalCPUTime/totalJobs)+"\n");
			
			fileWriter.write("Turn-around Time (vts): (DEC)\n");
			fileWriter.write("\tMax :"+SystemInfo.maxTAT+"\n");
			fileWriter.write("\tMin :"+SystemInfo.minTAT+"\n");
			fileWriter.write("\tAvg :"+(SystemInfo.totalTAT/totalJobs)+"\n");
			
			fileWriter.write("Code Segment Size : (DEC) \n");
			fileWriter.write("\tMax :"+SystemInfo.maxCodeSeg+"\n");
			fileWriter.write("\tMin :"+SystemInfo.minCodeSeg+"\n");
			fileWriter.write("\tAvg :"+(SystemInfo.totalCodeSeg/totalJobs)+"\n");
			
			fileWriter.write("Input Segment Size : (DEC)\n");
			fileWriter.write("\tMax :"+SystemInfo.maxInputSeg+"\n");
			fileWriter.write("\tMin :"+SystemInfo.minInputSeg+"\n");
			fileWriter.write("\tAvg :"+(SystemInfo.totalInputSeg/totalJobs)+"\n");
			
			fileWriter.write("Output Segment Size : (DEC)\n");
			fileWriter.write("\tMax :"+SystemInfo.maxOutputSeg+"\n");
			fileWriter.write("\tMin :"+SystemInfo.minOutputSeg+"\n");
			fileWriter.write("\tAvg :"+(SystemInfo.totalOutputSeg/totalJobs)+"\n");
			
			fileWriter.write("CPU Shots : (DEC)\n");
			fileWriter.write("\tMax :"+SystemInfo.maxCPUShots+"\n");
			fileWriter.write("\tMin :"+SystemInfo.minCPUShots+"\n");
			fileWriter.write("\tAvg :"+(SystemInfo.totalCPUShots/totalJobs)+"\n");
			
			fileWriter.write("IO Request : (DEC)\n");
			fileWriter.write("\tMax :"+SystemInfo.maxIO+"\n");
			fileWriter.write("\tMin :"+SystemInfo.minIO+"\n");
			fileWriter.write("\tAvg :"+(SystemInfo.totalIO/totalJobs)+"\n\n");
			
			fileWriter.write("No. of jobs normally terminated: "+SystemInfo.noOfNormalJobs+" (DEC)\n");
			fileWriter.write("No. of jobs abnormally terminated: "+SystemInfo.noOfAbnormalJobs+" (DEC)\n");
			
			fileWriter.write("Time lost due to Abnormal jobs: "+SystemInfo.timeOfAdnormalJobs+" (DEC)\n");
			fileWriter.write("Time lost due to Infinite loop jobs: "+SystemInfo.timeOfInfiniteLoops+" (DEC)\n");
			
			fileWriter.write("Infinite loop jobs: "+SystemInfo.infiniteLoopJobs+"\n");
			
			fileWriter.write("Mean TAT of normal jobs: "+(SystemInfo.tATOfNormalJobs/SystemInfo.noOfNormalJobs)+" (DEC)\n");
			fileWriter.write("Mean waiting time of normal jobs: "+(SystemInfo.waitingTimeOfNormalJobs/SystemInfo.noOfNormalJobs)+" (DEC)\n");
			
			fileWriter.write("Mean Page Faults: "+(SystemInfo.totalPF/SystemInfo.noOfNormalJobs)+" (DEC)\n");
			fileWriter.write("CPU idle time (vts): "+OperatingSystem.idleTime+" (DEC)\n");
			
			fileWriter.write("---------------------------------------------------\n");
			fileWriter.flush();
			fileWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * This method is called whenever events like IO, Loading, Pagefaule,etc
	 * occurred during the execution of job. It records, for which job this 
	 * event has occurred and at which time.
	 */
	public static void recordEvent(String event){
		File outPutFile = new File("execution_profile_"+InputSpooler.batchName+".txt");
		BufferedWriter fileWriter = null;
		try{
			if(!outPutFile.exists()){
				outPutFile.createNewFile();
				fileWriter = new BufferedWriter(new FileWriter(outPutFile));
			}else{
				fileWriter = new BufferedWriter(new FileWriter(outPutFile,true));
			}
			fileWriter.write(event+"\n");
			fileWriter.flush();
			fileWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * This method is used to record the snapshot of OS after
	 * specific interval of time.
	 */
	public static void recordSnapShot(String jobId){
		File outPutFile = new File("execution_profile_"+InputSpooler.batchName+".txt");
		BufferedWriter fileWriter = null;
		try{
			if(!outPutFile.exists()){
				outPutFile.createNewFile();
				fileWriter = new BufferedWriter(new FileWriter(outPutFile));
			}else{
				fileWriter = new BufferedWriter(new FileWriter(outPutFile,true));
			}
			//fileWriter.write("---------------------------------------------------\n");
			fileWriter.write("["+OperatingSystem.systemClock+"]\n");
			fileWriter.write("Running Job: "+jobId+"\n");
			fileWriter.write("Ready Queue: \n");
			for(String job:OperatingSystem.readyQueue){
				fileWriter.write(job+" | ");
			}
			fileWriter.write("\n");
			fileWriter.write("Block Queue: \n");
			for(String job:OperatingSystem.blockedQueue){
				fileWriter.write(job+" | ");
			}
			fileWriter.write("\n");
			int memoryframes = Memory.memoryUtilization();
			fileWriter.write("MEMORY Total: 256 W \t Used: "+((32-memoryframes)*8)+" W \t Free: "+(memoryframes*8)+" W\n");
			
			memoryframes = Disk.memoryUtilization();
			fileWriter.write("DISK Total: 2048 W \t Used: "+((256-memoryframes)*8)+" W \t Free: "+(memoryframes*8)+" W\n");
			
			fileWriter.write("Degree of Multiprogramming: "+OperatingSystem.readyQueue.size()+"\n");
			
			//fileWriter.write("---------------------------------------------------\n");
			fileWriter.flush();
			fileWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
