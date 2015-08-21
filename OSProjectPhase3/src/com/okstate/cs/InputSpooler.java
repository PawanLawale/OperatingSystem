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
 *			d. PAGE_FRAME_SIZE:
 *					Constant variable to fix the frame size to 8.
 *			e. systemJobId:
 *					System generated job id for each job.
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
	
	private static int jobBegningPtr = 0;
	private static boolean isDiskFull = false;
	
	public final int PAGE_FRAME_SIZE = 8;
	
	public static int systemJobId = 1;
	public static String batchName = null;
	public ArrayList<String> spoolingErrors;
	/*
	 * Constructor of the InputSpooler
	 */
	public InputSpooler(){
		spoolingErrors = new ArrayList<String>();
	}
	
	
	/*
	 * Start spooling the input file.
	 */
	public boolean startSpooling(String fileName) throws ErrorHandler{
		boolean eof = true;
		eof = readFileAndCreateSegment(fileName);
		return eof;
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
			batchName = fileName;
			//commandLineReader.close();
		}catch(IOException e){
			throw new ErrorHandler(106);
		}
		return fileName;
	}
	
	/*
	 * This method reads the input file containing the job and performs below tasks:
	 * 	a. Reads the file line by line.
	 * 	b. Validate it as per JCL.
	 * 	c. If any violation found, it throws an error, skips that job and resume reading
	 * 		from the next **JOB.
	 * 	d. It also validates for the contents and parameter to be in HEX.
	 */
	public boolean readFileAndCreateSegment(String fileName) throws ErrorHandler{
		boolean eof = false;
		try{
			File inputFile = new File(fileName);
			if(inputFile.exists()){
				BufferedReader fileReader = new BufferedReader(new FileReader(inputFile));
				String line = null;
				int errorCount = 0;
				
				for(int i=0;i <= jobBegningPtr;i++){
					line = fileReader.readLine();
				}
				
				while(line != null){
					String[] jobLine = line.split(" ");
					if(jobLine[0].equals("**JOB")){
						line = parseFile(line,fileReader);
						if(spoolingErrors.size()>0){
							ErrorHandler.writeErrors((systemJobId-1)+"", spoolingErrors);
							spoolingErrors.clear();
						}
						if(isDiskFull){
							isDiskFull = false;
							break;
						}
					}else{
						//Invalid Job construction. **JOB missing
						if(errorCount != 1){
							//Invalid job construction. **JOB missing.
							spoolingErrors.add(ErrorHandler.recordError(120));
							ErrorHandler.writeErrors(systemJobId+"",spoolingErrors);
							spoolingErrors.clear();
							errorCount = 1;
						}
						line = fileReader.readLine();
						jobBegningPtr++;
					}
				}
				if(line == null){
					eof = true;
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
		return eof;
	}
	
	/*
	 * This method validates each job section in the batch file.
	 * It creates the Program Segment, Input Segment and Output Segment.
	 * Loads those segments into the DISK.
	 */
	public String parseFile(String line,BufferedReader fileReader) throws ErrorHandler{
		int jobJCLCount = 0;
		int inputJCLCount = 0;
		int sizeOfInputSegment = 0;
		int sizeOfOutputSegment = 0;
		int currentJobPtr = 0;
		
		String currentJobId = null;
		
		boolean inputFlag = false;
		boolean error = false;
		
		PCB pcb = new PCB();
		
		ArrayList<String> programSegment = new ArrayList<String>();
		ArrayList<String> inputSegment = new ArrayList<String>();
		ArrayList<String> outputSegment = new ArrayList<String>();
		
		while(line != null){
			try{
				if(line.contains("**JOB")){
					jobJCLCount++;
					if(jobJCLCount == 1){
						String[] jobLine = line.split(" ");
						if(jobLine.length == 3){
							try{
								sizeOfInputSegment = Integer.parseInt(jobLine[1], 16);
								sizeOfOutputSegment = Integer.parseInt(jobLine[2], 16);
								for(int i=0;i<sizeOfOutputSegment;i++){
									outputSegment.add(null);
								}
							}catch(NumberFormatException e){
								//Input/Output sizes not in Hex
								spoolingErrors.add(ErrorHandler.recordError(109));
								error = true;
							}
						}else{
							//Invalid **JOB format
							spoolingErrors.add(ErrorHandler.recordError(124));
							error = true;
						}
						
						line = fileReader.readLine();
						if(line != null){
							line = line.trim();
						}
						currentJobPtr++;
						
						String[] jobParams = line != null ? line.split(" "):null;
						if(jobParams!=null && jobParams.length == 5){
							try{
								Integer.parseInt(jobParams[0], 16);
								Integer.parseInt(jobParams[1], 16);
								Integer.parseInt(jobParams[2], 16);
								Integer.parseInt(jobParams[3], 16);
								Integer.parseInt(jobParams[4], 16);
								
								currentJobId = systemJobId +"."+ Integer.parseInt(jobParams[0], 16);
								systemJobId++;
								
								pcb.jobId = currentJobId;
								pcb.programCounter = Integer.parseInt(jobParams[2], 16);
								pcb.baseAddress = Integer.parseInt(jobParams[1], 16);
								pcb.traceFlag = Integer.parseInt(jobParams[4], 16);
								pcb.arrivalTime = OperatingSystem.systemClock;
								
							}catch(NumberFormatException e){
								//Job parameters not in Hex
								spoolingErrors.add(ErrorHandler.recordError(125));
								error = true;
							}
						}else{
							//Invalid Loader format.
							spoolingErrors.add(ErrorHandler.recordError(103));
							error = true;
						}
					}else{
						//Invalid loader format: Missing **FIN
						spoolingErrors.add(ErrorHandler.recordError(123));
						break;
					}
					
					
				}
				else if(line.equals("**INPUT")){
					inputJCLCount++;
					if(inputJCLCount > 1){
						//Invalid loader format for the job. More than one **INPUT.
						spoolingErrors.add(ErrorHandler.recordError(121));
						error = true;
						inputFlag = false;
					}else{
						inputFlag = true;
					}
				}
				else if(line.equals("**FIN")){
					if(sizeOfInputSegment>0 && inputJCLCount==0){
						//Invalid loader format.Missing **INPUT
						spoolingErrors.add(ErrorHandler.recordError(122));
						error = true;
					}
					if(sizeOfInputSegment>0 && inputSegment.size()==0){
						//Invalid loader format.Missing INPUT segment
						spoolingErrors.add(ErrorHandler.recordError(126));
						error = true;
					}
					if(!error){
						boolean flag = loadSegment(programSegment,currentJobId,0); 
						if(flag){
							flag = loadSegment(inputSegment,currentJobId,1);
							if(flag){
								flag = loadSegment(outputSegment,currentJobId,2);
								if(flag){
									pcb.arrivalTime = OperatingSystem.systemClock;
									OperatingSystem.pcbs.put(pcb.jobId, pcb);
									OperatingSystem.readyQueue.add(pcb.jobId);
									line = fileReader.readLine();
									if(line != null){
										line = line.trim();
									}
									currentJobPtr++;
									jobBegningPtr += currentJobPtr;
									
									//Gather job segment information
									gatherSegmentInfo(programSegment.size(),inputSegment.size(),outputSegment.size());
									
								}else{
									isDiskFull = true;
								}
							}else{
								isDiskFull = true;
							}
						}else{
							isDiskFull = true;
						}
					}else{
						line = fileReader.readLine();
						if(line != null){
							line = line.trim();
						}
						currentJobPtr++;
						jobBegningPtr += currentJobPtr;
					}
					break;
				}else{
					if(line.length() > 16 || line.length()%4 != 0){
						//Invalid loader format
						spoolingErrors.add(ErrorHandler.recordError(103));
						error = true;
					}else{
						if(inputFlag){
							String[] instrutions = line.split("(?<=\\G....)");
							for(String str: instrutions){
								try{
									Integer.parseInt(str, 16);
									inputSegment.add(str);
								}catch(NumberFormatException e){
									spoolingErrors.add(ErrorHandler.recordError(103));
									error = true;
								}
							}
						}else{
							String[] instrutions = line.split("(?<=\\G....)");
							for(String str: instrutions){
								try{
									Integer.parseInt(str, 16);
									programSegment.add(str);
								}catch(NumberFormatException e){
									spoolingErrors.add(ErrorHandler.recordError(103));
									error = true;
								}
							}
						}
					}
				}
				line = fileReader.readLine();
				if(line != null){
					line = line.trim();
				}
				currentJobPtr++;
			}catch(IOException e){
				//IO Error while reading the file
				throw new ErrorHandler(106);
			}
			
		}
		return line;
	}
	
	/*
	 * This function loads the given segment onto the DISK, for a given jobId.
	 * If there isn't enough space on DISK, it revert the load done so far, 
	 * for the given job.
	 */
	public boolean loadSegment(ArrayList<String> segment, String jobId, int segmentNo){
		boolean flag = true;
		int i = 0;
		int frameNo = -1;
		int frameAddress = 0;
		int pageAddress = 0;
		for(String inst:segment){
			if(i%8 == 0){
				frameNo = Disk.getFreeFrame();
				frameAddress = frameNo * PAGE_FRAME_SIZE;
			}
			if(frameNo != -1){
				if(i%8 == 0){
					pageAddress = i;
					Disk.setDiskPageToFrameMapping(jobId, segmentNo, Utility.decimalToHex(pageAddress,2), Utility.decimalToHex(frameAddress, 3));
				}
				Disk.loadInstructionOntoDisk(Utility.decimalToHex(frameAddress, 3), inst);
				frameAddress++;
			}else{
				//Insufficient memory on Disk.
				Disk.releaseDiskForJob(jobId);
				flag = false;
				break;
			}
			i++;
		}
		return flag;
	}
	
	/*
	 * This method gathers the system info for each job about its segment size.
	 */
	public void gatherSegmentInfo(int sizeOfCode, int sizeOfInput, int sizeOfOutput){
		
		//Code Segment Info
		SystemInfo.maxCodeSeg = Math.max(SystemInfo.maxCodeSeg, sizeOfCode);
		if(SystemInfo.minCodeSeg==0){
			SystemInfo.minCodeSeg = sizeOfCode;
		}else{
			SystemInfo.minCodeSeg = Math.min(SystemInfo.minCodeSeg, sizeOfCode);
		}
		SystemInfo.totalCodeSeg += sizeOfCode;
		
		//Input Segment Info
		SystemInfo.maxInputSeg = Math.max(SystemInfo.maxInputSeg, sizeOfInput);
		if(SystemInfo.minInputSeg==0){
			SystemInfo.minInputSeg = sizeOfInput;
		}else{
			SystemInfo.minInputSeg = Math.min(SystemInfo.minInputSeg, sizeOfInput);
		}
		SystemInfo.totalInputSeg += sizeOfInput;
		
		//Output Segment Info
		SystemInfo.maxOutputSeg = Math.max(SystemInfo.maxOutputSeg, sizeOfOutput);
		if(SystemInfo.minOutputSeg==0){
			SystemInfo.minOutputSeg = sizeOfOutput;
		}else{
			SystemInfo.minOutputSeg = Math.min(SystemInfo.minOutputSeg, sizeOfOutput);
		}
		SystemInfo.totalOutputSeg += sizeOfOutput;
	}
}