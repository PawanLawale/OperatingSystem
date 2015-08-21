package com.okstate.cs;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author : Pawan Lawale
 * @Date : 25-Feb-2014
 * 
 * @Routine : Memory
 *	1. Description:
 *			This is the virtual representation of physical memory. This routine is 
 *			responsible for creating the virtual memory. It allows MemoryMamager to
 *			add/modify/delete the content of the memory. It also serves to some of the
 *			requests made by MemoryManager like memory availability, read a value at
 *			EA.
 *
 *	2. Global Variables:
 *		a. TreeMap<String,String> memory : 
 *				This is the actual memory. All the data is stored into this memory. Its
 *				a map in which "key" is the cell address and "value" is the data at that
 *				cell.
 *		b. TreeMap<String,ArrayList<String>> jobAddressMap:
 *				This is the job to address mapping variable. It keeps track of the memory
 *				addresses being occupied by a particular job.
 * 			
 */

public class Memory {
	
	static TreeMap<String,String> memory;
	static TreeMap<String,ArrayList<String>> jobAddressMap;
	
	/*
	 * This method creates a virtual memory. This method creates memory only once.
	 * If called again it simply return without recreating the memory. Thus keeps
	 * only one instance of memory and prevent from resetting. It also does the same
	 * thing for job to address mapping variable.
	 */
	public static void initializeMemory(){
		if(memory == null){
			memory = new TreeMap<String,String>();
			for(int i=0;i<256;i++){
				memory.put(Utility.decimalToBinary(i, 8), null);
			}
		}
		if(jobAddressMap == null){
			jobAddressMap = new TreeMap<String,ArrayList<String>>();
		}
	}
	
	/*
	 * This method stores the address location stored by each job.
	 * If Job does not exist it creates new entry for it and adds
	 * address against new entry.
	 * Else it adds address against the supplied job as parameter.
	 */
	public static void addJobAddress(String jobId,String address){
		if(jobAddressMap.containsKey(jobId)){
			ArrayList<String> occupiedAddresses = jobAddressMap.get(jobId);
			if(!occupiedAddresses.contains(address)){
				occupiedAddresses.add(address);
			}
		}else{
			ArrayList<String> oppiedAddresses = new ArrayList<String>();
			oppiedAddresses.add(address);
			jobAddressMap.put(jobId, oppiedAddresses);
		}
	}
	
	
	/*
	 * This method calculates the available number of memory units to store new job.
	 */
	public static int availableMemory(){
		int availableUnits = 0;
		int occupiedUnits = 0;
 		for(String key : jobAddressMap.keySet()){
			ArrayList<String> occpupiedAddresses = jobAddressMap.get(key);
			occupiedUnits =+ occpupiedAddresses.size();
		}
 		availableUnits = 256 - occupiedUnits;
		return availableUnits;
	}
	
	/*
	 * Load one word of data into Memory. It takes address and data
	 * as input, and loads it data at the specified address.
	 */
	public static void loadProgram(String address, String data){
		memory.put(address,data);
	}
	
	
	public static void showMemoryContent(){
		for(String key: memory.keySet()){
			System.out.println(key + "------"+memory.get(key));
		}
	}
	
	/*
	 * This method returns the value at a particular address supplied
	 * to it as a parameter.
	 */
	public static String valueAtAddress(String address){
		return memory.get(address);
	}
	
	/*
	 * This method checks if the address passed to it is a part of the
	 * Job Id sent along with it.
	 * If yes, it returns true
	 * Else returns false.
	 */
	public static boolean isAddressOfJob(String jobId, String address){
		boolean flag = false;
		ArrayList<String> addresses = jobAddressMap.get(jobId);
		if(addresses != null && addresses.contains(address)){
			flag = true;
		}
		return flag;
	}
}
