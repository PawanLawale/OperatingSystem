package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
 * 
 * @Routine : InputSpooler
 *	1. Description:
 *			This routine is used to spool the input file.
 *			It reads the Job information from the input file.
 *			Converts the file into segment and pages and store
 *			the pages into the DISK's available frames.
 *
 *	2. Global Variables:
 *			a. programSegment:
 *					Contains all the instructions in Program segment.
 *			b. inputSegment:
 *					Contains all the data in the input segment.
 *			c. outputSegment:
 *					Allocate the required size of output segment.
 *			d. jobParameters:
 *					Stores the jobid and its program parameters for future use.
 *			e. PAGE_FRAME_SIZE:
 *					Constant variable to fix the frame size to 8.
 *			
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;

public class InputSpooler {
	
	public ArrayList<String> programSegment;
	public ArrayList<String> inputSegment;
	public ArrayList<String> outputSegment;
	public static TreeMap<String,String[]> jobParameters;
	public final int PAGE_FRAME_SIZE = 8;
	
	/*
	 * Constructor of the InputSpooler
	 */
	public InputSpooler(){
		if(jobParameters == null){
			jobParameters = new TreeMap<String, String[]>();
		}
	}
	
	
	/*
	 * Start spooling the input file.
	 */
	public void startSpooling() throws ErrorHandler{
		String fileName = readFileName();
		String jobId = readFileAndCreateSegment(fileName);
		loadPagesIntoDisk(jobId);
	}
	
	/*
	 * This method prompts for the Inputfile Name on the terminal.
	 */
	public String readFileName() throws ErrorHandler{
		String fileName = null;
		BufferedReader commandLineReader = new BufferedReader(new InputStreamReader(System.in));
		try{
			System.out.println("Enter File Name:");
			fileName = commandLineReader.readLine();
			commandLineReader.close();
		}catch(IOException e){
			throw new ErrorHandler(106);
		}
		return fileName;
	}
	
	/*
	 * This method reads the input file containing the job and performs below tasks:
	 * 	a. Reads the file line by line.
	 * 	b. Validate it as per JCL.
	 * 	c. If any violation found, it throws an error and terminates the program.
	 * 	d. It also validates for the contentes and parameter to be in HEX.
	 */
	public String readFileAndCreateSegment(String fileName) throws ErrorHandler{
		File inputFile = new File(fileName);
		String jobId = null;
		int inputJCLCount = 0;
		int finJCLCount = 0;
		try{
			if(inputFile.exists()){
				BufferedReader fileReader = new BufferedReader(new FileReader(inputFile));
				String line = fileReader.readLine();
				
				//check if the file is empty
				if(line == null){
					fileReader.close();
					//Invalid input file
					throw new ErrorHandler(103);
				}
				
				String[] jcl1 = line.split(" ");
				int sizeOfInputSeg = 0;
				int sizeOfOutputSeg = 0;
				if(jcl1 != null && jcl1.length == 3 && jcl1[0].equals("**JOB")){
					//read Input and Output segment size as a part of JCL. 
					//Check if they are valid hex values.
					try{
						sizeOfInputSeg = Integer.parseInt(jcl1[1], 16);
						sizeOfOutputSeg = Integer.parseInt(jcl1[2], 16);
					}catch(NumberFormatException e){
						fileReader.close();
						throw new ErrorHandler(109);
					}
					
					//read job initialization parameters
					line = fileReader.readLine();
					String[] parameters = line.split(" ");
					
					//check if # of initialization parameters are correct.
					if(parameters.length != 5){ 
						fileReader.close();
						//Invalid program initialization parameters.
						throw new ErrorHandler(103);
					}
					
					//check if values are in HEX
					try{
						Integer.parseInt(parameters[0], 16);
						Integer.parseInt(parameters[1], 16);
						Integer.parseInt(parameters[2], 16);
						Integer.parseInt(parameters[3], 16);
						Integer.parseInt(parameters[4], 16);
					}catch(NumberFormatException e){
						fileReader.close();
						throw new ErrorHandler(109);
					}
					
					int programSize = Integer.parseInt(parameters[3], 16);
					//OutputSpooler.noOfWords = programSize + sizeOfInputSeg + sizeOfOutputSeg;
					OutputSpooler.noOfOutputWords = sizeOfOutputSeg;
					jobId = parameters[0];
					
					
					line = fileReader.readLine();
					boolean inputFlag = false;
					while(line != null){
						
						if(line.length()>=7 && line.substring(0,7).equals("**INPUT")){
							inputJCLCount++;
							if(inputJCLCount>1){
								fileReader.close();
								throw new ErrorHandler(121);
							}
							inputFlag = true;
							line = fileReader.readLine();
							continue;
						}
						
						if(line.equals("**FIN")){
							finJCLCount++;
							break;
						}else if(inputFlag){
							makeSegment(line,"Input");
						}else{
							makeSegment(line,"Program");
						}
						line = fileReader.readLine();
					}
					
					//**INPUT missing
					if(sizeOfInputSeg>0 && inputJCLCount == 0){
						fileReader.close();
						throw new ErrorHandler(122);
					}
					
					if(finJCLCount == 0){
						fileReader.close();
						throw new ErrorHandler(123);
					}
					//validate if any conflict in # of words in Input segment vs given in **JOB line.
					if(sizeOfInputSeg != inputSegment.size()){
						fileReader.close();
						throw new ErrorHandler(115);
					}
					
					//create outputSegment
					makeOutputSegment(sizeOfOutputSeg);
					jobParameters.put(parameters[0], parameters);
					OperatingSystem.jobIds.add(jobId);
				}else{
					//Invalid Input file. Missing **JOB
					fileReader.close();
					throw new ErrorHandler(120);
				}
				
				fileReader.close();
			}else{
				//File not found ERROR
				throw new ErrorHandler(108);
			}
		}catch(IOException e){
			//IO Error while reading the file
			throw new ErrorHandler(106);
		}
		return jobId;
	}
	
	/*
	 * This method creates a segment list for a given segment.
	 * It breaks the line in a file into words and store them
	 * in a segment.
	 */
	public void makeSegment(String line, String segment){
		boolean flag = true;
		int low = 0;
		int high = 4;
		
		if(programSegment == null){
			programSegment = new ArrayList<String>();
		}
		if(inputSegment == null){
			inputSegment = new ArrayList<String>();
		}
		
		while(flag){
			if(high > line.length()){
				flag = false;
				high = line.length();
			}
			if(low<high){
				//Break like of 4 words into 1 word each
				String instruction = line.substring(low, high);
				if(segment.equals("Program")){
					programSegment.add(instruction);
				}else if(segment.equals("Input")){
					inputSegment.add(instruction);
				}
			}
			low = high;
			high += 4;
		}
	}
	
	/*
	 * This method creats reserves the space for output segemtn
	 * as per the given parameter in the input file.
	 */
	public void makeOutputSegment(int size) throws ErrorHandler{
		outputSegment = new ArrayList<String>();
		int noOfPages = (size/8);
		int mod = size % 8;
		if(mod>0){
			noOfPages+=1;
		}
		for(int i=0;i<(noOfPages*8);i++){
			outputSegment.add(null);
		}
	}
	
	/*
	 * This method initiates the process of loading the 
	 * segment into the memory, for each segment.
	 */
	public void loadPagesIntoDisk(String jobId) throws ErrorHandler{
		
		int diskFramesCount = 0;
		diskFramesCount += loadSegment(programSegment,jobId,0);
		if(inputSegment.size()>0){
			diskFramesCount += loadSegment(inputSegment,jobId,1);
		}
		if(outputSegment.size()>0){
			diskFramesCount += loadSegment(outputSegment,jobId,2);
		}
		OutputSpooler.diskNoOfFrames = diskFramesCount;
	}
	
	/*
	 * This method loads the given segment data into memory. It performs below tasks:
	 * 	a. It divides segment into the pages of the size of frame.
	 * 	b. reserve frame for the storing these pages into memory.
	 * 	c. Store the pages into the memory.
	 */
	public int loadSegment(ArrayList<String> segment, String jobId, int segId) throws ErrorHandler{
		int frameCount = 0;
		boolean loadFlag = true;
		int low = 0;
		int high = 8;
		
		String[] parameters = jobParameters.get(jobId);
		int virtualBaseAddress = Integer.parseInt(parameters[1],16);
		
		//Start loading 
		while(loadFlag){
			if(high > segment.size()){
				loadFlag = false;
				high = segment.size();
				if(low>=high){
					break;
				}
			}
			
			//get a free frame from DISK
			int freeFrame = Disk.getFreeFrame();
			if(freeFrame > -1){
				frameCount++;
				int frameBaseAddress = freeFrame * PAGE_FRAME_SIZE;
				
				Disk.setDiskPageToFrameMapping(jobId, segId, Utility.decimalToHex(virtualBaseAddress,2), Utility.decimalToHex(frameBaseAddress,3));
				
				for(int i=low;i<high;i++){
					String address = Utility.decimalToHex(frameBaseAddress,3);
					Disk.loadInstructionOntoDisk(address, segment.get(i));
					frameBaseAddress++;
					virtualBaseAddress++;
				}
				low = high;
				high+= 8;
			}else{
				//Insufficient memory to load program.
				throw new ErrorHandler(107);
			}
		}
		return frameCount;
	}
	
	
}