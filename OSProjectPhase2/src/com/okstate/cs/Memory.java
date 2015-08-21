package com.okstate.cs;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
 * 
 * @Routine : Memory
 *	1. Description:
 *			This is the virtual representation of physical memory. This routine is 
 *			responsible for creating the virtual memory. It allows MemoryMamager to
 *			add/modify/delete the content of the memory.
 *
 *	2. Global Variables:
 *		a. TreeMap<String,String> memory : 
 *				This is the actual memory. All the data is stored into this memory. Its
 *				a map in which "key" is the cell address and "value" is the data at that
 *				cell.
 *		b. memory_fmbv:
 *				Its a FMBV vector for the memory which gives the infrmation of frame accupancy
 *				in the memory.
 * 			
 */

public class Memory {
	
	static TreeMap<String,String> memory;
	static int [] memory_fmbv = new int[32];
	/*
	 * This method creates a virtual memory. This method creates memory only once.
	 * If called again it simply return without recreating the memory. Thus keeps
	 * only one instance of memory and prevent from resetting.
	 */
	public static void initializeMemory(){
		if(memory == null){
			memory = new TreeMap<String,String>();
			for(int i=0;i<256;i++){
				memory.put(Utility.decimalToHex(i, 2), "0000");
			}
			for(int i=0;i<memory_fmbv.length;i++){
				if(i==5||i==8||i==10||i==17||i==20||i==31){
					memory_fmbv[i] = 0;
				}else{
					memory_fmbv[i] = 1;
				}
				
			}
		}
	}
	
	/*
	 * This method loads data at the specified address.
	 */
	public static void writeMemory(String address, String data){
		memory.put(address, data);
	}
	
	public static void showMemoryContent(){
		int count = 1;
		for(String key: memory.keySet()){
			System.out.println(key + "------"+memory.get(key));
			if(count == 8){
				System.out.println();
				count = 0;
			}
			count++;
		}
	}
	
	/*
	 * This method reads information from memory at a given address.
	 */
	public static String readMemory(String address){
		return memory.get(address);
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
	
}
