package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
 * 
 * @Routine : DISK
 *	1. Description:
 *			DISK is a virtual representation of the hard disk in OS.
 *			The size of DISK is 2048 words and it is divided into 256 frames
 *			each of size 8 words. It also has a FMBV for all the frames, to 
 *			indicate if the frame is accupied or available.
 *
 *	2. Global Variables:
 *			a. memory:
 *					Actual storage for data.
 *			b. memory_fmbv:
 *					Vector to indicate which frame is occupied and which one is available.
 * 			c. diskJobStorage:
 * 					Keep the track of Job page allocation in the frames for DISK.
 */
import java.util.TreeMap;

public class Disk {
	
	static TreeMap<String,String> memory;
	static int [] memory_fmbv;
	static TreeMap<String,DiskSMT> diskJobStorage;
	
	/*
	 * This method creates a virtual memory. This method creates memory only once.
	 * If called again it simply return without recreating the memory. Thus keeps
	 * only one instance of memory and prevent from resetting. 
	 */
	public static void initializeMemory(){
		if(memory == null){
			memory = new TreeMap<String,String>();
			for(int i=0;i<2048;i++){
				memory.put(Utility.decimalToHex(i, 3), null);
			}
			memory_fmbv = new int[256];
			for(int i:memory_fmbv){
				memory_fmbv[i] = 0;
			}
		}
		if(diskJobStorage == null){
			diskJobStorage = new TreeMap<String,DiskSMT>();
		}
	}
	
	public static void showMemoryContent(){
		int count = 1;
		for(String key: memory.keySet()){
			System.out.println(key + "------"+memory.get(key));
			if(count == 4){
				System.out.println();
				count = 0;
			}
			count++;
		}
	}
	
	/*
	 * This method finds and returns the available frame. If none
	 * is available it returns -1. 
	 */
	public static int getFreeFrame(){
		int frameNo = -1;
		for(int i=0;i<memory_fmbv.length;i++){
			if(memory_fmbv[i] == 0){
				frameNo = i;
				memory_fmbv[i] = 1;
				break;
			}
		}
		return frameNo;
	}

	/*
	 * This method loads the instruction at the specified address.
	 */
	public static void loadInstructionOntoDisk(String address,String instruction){
		memory.put(address, instruction);
	}
	
	/*
	 * This method creats a SMT and PMT for the DISK for a respective job.
	 * This is done to keep a track where in DISK we have loaded the job pages.
	 */
	public static void setDiskPageToFrameMapping(String jobId,int segment,String pageAddress,String frameAddress){
		if(diskJobStorage.get(jobId) == null){
			DiskSMT smt = new DiskSMT();
			smt.segment[segment] = new DiskPMT();
			smt.segment[segment].pageFrameMap.put(pageAddress, frameAddress);
			diskJobStorage.put(jobId, smt);
		}else{
			DiskSMT smt = diskJobStorage.get(jobId);
			DiskPMT pmt = null;
			if(smt.segment[segment] == null){
				smt.segment[segment] = new DiskPMT();
				pmt = smt.segment[segment];
			}else{
				pmt = smt.segment[segment];
			}
			pmt.pageFrameMap.put(pageAddress, frameAddress);
		}
		
	}
	
	/*
	 * It method returns the PMT for a given job and its segment.
	 */
	public static DiskPMT getDiskSegmentPMT(String jobId,int segment){
		DiskSMT smt = diskJobStorage.get(jobId);
		return smt.segment[segment];
	}
	
	/*
	 * This method reads information from disk at a given address.
	 */
	public static String readDiskMemory(String address){
		return memory.get(address);
	}
	
}

class DiskSMT{
	DiskPMT[] segment;
	public DiskSMT(){
		segment = new DiskPMT[3];
	}
}

class DiskPMT{
	TreeMap<String,String> pageFrameMap;
	public DiskPMT(){
		pageFrameMap = new TreeMap<String, String>();
	}
	
	public String[] findPageAndItsFrame(int virtualAddress){
		
		String[] pageFrame = new String[2];
		boolean found = false;
		for(String key: pageFrameMap.keySet()){
			int pageBase = Integer.parseInt(key,16);
			int pageEnd = pageBase + 7;
			if(virtualAddress >= pageBase && virtualAddress <= pageEnd){
				pageFrame[0] = key;
				pageFrame[1] = pageFrameMap.get(key);
				found = true;
				break;
			}
		}
		if(!found){
			pageFrame = null;
		}
		return pageFrame;
	}
	
}