package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : MemoryManager
 *	1. Description:
 *			This routine is used to perform all memory related activities:
 *			a. Virtual to Physical address calculation
 *			b. loading the instruction at value in PC into IR.
 *			
 */

public class MemoryManager {

	public static final int PAGE_SIZE = 8;
	
	/*
	 * Identifies which page to access for a given virtual address and a offset in it.
	 * 
	 */
	
	public static Integer[] pageForVirtualAddress(int virtualAddress){
		
		int pages = virtualAddress/PAGE_SIZE;
		int pageAddress = pages * PAGE_SIZE;
		int displacement = virtualAddress % PAGE_SIZE;
		Integer[] pageAddressAndDisplacement = new Integer[2];
		pageAddressAndDisplacement[0] = pageAddress;
		pageAddressAndDisplacement[1] = displacement;
		
		return pageAddressAndDisplacement;
	}
	
	/*
	 * This method accepts a virtual address of a job and its segment,
	 * and returns the actual physical address mapping of it on the memory.
	 */
	public static Integer virtualToPhysicalAddress(String jobId, int virtualAddress, int segmentNo){
		Integer physicalAddress = null;
		Integer[] pageAndDisplacement = pageForVirtualAddress(virtualAddress);
		if(segmentExist(jobId,pageAndDisplacement,segmentNo)){
			Integer frameIndex = pageExistInMemory(jobId,pageAndDisplacement,segmentNo);
			if(frameIndex != null){
				PCB pcb = OperatingSystem.pcbs.get(jobId);
				MemoryPMT memoryPmt = pcb.smt.segment[segmentNo];
				physicalAddress = (memoryPmt.pmt[frameIndex][0] * MemoryManager.PAGE_SIZE) + pageAndDisplacement[1];
			}else{
				OperatingSystem.pageFault = true;
				OperatingSystem.pageNo = pageAndDisplacement[0];
				OperatingSystem.segmentNo = segmentNo;
				OperatingSystem.segmentFault = false;
			}
		}else{
			OperatingSystem.pageFault = false;
			OperatingSystem.segmentFault = true;
			OperatingSystem.segmentNo = segmentNo;
		}
		return physicalAddress;
	}
	
	/*
	 * This method checks if the given segment for a job exists.
	 */
	public static boolean segmentExist(String jobId,Integer[] pageAndDisplacement, int segmentNo){
		boolean fault = false;
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		MemoryPMT segmentPmt = pcb.smt.segment[segmentNo];
		if(segmentPmt == null){
			fault = false;
		}else{
			fault = true;
		}
		return fault;
	}
	
	/*
	 * This method checks if the the page exists in the memory
	 * for a given and segment.
	 */
	public static Integer pageExistInMemory(String jobId,Integer[] pageAndDisplacement, int segmentNo){
		Integer frameIndex = null;
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		MemoryPMT segmentPmt = pcb.smt.segment[segmentNo];
		for(int i=0;i<segmentPmt.pmt.length;i++){
			if(segmentPmt.pmt[i][1] == pageAndDisplacement[0]){
				segmentPmt.pmt[i][2] = 1;
				frameIndex = i;
				break;
			}
		}
		return frameIndex;
	}
	
	/*
	 * This method reads the address in PC, converts that address
	 * into a actual physical address. If that address is in Memory,
	 * load the instruction at that address into IR. Else raise
	 * Page Fault and return to system.
	 */
	public static boolean loadInstructionIntoIR(String jobId){
		boolean fault = false;
		int virtualAddress = Integer.parseInt(CPU.getProgramCounter(),16);
		Integer[] pageAndDisplacement = pageForVirtualAddress(virtualAddress);
		
		Integer frameIndex = pageExistInMemory(jobId,pageAndDisplacement,0);
		if(frameIndex != null){
			PCB pcb = OperatingSystem.pcbs.get(jobId);
			MemoryPMT memoryPmt = pcb.smt.segment[0];
			int actualAddress = (memoryPmt.pmt[frameIndex][0] * 8) + pageAndDisplacement[1];
			String instruction = Memory.readMemory(Utility.decimalToHex(actualAddress, 2));
			CPU.setInstructionRegister(instruction);
		}else{
			OperatingSystem.segmentFault = false;
			OperatingSystem.pageFault = true;
			OperatingSystem.pageNo = pageAndDisplacement[0];
			OperatingSystem.segmentNo = 0;
			fault = true;
		}
			
		
		return fault;
	}
}
