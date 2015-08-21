package com.okstate.cs;

import java.util.ArrayList;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : Loader
 *	1. Description:
 *			This routine is used for loading the initial page into memory.
 *			It looks for the page on DISK containing the initial value of PC.
 *			Creats PCB for the job. Loads that page into the main MEMORY.
 *			Creates program SMT and PMT.
 *			
 */


public class Loader {
	
	private final int PAGE_SIZE=8;
	ArrayList<String> loadingErrors = new ArrayList<String>();
	/*
	 * This method initiates the loading process step by step.
	 * It first calculate the number of frames required to allocate
	 * for the job. Once found, it allocates those many frames in PCB.
	 * Then it initiates the process of moving required page into the
	 * allocated frame in the memory.
	 * 
	 */
	public boolean startLoading(String jobId){
		boolean flag = true;
		int totalPagesInJob = getTotalPagesInJob(jobId);
		int frameCount = Math.min(6, totalPagesInJob);

		PCB pcb = OperatingSystem.pcbs.get(jobId);
		if(pcb.allocatedFrames.size()==0){
			flag = allocateFrames(pcb,jobId,frameCount);
		}
		
		if(flag){
			boolean needLoading = true;
			//Check if page is already in memory
			MemoryPMT seg = pcb.smt.segment[0];
			int pageNo = pcb.programCounter/PAGE_SIZE;
			for(int i=0;i<seg.pmt.length;i++){
				if(seg.pmt[i][1] != null && seg.pmt[i][1] == (pageNo*PAGE_SIZE)){
					needLoading = false;
					break;
				}
			}
			
			//Check if loading is required
			if(needLoading){
				flag = moveInitialPageIntoMemory(pcb);
				if(!flag){
					ErrorHandler.writeErrors(jobId, loadingErrors);
					Disk.releaseDiskForJob(jobId);
					OperatingSystem.readyQueue.remove(jobId);
					flag = true;
				}else{
					flag = false;
				}
			}else{
				flag = false;
			}
			
		}else{
			OperatingSystem.blockedQueue.add(jobId);
			OperatingSystem.readyQueue.remove(jobId);
			flag = true;
		}
		
		return flag;
	}
	
	/*
	 * This method checks for the initial value of the PC. 
	 * Accordingly moves the page into the memory and updates
	 * PMT for the new page entry.
	 */
	public boolean moveInitialPageIntoMemory(PCB pcb){
		boolean flag = true;
		DiskPMT diskPmt = Disk.getDiskSegmentPMT(pcb.jobId, 0);
		String[] pageAndFrame = diskPmt.findPageAndItsFrame(pcb.programCounter);
		if(pageAndFrame != null){
			int diskFrameBaseDec = Integer.parseInt(pageAndFrame[1],16);
			int physicalMemoryFrameAddress = pcb.allocatedFrames.get(0) * PAGE_SIZE;
			for(int i=0;i<PAGE_SIZE;i++){
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
			
			//loading event
			String event = "["+OperatingSystem.systemClock + "] EVENT: Loading -- JobId: "+pcb.jobId;
			OutputSpooler.recordEvent(event);
		}else{
			//Invalid address look up.
			loadingErrors.add(ErrorHandler.recordError(116));
			flag = false;
		}
		return flag;
	}
	
	/*
	 * This method initializes PCB. 
	 */
	public boolean allocateFrames(PCB pcb, String jobId, int frameCount){
		boolean flag = true;
		pcb.jobId = jobId;
		
		//allocate frames
		for(int i=0;i<frameCount;i++){
			int freeFrame = Memory.getFreeFrame();
			if(freeFrame != -1){
				pcb.allocatedFrames.add(freeFrame);
			}else{
				//Insufficient memory to load program. Revert so far allocated frames, if any.
				if(pcb.allocatedFrames.size()>0){
					for(int fno: pcb.allocatedFrames){
						Memory.freeMemoryFrame(fno);
					}
				}
				pcb.allocatedFrames.clear();
				flag = false;
				break;
			}
		}
		
		//create SMT for program segment
		if(pcb.allocatedFrames.size()>0){
			pcb.smt.segment[0] = new MemoryPMT(frameCount-2);
			for(int i=0; i< pcb.smt.segment[0].pmt.length;i++){
				pcb.smt.segment[0].pmt[i][0] = pcb.allocatedFrames.get(i);
				pcb.smt.segment[0].pmt[i][1] = null;
				pcb.smt.segment[0].pmt[i][2] = null;
				pcb.smt.segment[0].pmt[i][3] = null;
				pcb.smt.segment[0].pmt[i][4] = null;
			}
		}
		return flag;
	}
	
	/*
	 * This method calculates the total number of pages in the given Job
	 */
	public int getTotalPagesInJob(String jobId){
		int totalPages = 0;
		DiskSMT smt = Disk.diskJobStorage.get(jobId);
		if(smt.segment[0] != null){
			totalPages += smt.segment[0].pageFrameMap.size();
		}
		if(smt.segment[1] != null){
			totalPages += smt.segment[1].pageFrameMap.size();
		}
		if(smt.segment[2] != null){
			totalPages += smt.segment[2].pageFrameMap.size();
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