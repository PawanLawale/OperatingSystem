package com.okstate.cs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;


/**
 * @author : Pawan Lawale
 * @Course # : CS-5323
 * @Assignment : A Simple Batch System
 * @Date : 25-Feb-2014
 * 
 * @Routine : OperatingSystem
 *	1. Description:
 *			OperatingSystem is a routine which starts up the virtual OS. First it creates the 
 *			virtual hardware i.e Memory & CPU. After creation it initializes these hardwares with
 *			default values. This is done in the constructor of OperatingSystem, where is instantiate
 *			loader and cpuManager Objects. Apart from hardware initialization, it also  initializes
 *			ErrorHandler Module, which is responsible to handle all the errors generated by OS.
 *			The functionality of individual method is explained in the comments above methods.
 *
 *	2. Global Variables:
 *		a. TreeMap<String,String> batch : 
 *				This is a map, which keeps track of JodId and its initialization parameters 
 *				(i.e. first line in the input file).
 *		b. Loader loader:
 *				This variable is an instance of LOADER module and is used to connect to that module.
 *		c. CPUManager cpuManager:
 *				CPUManager is a routine which is responsible for managing the various tasks for CPU.
 *				cpuManager is a instance variable of this routine. OS connects to CPUManager instead of
 *				directly connecting to CPU.
 *		
 * 			
 */
public class OperatingSystem{
	public Loader loader = null;
	public CPUManager cpuManager = null;
	public static TreeMap<String,String> batch;
	ErrorHandler eh = null;
	
	/*
	 * This is a constructor of OS
	 * It is responsible for initializing all the virtual hardwares i.e Memory and CPU.
	 * It also initializes the global variable "batch", used to keep track of all Jobs. 
	 */
	public OperatingSystem(){
		loader = new Loader();
		cpuManager = new CPUManager();
		batch = new TreeMap<String, String>();
		eh = new ErrorHandler();
	}
	
	/*
	 * This is a main method from where the OS begins its execution.
	 */
	public static void main(String args[]){
		OperatingSystem os = new OperatingSystem();
		try{
			os.startOS();
		}catch(ErrorHandler e){
			e.throwErrorMessage(e.getErrorCode());
		}
		os.generateOutputFile();
	}
	
	/*
	 * This method adds Job initialization i.e. JobId and initialization parameters
	 * details into "batch" variable map.
	 */
	public static void addJobToBatch(String jobId,String initialization){
		batch.put(jobId, initialization);
	}
	
	/*
	 * After the virtual hardware has been created by main() method, it kicks off the 
	 * execution of OS by calling this method. The 3 most important tasks performed by this
	 * method are:
	 * 1. Load the input program into memory.
	 * 2. Initialize the CPU registers according to the input File.
	 * 3. Execute the program loaded into the memory.
	 * 
	 * It has a while loop implementation which is not of any significance for this phase.
	 * It will be useful in future for phase 2 and phase 3, to read multiple Job files in
	 * single run. Currently implementation ensures the loop termination after exactly one job.
	 */
	public void startOS() throws ErrorHandler{
		boolean flag = true;
		BufferedReader commandLineReader = new BufferedReader(new InputStreamReader(System.in));
		String jobId = "";
		try{
			while(flag){
				String fileName = null;
				System.out.println("Enter File Name:");
				fileName = commandLineReader.readLine();
				if(fileName != null){
					loader.loadProgramIntoMemory(fileName);
					jobId = loader.initializeCPURegisters();
					cpuManager.executeProgram(jobId,batch.get(jobId));
					flag = false;
				}else{
					System.out.println("Invalid File Name.");
				}
				commandLineReader.close();
			}
			
		}catch(IOException e){
			throw new ErrorHandler(106);
		}catch(ErrorHandler e){
			throw e;
		}
	}
	
	/*
	 * This method is responsible for reading any input need by the loaded Job
	 * during its execution. It reads the input through keyboard when prompted by
	 * the executing job.
	 */
	public static String readInput() throws ErrorHandler{
		String input = null;
		try{
			BufferedReader commandLineReader = new BufferedReader(new InputStreamReader(System.in));
			input = commandLineReader.readLine();
		}catch(IOException e){
			//I/O ERROR handling
			throw new ErrorHandler(106);
		}	
		return input;
	}
	
	/*
	 * This method is responsible for generating the output file after the execution of the Job
	 * It writes the below details onto the file:
	 * 1. Job Id
	 * 2. Warnings (if any)
	 * 3. Termination nature.(Nornal Abnormal)
	 * 4. Program output
	 * 5. System Clock
	 */
	public void generateOutputFile(){
		File outPutFile = new File("OutputFile.txt");
		
		try{
			if(!outPutFile.exists()){
				outPutFile.createNewFile();
			}
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outPutFile));
			if(batch.size()>0){
				for(String key: batch.keySet()){
					fileWriter.write("Job Identification Number : "+key+"\n");
					fileWriter.write("Warnings : \n");
					for(String msg:ErrorHandler.warningMessages){
						fileWriter.write("\t\t"+msg+"\n");
					}
					fileWriter.write("Termination : "+ ErrorHandler.terminationStatues + "\n");
					if(ErrorHandler.terminationStatues.equals("Abnormal")){
						fileWriter.write("Error Message : " + ErrorHandler.terminationErrorMessage + "\n");
					}
					fileWriter.write("Output : \n");
					for(String output : InstructionExecutor.output){
						fileWriter.write("\t\t"+output + "\n");
					}
					fileWriter.write("CLOCK (vts) : " + Utility.decimalToHex(InstructionExecutor.systemClock, 4).toUpperCase() + "\n");
					fileWriter.write("Run Time:\n");
					fileWriter.write("\t\tExecution Time: " + InstructionExecutor.systemClock + "\n");
					fileWriter.write("\t\tInput/Output Time: " +InstructionExecutor.totalIoTime + "\n");
				}
			}else{
				fileWriter.write("Job Identification Number : ---\n");
				fileWriter.write("Warnings : ---\n");
				fileWriter.write("Termination : "+ErrorHandler.terminationStatues + "\n");
				if(ErrorHandler.terminationStatues.equals("Abnormal")){
					fileWriter.write("Error Message : " + ErrorHandler.terminationErrorMessage + "\n");
				}
				fileWriter.write("Output : ---\n");
				fileWriter.write("CLOCK in HEX (vts): "+ Utility.decimalToHex(InstructionExecutor.systemClock, 4).toUpperCase() + "\n");
				fileWriter.write("Run Time:\n");
				fileWriter.write("\t\tExecution Time: " + InstructionExecutor.systemClock + "\n");
				fileWriter.write("\t\tInput/Output Time: " +InstructionExecutor.totalIoTime + "\n");
			}
			fileWriter.flush();
			fileWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
}