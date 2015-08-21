package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : ProcessManager
 *	1. Description:
 *			This routine takes care of the round robin 
 *			implementation of the process management.
 *			
 */

import java.util.ArrayList;
import java.util.HashSet;

public class ProcessManager {

	public static boolean contextSwitch = false;
	public static ArrayList<String> blockedQueue = new ArrayList<String>();
	public static boolean hault = false;
	public static boolean IO = false;
	public static boolean error = false;
	
	/*
	 * This function loops infinitely till a job is terminated
	 * either normally or abnormally. This function is responsible
	 * for moving jobs into blocked queue from ready and vice-vera
	 * whenever an interrupt occurrs.  
	 */
	public void startProcessing(Loader loader){
		while(true){
			if(OperatingSystem.readyQueue.size()>0){
				
				String jobId = OperatingSystem.readyQueue.get(0);
				OperatingSystem.readyQueue.remove(0);
				
				SystemInfo.totalProcessedJobs.add(jobId);
				 
				if(loader.startLoading(jobId)){
					continue;
				}
				PCB pcb = OperatingSystem.pcbs.get(jobId);
				boolean flag = CPUManager.executeJob(jobId);
				if(!flag){
					if(OperatingSystem.segmentFault || OperatingSystem.pageFault){
						//Fault
						if(OperatingSystem.segmentFault){
							if(FaultHandler.segmentFaultHandler(jobId, OperatingSystem.segmentNo)){
								OperatingSystem.blockedQueue.add(jobId);
							}else{
								//Write error to execution profile
								OutputSpooler.generateOutputFile(jobId);
								//release job
								releaseJob(jobId);
							}
						}
						
						//PageFault
						if(OperatingSystem.pageFault){
							SystemInfo.totalPF++;
							if(FaultHandler.pageFaultHandler(jobId, OperatingSystem.segmentNo, OperatingSystem.pageNo)){
								OperatingSystem.blockedQueue.add(jobId);
								String event = "["+OperatingSystem.systemClock + "] EVENT: Page fault -- JobId: "+jobId;
								OutputSpooler.recordEvent(event );
							}else{
								//Write error to execution profile
								OutputSpooler.generateOutputFile(jobId);
								//release job
								releaseJob(jobId);
							}
						}
						
					}else if(IO){
						//I/O
						String event = "["+OperatingSystem.systemClock + "] EVENT: I/O -- JobId: "+jobId;
						OutputSpooler.recordEvent(event );
						OperatingSystem.blockedQueue.add(jobId);
						IO =false;
					}else if(hault || error){
						//End of job or Error
						pcb.completionTime = OperatingSystem.systemClock;
						
						if(error){
							pcb.errorHandlingTime+=5;
							SystemInfo.noOfAbnormalJobs++;
							SystemInfo.timeOfAdnormalJobs+= pcb.executionTime;
						}else{
							SystemInfo.noOfNormalJobs++;
							SystemInfo.tATOfNormalJobs += (pcb.completionTime - pcb.arrivalTime);
							SystemInfo.waitingTimeOfNormalJobs += ((pcb.completionTime - pcb.arrivalTime) - pcb.executionTime);
						}
						
						
						gatherSystemInfoAfterTermination(jobId);
						
						//Write out to execution profile
						
						OutputSpooler.generateOutputFile(jobId);
						
						//Release job after writing to file.
						releaseJob(jobId);
						hault = false;
						error = false;
						break;
					}else if(contextSwitch){
						//End of time slice
						OperatingSystem.readyQueue.add(jobId);
						contextSwitch = false;
					}
				}
			}else if(OperatingSystem.readyQueue.size() == 0 && OperatingSystem.blockedQueue.size() > 0){
				OperatingSystem.systemClock++;
				OperatingSystem.idleTime++;
			}else{
				break;
			}
			
			if(OperatingSystem.blockedQueue.size() > 0){
				ArrayList<String> tempJobId = new ArrayList<String>();
				for(String jobid: OperatingSystem.blockedQueue){
					PCB tempPcb = OperatingSystem.pcbs.get(jobid);
					if(tempPcb.expectedReadyTime <= OperatingSystem.systemClock){
						OperatingSystem.readyQueue.add(tempPcb.jobId);
						tempJobId.add(tempPcb.jobId);
						//Block to Ready movement EVENT
						//String blockToReadyEvent = "["+OperatingSystem.systemClock + "] EVENT: Block to Read -- JobId: "+tempPcb.jobId;
						//OutputSpooler.recordEvent(blockToReadyEvent );
					}
				}
				for(String jobid: tempJobId){
					OperatingSystem.blockedQueue.remove(jobid);
				}
			}
		}
		
	}
	
	/*
	 * This method releases the memory and Disk 
	 * occupancy by the given job.
	 */
	public void releaseJob(String jobId){
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		for(int fno: pcb.allocatedFrames){
			Memory.freeMemoryFrame(fno);
		}
		Disk.releaseDiskForJob(jobId);
		OperatingSystem.pcbs.remove(jobId);
	}
	
	/*
	 * This method gathers the system wide information 
	 * of the OS when all jobs are executed.
	 */
	public void gatherSystemInfoAfterTermination(String jobId){
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		
		//Gather CPU Time
		SystemInfo.maxCPUTime = Math.max(SystemInfo.maxCPUTime, pcb.executionTime);
		if(SystemInfo.minCPUTime==0){
			SystemInfo.minCPUTime = pcb.executionTime;
		}else{
			SystemInfo.minCPUTime = Math.min(SystemInfo.minCPUTime, pcb.executionTime);
		}
		SystemInfo.totalCPUTime+=pcb.executionTime;
		
		//Gather JOB TAT
		SystemInfo.maxTAT = Math.max(SystemInfo.maxTAT, (pcb.completionTime - pcb.arrivalTime));
		if(SystemInfo.minTAT==0){
			SystemInfo.minTAT = (pcb.completionTime - pcb.arrivalTime);
		}else{
			SystemInfo.minTAT = Math.min(SystemInfo.minTAT, (pcb.completionTime - pcb.arrivalTime));
		}
		SystemInfo.totalTAT += (pcb.completionTime - pcb.arrivalTime);
		
		//Gather CPU Shots
		SystemInfo.maxCPUShots = Math.max(SystemInfo.maxCPUShots, pcb.cpuShots);
		if(SystemInfo.minCPUShots==0){
			SystemInfo.minCPUShots = pcb.cpuShots;
		}else{
			SystemInfo.minCPUShots = Math.min(SystemInfo.minCPUShots, pcb.cpuShots);
		}
		SystemInfo.totalCPUShots += pcb.cpuShots;
		
		//Gather IO reguests
		SystemInfo.maxIO = Math.max(SystemInfo.maxIO, pcb.noOfIO);
		if(SystemInfo.minIO==0){
			SystemInfo.minIO = pcb.noOfIO;
		}else{
			SystemInfo.minIO = Math.min(SystemInfo.minIO, pcb.noOfIO);
		}
		SystemInfo.totalIO += pcb.noOfIO;
	}
}

/*
 * Structure of the system wide information gathering class.
 */
class SystemInfo{
	public static HashSet<String> totalProcessedJobs = new HashSet<String>();
	
	public static int maxCPUTime;
	public static int minCPUTime;
	public static int totalCPUTime;
	
	public static int maxTAT;
	public static int minTAT;
	public static int totalTAT;
	
	public static int maxCodeSeg;
	public static int minCodeSeg;
	public static int totalCodeSeg;
	
	public static int maxInputSeg;
	public static int minInputSeg;
	public static int totalInputSeg;
	
	public static int maxOutputSeg;
	public static int minOutputSeg;
	public static int totalOutputSeg;
	
	public static int maxCPUShots;
	public static int minCPUShots;
	public static int totalCPUShots;
	
	public static int maxIO;
	public static int minIO;
	public static int totalIO;
	
	public static int noOfNormalJobs;
	public static int noOfAbnormalJobs;
	
	public static int timeOfAdnormalJobs;
	public static int timeOfInfiniteLoops;
	
	public static StringBuffer infiniteLoopJobs = new StringBuffer();
	
	public static int tATOfNormalJobs;
	public static int waitingTimeOfNormalJobs;
	
	public static int totalPF;
}
