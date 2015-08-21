package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
 * 
 * @Routine : Loader
 *	1. Description:
 *			This routine is used for loading the initial page into memory.
 *			It looks for the page on DISK containing the initial value of PC.
 *			Creats PCB for the job. Loads that page into the main MEMORY.
 *			Creates program SMT and PMT.
 *			
 */

import java.util.ArrayList;

public class Loader {
	
	/*
	 * This method initiates the loading process step by step.
	 * It first calculate the number of frames required to allocate
	 * for the job. Once found, it allocates those many frames in PCB.
	 * Then it initiates the process of moving required page into the
	 * allocated frame in the memory.
	 * 
	 */
	public void startLoading(String jobId) throws ErrorHandler{
		int totalPagesInJob = getTotalPagesInJob(jobId);
		int frameCount = Math.min(6, totalPagesInJob);
		OutputSpooler.memoryNoOfFrames = frameCount;
		PCB pcb = new PCB();
		createPCB(pcb,jobId,frameCount);
		OperatingSystem.pcbs.put(jobId, pcb);
		moveInitialPageIntoMemory(pcb);
		
	}
	
	/*
	 * This method checks for the initial value of the PC. 
	 * Accordingly moves the page into the memory and updates
	 * PMT for the new page entry.
	 */
	public void moveInitialPageIntoMemory(PCB pcb) throws ErrorHandler{
		DiskPMT diskPmt = Disk.getDiskSegmentPMT(pcb.jobId, 0);
		String[] pageAndFrame = diskPmt.findPageAndItsFrame(pcb.programCounter);
		if(pageAndFrame != null){
			int diskFrameBaseDec = Integer.parseInt(pageAndFrame[1],16);
			int physicalMemoryFrameAddress = pcb.allocatedFrames.get(0) * MemoryManager.PAGE_SIZE;
			for(int i=0;i<MemoryManager.PAGE_SIZE;i++){
				String diskMemoryHex = Utility.decimalToHex(diskFrameBaseDec, 3);
				String physicalMemoryHex = Utility.decimalToHex(physicalMemoryFrameAddress, 2);
				Memory.writeMemory(physicalMemoryHex, Disk.readDiskMemory(diskMemoryHex));
				diskFrameBaseDec++;
				physicalMemoryFrameAddress++;
			}
			pcb.smt.segment[0].pmt[0][1] = Integer.parseInt(pageAndFrame[0],16);
			pcb.smt.segment[0].pmt[0][2] = 1;
			pcb.smt.segment[0].pmt[0][3] = OperatingSystem.pageFaultClock++;
			pcb.smt.segment[0].pmt[0][4] = 0;
			
		}else{
			//Invalid address look up.
			throw new ErrorHandler(116);
		}
	}
	
	/*
	 * This method initializes PCB. 
	 */
	public void createPCB(PCB pcb, String jobId, int frameCount)  throws ErrorHandler{
		pcb.jobId = jobId;
		
		//allocate frames
		for(int i=0;i<frameCount;i++){
			int freeFrame = Memory.getFreeFrame();
			if(freeFrame != -1){
				pcb.allocatedFrames.add(freeFrame);
			}else{
				//Insufficient memory to load program.
				throw new ErrorHandler(107);
			}
		}
		
		//create SMT for program segment
		pcb.smt.segment[0] = new MemoryPMT(frameCount-2);
		for(int i=0; i< pcb.smt.segment[0].pmt.length;i++){
			pcb.smt.segment[0].pmt[i][0] = pcb.allocatedFrames.get(i);
			pcb.smt.segment[0].pmt[i][1] = null;
			pcb.smt.segment[0].pmt[i][2] = null;
			pcb.smt.segment[0].pmt[i][3] = null;
			pcb.smt.segment[0].pmt[i][4] = null;
		}
		
		//PC and BR value for the job
		pcb.baseAddress = Integer.parseInt(InputSpooler.jobParameters.get(jobId)[1],16);
		pcb.programCounter = Integer.parseInt(InputSpooler.jobParameters.get(jobId)[2],16);
	
	}
	
	/*
	 * This method calculates the total number of pages in the given Job
	 */
	public int getTotalPagesInJob(String jobId){
		int totalPages = 0;
		DiskSMT smt = Disk.diskJobStorage.get(jobId);
		if(smt.segment[0] != null){
			totalPages += smt.segment[0].pageFrameMap.size();
			OutputSpooler.noOfUnusedWords += getFragmentation(smt,0);
		}
		if(smt.segment[1] != null){
			totalPages += smt.segment[1].pageFrameMap.size();
			OutputSpooler.noOfUnusedWords += getFragmentation(smt,1);
		}
		if(smt.segment[2] != null){
			totalPages += smt.segment[2].pageFrameMap.size();
			OutputSpooler.noOfUnusedWords += getFragmentation(smt,2);
		}
		
		return totalPages;
	}
	
	/*
	 * This method checks for the internal fragmentation caused by the given segment.
	 * Returns the number of unutilized words.
	 */
	public int getFragmentation(DiskSMT smt,int segmentNo){
		int fragmentation = 0;
		String key = smt.segment[segmentNo].pageFrameMap.lastKey();
		String frameAddress = smt.segment[segmentNo].pageFrameMap.get(key);
		for(int i=0;i<8;i++){
			if(Disk.readDiskMemory(frameAddress)==null){
				fragmentation++;
			}
			int decAddress = Integer.parseInt(frameAddress, 16);
			frameAddress = Utility.decimalToHex(++decAddress, 3);
		}
		return fragmentation;
	}
}

/*
 * This is a structure of PCB
 */
class PCB{
	
	String jobId;
	MemorySMT smt;
	ArrayList<Integer> allocatedFrames;
	int programCounter;
	int baseAddress;
	public PCB(){
		smt = new MemorySMT();
		allocatedFrames = new ArrayList<Integer>();
	}
	
}

/*
 * This is a structure of SMT for memory
 */
class MemorySMT{
	MemoryPMT[] segment;
	public MemorySMT(){
		segment = new MemoryPMT[3];
	}
}

/*
 * This is a structure of PMT for memory
 */
class MemoryPMT{
	Integer[][] pmt;
	public MemoryPMT(int frames){
		pmt = new Integer[frames][6];
	}
	
	public Integer getNextAvailableFrameIndex(){
		Integer index = null;
		for(int i=0;i<pmt.length;i++){
			if(pmt[i][0] == null){
				index = i;
				break;
			}
		}
		return index;
	}
}