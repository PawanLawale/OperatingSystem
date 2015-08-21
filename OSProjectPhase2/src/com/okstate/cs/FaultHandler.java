package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
 * 
 * @Routine : FaultHandler
 *	1. Description:
 *			This routine is to handle the faults generated by the system.
 *			It handles Page Fault as well as Segment Fault. In case of page fault
 *			If there is a need to replace a page, this routine also provides
 *			the implementation of the Second Chance Algorithm, to replace the page
 *			from memory.
 *
 *	2. Global Variables:
 *			a. pageReplacementClock :
 *					This variable is used by the Second Chance algorithm to keep
 *					the track of arrival time of each page into memory.
 *			
 */

import java.util.ArrayList;
import java.util.TreeMap;

public class FaultHandler {

	public static int pageReplacementClock = 0;
	
	/*
	 * This method is used to handle a segment fault.
	 * It mainly performs below operations:
	 *   a. Takes in the Job Id for which the fault has 
	 * 		occured and also the segment number.
	 * 	 b. Creates a SMT for a given segment.
	 *   c. Returns true if segment is created, else 
	 *   	throws an error.
	 * 	  
	 */
	public static boolean segmentFaultHandler(String jobId, int segmentNo) throws ErrorHandler{
		boolean flag = false;
		try{
			PCB pcb = OperatingSystem.pcbs.get(jobId);
			pcb.smt.segment[segmentNo] = new MemoryPMT(1);
			int allocated = pcb.smt.segment[0].pmt.length;
			MemoryPMT memoryPmt = pcb.smt.segment[segmentNo];
			if(segmentNo == 1){
				memoryPmt.pmt[0][0] = pcb.allocatedFrames.get(allocated);
			}else if(segmentNo == 2){
				memoryPmt.pmt[0][0] = pcb.allocatedFrames.get(allocated+1);
			}else{
				// Invalid Segment creation request.
				 throw new ErrorHandler(117);
			}
			OperatingSystem.totalSegmentFaultTime += 5;
			OperatingSystem.systemClock += 5;
			flag = true;
			OperatingSystem.segmentFault = false;
		}catch(Exception e){
			throw new ErrorHandler(117);
		}
		return flag;
	}
	
	/*
	 * This method is used to handle Page Fault.
	 * The function performs following tasks:
	 * 	a. Checks if the page exist of DISK, if not then throw error.
	 * 	b. If exist, check if any frame is available in memory. If NOT
	 * 		then go for page replacement policy.
	 * 	c. If yes, move the page from disk in the available frame.
	 * 	d. In case of page replacement, it first find out which page to
	 * 		replace by Second change algorithm.
	 * 	e. Once the page is identified, it checks for its dirty bit. If
	 * 		it is set then it swaps the page with DISK, else it replaces
	 * 		the identified page with the new page in memory.
	 */
	public static boolean pageFaultHandler(String jobId, int segmentNo, int pageNo) throws ErrorHandler{
		
		boolean flag = false;
		String[] diskPageAndFrame = doesPageExistOnDisk(jobId, segmentNo, pageNo);
		if(diskPageAndFrame != null){
			Integer frameIndex = isPMTFull(jobId, segmentNo);
			if(frameIndex != null){
				//Get page from disk into memory when no replacement needed.
				replacePage(jobId, segmentNo,frameIndex,diskPageAndFrame);
			}else{
				//Use second chance algorithm to figure out which page to replace/swap
				PCB pcb = OperatingSystem.pcbs.get(jobId);
				MemoryPMT memoryPmt = pcb.smt.segment[segmentNo];
				
				frameIndex = secondChanceReplacement(jobId, segmentNo);
				if(frameIndex != null){
					if(memoryPmt.pmt[frameIndex][4] == 1){
						// swap page
						
						//Temp storage start
						int pageBaseAddress = memoryPmt.pmt[frameIndex][1];
						ArrayList<String> tempData = new ArrayList<String>();
						int frameAddress = memoryPmt.pmt[frameIndex][0] * MemoryManager.PAGE_SIZE;
						for(int i=0;i<MemoryManager.PAGE_SIZE;i++){
							String actualAddress = Utility.decimalToHex(frameAddress, 2);
							tempData.add(Memory.readMemory(actualAddress));
							frameAddress++;
						}
						//Temp storage end
						
						replacePage(jobId, segmentNo,frameIndex,diskPageAndFrame);
						
						//Store temp data back on DISK
						diskPageAndFrame = doesPageExistOnDisk(jobId, segmentNo, pageBaseAddress);
						int diskFrameBaseAddress = Integer.parseInt(diskPageAndFrame[1], 16);
						for(int i=0;i<MemoryManager.PAGE_SIZE;i++){
							String actualAddress = Utility.decimalToHex(diskFrameBaseAddress, 3);
							Disk.loadInstructionOntoDisk(actualAddress, tempData.get(i));
							diskFrameBaseAddress++;
						}
						
					}else{
						//replace page
						replacePage(jobId, segmentNo,frameIndex,diskPageAndFrame);
					}
				}else{
					//some error while searching page to replace.
					throw new ErrorHandler(119);
				}
			}
			OperatingSystem.totalPageFaultTime += 10;
			OperatingSystem.systemClock += 10;
			flag = true;
			OperatingSystem.pageFault = false;
		}else{
			//page does not exist on Disk.
			throw new ErrorHandler(118);
		}
		
		return flag;
	}
	
	/*
	 * This function is used to replace the page of memory with the one in disk.
	 */
	private static void replacePage(String jobId, int segmentNo,Integer frameIndex,String[] diskPageAndFrame){
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		MemoryPMT memoryPmt = pcb.smt.segment[segmentNo];
		int diskFrameBaseDec = Integer.parseInt(diskPageAndFrame[1],16);
		int physicalMemoryFrameAddress = memoryPmt.pmt[frameIndex][0] * MemoryManager.PAGE_SIZE;
		for(int i=0;i<MemoryManager.PAGE_SIZE;i++){
			String diskMemoryHex = Utility.decimalToHex(diskFrameBaseDec, 3);
			String physicalMemoryHex = Utility.decimalToHex(physicalMemoryFrameAddress, 2);
			Memory.writeMemory(physicalMemoryHex, Disk.readDiskMemory(diskMemoryHex));
			diskFrameBaseDec++;
			physicalMemoryFrameAddress++;
		}
		
		pcb.smt.segment[segmentNo].pmt[frameIndex][1] = Integer.parseInt(diskPageAndFrame[0],16);
		pcb.smt.segment[segmentNo].pmt[frameIndex][2] = 1;
		pcb.smt.segment[segmentNo].pmt[frameIndex][3] = OperatingSystem.pageFaultClock++;
		pcb.smt.segment[segmentNo].pmt[frameIndex][4] = 0;
	}
	
	private static String[] doesPageExistOnDisk(String jobId, int segmentNo, int pageNo){
		
		DiskSMT smt = Disk.diskJobStorage.get(jobId);
		DiskPMT diskPmt = smt.segment[segmentNo];
		String [] pageAndFrame = diskPmt.findPageAndItsFrame(pageNo);
		return pageAndFrame;
	}
	
	private static Integer isPMTFull(String jobId, int segmentNo){
		Integer index = null;
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		MemoryPMT memoryPmt = pcb.smt.segment[segmentNo];
		for(int i=0;i<memoryPmt.pmt.length;i++){
			if(memoryPmt.pmt[i][1] == null){
				index = i;
				break;
			}
		}
		return index;
	}
	
	/*
	 * This method implements the Second Chance replacement policy.
	 * It returns the frame number of whose page needed to be 
	 * replace as per the algorithm.
	 */
	private static Integer secondChanceReplacement(String jobId, int segmentNo){
		Integer index = null;
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		MemoryPMT memoryPmt = pcb.smt.segment[segmentNo];
		TreeMap<Integer,Integer> sortByClock = new TreeMap<Integer,Integer>();
		
		for(int i=0;i<memoryPmt.pmt.length;i++){
			sortByClock.put(memoryPmt.pmt[i][3], i);
		}
		
		boolean flag = true;
		
		while(flag){
			for(Integer key: sortByClock.keySet()){
				int frameIndex = sortByClock.get(key);
				MemoryPMT memPmt = pcb.smt.segment[segmentNo];
				if(memPmt.pmt[frameIndex][2]==1){
					memPmt.pmt[frameIndex][2] = 0;
				}else{
					index = frameIndex;
					break;
				}
			}
			if(index != null){
				flag = false;
			}
		}
		
		return index;
	}
}