package com.okstate.cs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author : Pawan Lawale
 * @Date : 25-Feb-2014
 * 
 * @Routine : Loader
 *	1. Description:
 *			This is the representation of the LOADER module as described in specification.
 *			Loader communicates with MemoryManager and CPUManager. Loader mainly performs 
 *			below activities:
 *			a. Read input file of Jobs.
 *			b. Validate file for its correctness.
 *			c. Load program into memory.
 *			d. Initialize CPU registers
 *			e. Generate trace file if required.
 *
 *	2. Global Variables:
 *		a. MemoryManager memoryManager : 
 *				This is an instance of the MemoryManager required to communicate with MemoryManager functionality.
 *		b. CPUManager cpuManager:
 *				This is an instance of CPUManager required to communicate with CPUManager functionality.
 *		c. String initialLine:
 *				This is required to keep track of the initialization parameters as read from the input file.
 * 			
 */

public class Loader{

	String initialLine = null;
	MemoryManager memoryManager = null;
	CPUManager cpuManager = null;
	
	/*
	 * Its a constructor of LOADER.
	 * It instantiate both MemoryManager and CPUManager
	 */
	public Loader(){
		memoryManager = new MemoryManager();
		cpuManager = new CPUManager();
	}
	
	/*
	 * This method does the below tasks:
	 * 1. Check for the availability of input file.
	 * 2. If file not available, throw ERROR and terminate.
	 * 3. If file available, read it line by line.
	 * 4. Checks for the 5 initialization parameters on first line.
	 * 5. If its not exactly five, throw ERROR and terminate.
	 * 6. Else move to read next line, which is actual program.
	 * 7. Check if there is sufficient memory to load program.
	 * 8. If No the terminates with Error message, else proceed to load.
	 * 9. It also handles the I/O exception, if any occured during file read operation.
	 */
	public void loadProgramIntoMemory(String fileName) throws ErrorHandler{
		
		try{
			File inputFile = new File(fileName);
			if(inputFile.exists()){
				BufferedReader fileReader = new BufferedReader(new FileReader(inputFile));
				String line = fileReader.readLine();
				initialLine = line;
				
				//check if the file is empty
				if(line == null){
					fileReader.close();
					//Invalid input file
					throw new ErrorHandler(103);
				}
				
				String[] info = line.split(" ");
				
				//check if # of initialization parameters are correct.
				if(info.length != 5){ 
					fileReader.close();
					//Invalid program initialization parameters.
					throw new ErrorHandler(103);
				}
				
				// check if job can be loaded with available memory.
				if(memoryManager.canLoadProceed(line)){ 
					
					//this is used to keep track of addressed being used by job.
					memoryManager.addJobAddress(info);
					
					OperatingSystem.addJobToBatch(info[0], initialLine);
					line = fileReader.readLine();
					String address = info[1];
					
					//Load input file line by line 4 words at a time.
					while(line != null){
						
						//Stop loading if memory is full.
						if(address.equalsIgnoreCase("FF")){
							break;
						}
						address = memoryManager.loadProgram(info[0], address, line.trim());
						line = fileReader.readLine();
					}
					
				}else{
					//Insufficient Memory ERROR
					fileReader.close();
					throw new ErrorHandler(107);
				}
				fileReader.close();
				//Memory.showMemoryContent();
			}else{
				//File not found ERROR
				throw new ErrorHandler(108);
			}
		}catch(IOException e){
			//IO ERROR
			throw new ErrorHandler(106);
		}
		
	}
		
	/*
	 * This method is to initialize the CPU registers i.e. PC & BR
	 * as per the initialization parameters given in the file.
	 */
	public String initializeCPURegisters() throws ErrorHandler{
		String[] info = initialLine.split(" ");
		cpuManager.initializeCPURegisters(info[1], info[2]);
		return info[0];
	}
	
	/*
	 * This method is to generate the trace file, if the trace flag is set.
	 */
	public static void writeToTraceFile(StringBuffer trace) throws ErrorHandler{
		try {
			File traceFile = new File("trace_file.txt");
			
			if(traceFile.exists()){
				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(traceFile,true));
				fileWriter.write(trace+"\n");
				fileWriter.flush();
				fileWriter.close();
			}else{ //If file does not exist create one.
					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(traceFile));
					traceFile.createNewFile();
					
					//Header line1
					fileWriter.write(String.format("%64s", "||                BEFORE EXECUTION          || ")
							+ String.format("%45s", "AFTER EXECUTION           || \n")
							);
					
					//Underline
					StringBuffer underLine = new StringBuffer();
					for(int i=0;i<107;i++){
						underLine.append("-");
					}
					fileWriter.write(underLine+"\n");
					
					//Header line2
					fileWriter.write(String.format("%5s", "PC | ")
										+ String.format("%6s", "BR | ")
										+ String.format("%9s", "IR || ")
										
										+ String.format("%5s", "TOS | ")
										+ String.format("%15s", "Value at TOS | ")
										+ String.format("%8s", " EA | ")
										+ String.format("%14s", "Value at EA || ")
										
										+ String.format("%5s", "TOS | ")
										+ String.format("%15s", "Value at TOS | ")
										+ String.format("%8s", "EA | ")
										+ String.format("%14s", "Value at EA || \n")
										);
					
					//Underline
					underLine.setLength(0);
					for(int i=0;i<107;i++){
						underLine.append("-");
					}
					fileWriter.write(underLine+"\n");
					
					fileWriter.write(trace+"\n");
					fileWriter.flush();
					fileWriter.close();
			}
			
		} catch (IOException e) {
			//e.printStackTrace();
			throw new ErrorHandler(106);
		}
	}
}
