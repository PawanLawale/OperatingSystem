package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 25-Feb-2014
 * 
 * @Routine : MemoryManager
 *	1. Description:
 *			MemoryManager is a routine which acts as a manager of every task which is performed
 *			on the memory. It checks for the validity of the task to be performed on the memory.
 *			It performs operations like
 *				a. Loading program into memory after all checks.
 *				b. Check if there is sufficient memory before beginning the load.
 *				c. Request to memory to read value at particular location.
 *			In short every operation which we perform on memory has to go through Memory manager.
 *
 *	2. Global Variables: NONE
 * 			
 */

public class MemoryManager {
	
	/*
	 * Constructor of MemoryManager which initializes memory.
	 */
	public MemoryManager(){
		Memory.initializeMemory();
	}
	
	/*
	 * This method loads input program word by word in memory, one word at a time.
	 * Before loading it performs certain checks like
	 * 	a. There should contain not more than 4 words in a single line.
	 * 	b. While loading, memory should not overflow. If overflow, raise warning and stop loading.
	 */
	public String loadProgram(String jobid, String address, String buffer) throws ErrorHandler{
		short location = 0;
		String hexLocation = null;
		try{
			location = (short)Integer.parseInt(address, 16);
			
			//variable to break words for loading
			int low = 0;
			int high = 4;
			
			boolean flag = true;
			
			//If there are more than 4 words in a single line, terminate and raise ERROR
			if(buffer.length()>16){
				//Error Invalid input File.
				throw new ErrorHandler(103);
			}
			
			//Start loading 4 words.
			while(flag){
				if(location < 255){
					if(high > buffer.length()){
						flag = false;
						high = buffer.length();
					}
					if(low<high){
						
						//Break like of 4 words into 1 word each
						String instruction = buffer.substring(low, high);
						Memory.addJobAddress(jobid, Utility.decimalToBinary(location, 8));
						Memory.loadProgram(Utility.decimalToBinary(location, 8), Utility.hexToBinary(instruction, 16));
						location++;
					}
					low = high;
					high += 4;
				}else{
					//Memory Overflow.
					flag = false;
					String msg = "WARNING: Memory overflow. Potential loss of instructions. Program execution may terminate Abnormally";
					System.out.println(msg);
					ErrorHandler.warningMessages.add(msg);
				}
			}
			hexLocation = Utility.decimalToHex(location, 2);
		}catch(NumberFormatException e){
			throw new ErrorHandler(109);
		}catch(ErrorHandler e){
			throw e;
		}
		
		return hexLocation;
	}
	
	/*
	 * This method check with the memory if there is appropriate space available to
	 * load the program, for the size of program given in the file as initialization
	 * parameter.
	 */
	public boolean canLoadProceed(String initialLine){
		boolean flag = false;
		String[] info = initialLine.split(" ");
		if(Integer.parseInt(info[3],16) <= Memory.availableMemory()){
			flag = true;
		}
		return flag;
	}
	
	/*
	 * Creates a map of Job and its corresponding addresses into the memory.
	 */
	public void addJobAddress(String[] info){
		Memory.addJobAddress(info[0], Integer.toBinaryString(Integer.parseInt(info[1],16)));
	}

	/*
	 * This method reads the instruction stored at the supplied address.
	 * Before doing that it checks of the memory which it is reading belongs
	 * its Job Id, if Not then it raises error of Illegal memory access.
	 * Else returns the instruction.
	 */
	public String getInstructionAtLocation(String jobId, String nextAddress) throws ErrorHandler{
		String instruction = null;
		if(Memory.isAddressOfJob(jobId, nextAddress)){
			instruction = Memory.valueAtAddress(nextAddress);
		}else{
			//Throw exception: Illegal memory access.
			throw new ErrorHandler(100);
		}
		
		return instruction;
	}
	
	/*
	 * During program execution, instruction requires to get the value
	 * at Effective Address (EA). This method provides the value at EA.
	 */
	public String getValueAtEA(String jobId, String nextAddress){
		String instruction = "";
		if(Memory.isAddressOfJob(jobId, nextAddress)){
			instruction = Memory.valueAtAddress(nextAddress);
		}
		return instruction;
	}
	
}
