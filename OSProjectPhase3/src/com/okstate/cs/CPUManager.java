package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : CPUManager
 *	1. Description:
 *			CPUManager is a routine written to take care of task which need CPU to perform
 *			action. Its major task is to execute the program loaded into the memory. 
 *			To do this, it performs activities like:
 *				a. Read PC
 *				b. Load IR with instruction at address in PC by converting virtual address to Physical address.
 *				c. Decode the instruction
 *				d. Execute the instruction
 *			It also generates the trace file, when required. It also interacts with MemoryManager
 *			to read/write data into memory.
 *
 *	2. Global Variables:
 *			a. StringBuffer trace:
 *					This variable is used to store the single line of tracefile, if needed to generate.
 *			b. boolean traceFlag:
 *					This variable indicated whether to generate trace file or not.
 * 			c. timeslice:
 * 					This is a variable to keep check on time quantum.
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CPUManager {

	public static StringBuffer trace = null;
	static boolean traceFlag = false;
	static private boolean traceWarning = true;
	static int timeslice = 0;
	static int snapShotQuantum = 0;
	
	
	/*
	 * This function is the place from where the job execution starts.It loops infinitely, until any
	 * error, fault or program termination occures. Its two primary execution sequences are to load IR with next instruction
	 * to be executed and then execute the instrution in IR. 
	 */
	public static boolean executeJob(String jobId){
		boolean flag = true;
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		pcb.cpuShots++;
		
		//Record snapshot of system after every 1000 vts
		if(snapShotQuantum == 0 || snapShotQuantum <= OperatingSystem.systemClock){
			snapShotQuantum = OperatingSystem.systemClock + 1000;
			OutputSpooler.recordSnapShot(jobId);
		}
		
		//If trace flag is not 0 or 1, then its invalid.
		if( pcb.traceFlag != 1 && pcb.traceFlag != 0){
			String msg = "WARNING: Invalid trace flag. Trace file will not generate.\n";
			if(traceWarning){
				StringBuffer warning = ErrorHandler.warningMessages.get(jobId);
				warning = warning == null?new StringBuffer(): warning;
				ErrorHandler.warningMessages.put(jobId,warning.append(msg));
				traceWarning = false;
			}
			
		}
		
		//If trace flag is 1, set variables to generate trace file.
		if(pcb.traceFlag == 1){
			traceFlag = true;
			trace = new StringBuffer();
		}
		
		//restore CPU registers and job stack for a given job
		CPU.setBaseRegister(Utility.decimalToHex(pcb.baseAddress,2));
		CPU.setProgramCounter(Utility.decimalToHex(pcb.programCounter, 2));
		CPU.getStack().stack = pcb.stack;
		CPU.getStack().TOS = pcb.TOS;
		
		
		while(flag){
			
			if(timeslice>=20){
				flag = false;
				ProcessManager.contextSwitch = true;
				timeslice = 0;
				break;
			}
			//Infinity Check
			if(pcb.infinityCheck>100000){
				SystemInfo.timeOfInfiniteLoops += pcb.executionTime;
				SystemInfo.infiniteLoopJobs.append(jobId+" | ");
				ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(110));
				ProcessManager.error = true;
				flag = false;
				break;
			}
			
			//Load next instruction to execute.
			boolean fault = MemoryManager.loadInstructionIntoIR(jobId);
			if(fault){
				flag = false;
			}else{
				//Decode the loaded instruction and execute it.
				flag = decodeAndExecuteInstruction(jobId);
			}
			pcb.infinityCheck++;
		}
		
		if(!flag){
			//restore the PC and BR values back to PCB in case of Page/Segment Fault.
			pcb.baseAddress = Integer.parseInt(CPU.getBaseRegister(),16);
			pcb.programCounter = Integer.parseInt(CPU.getProgramCounter(),16);
			pcb.TOS = CPU.getStack().getTOS();
			pcb.stack = CPU.getStack().stack;
		}
		return flag;
	}
	
	/*
	 * This method reads the instruction from the IR.
	 * Decodes the instruction to identify its type i.e.
	 * One Address or Zero Address
	 * Depending on the address type, retrieve the Op Code and execute the instruction.
	 * If instruction does not satisfies to the rules of address type, throw ERROR as
	 * Invalid instruction.
	 * This method also generates trace file, if required.
	 * 
	 */
	public static boolean decodeAndExecuteInstruction(String jobId){
		boolean flag = false;
		if(CPU.getInstructionRegister() == null){
			ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(100));
			ProcessManager.error = true;
			flag = false;
			return flag;
		}
		String instruction = Utility.hexToBinary(CPU.getInstructionRegister(),16);
		
		
		//Set trace file variables before execution
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		if(pcb.traceFlag == 1){
			TraceFile.jobId = jobId;
			TraceFile.pc = CPU.getProgramCounter();
			TraceFile.br = CPU.getBaseRegister();
			TraceFile.ir = CPU.getInstructionRegister();
			TraceFile.tosBefore = CPU.getStack().getTOS();
			if(TraceFile.tosBefore<0){
				TraceFile.valueAtTOSBefore = "";
			}else{
				TraceFile.valueAtTOSBefore = CPU.getStack().valueAtTOS();
			}
		}
		
		if(instruction.charAt(0) == '1'){
			//its a one address instrucion
			
			String opCode = instruction.substring(1, 6);
			char index = instruction.charAt(6);
			String daddr = instruction.substring(9, 16);
			String effectiveAddress = null;
			if(index == '1'){
				effectiveAddress = Utility.binaryAddition(daddr, CPU.getStack().valueAtTOS(),8);
			}else{
				effectiveAddress = String.format("%8s", daddr).replace(" ", "0");
			}
			
			TraceFile.eaBeforeExecution = Utility.binaryToHex(effectiveAddress,2);
			
			// Call Instruction Executor to execute the instruction
			flag = InstructionExecutor.oneAddressInstrutions(opCode, Integer.parseInt(effectiveAddress,2),jobId);
			
			TraceFile.eaAfterExecution = Utility.binaryToHex(effectiveAddress,2);
			
			
		}else{
			//its a Zero address instruction
			
			if(instruction.charAt(8) == '0'){
				String opCode1 = instruction.substring(3, 8);
				String opCode2 = instruction.substring(11,16);
				
				//Call Instruction executor
				flag = InstructionExecutor.zeroAddressInstrutions(jobId, opCode1);
				
				//Call second instruction only if 1st one was not HLT or RTN or invalid Op Code.
				if((flag || ProcessManager.IO) && Integer.parseInt(opCode1, 2) != 21){
					flag = InstructionExecutor.zeroAddressInstrutions(jobId, opCode2);
				}
				
				if(flag || ProcessManager.IO){
					//Increment program counter by 1
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
				}
				
				
			}else{
				//Raise error. Invalid instruction
				ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(101));
				ProcessManager.error = true;
				flag = false;
				return flag;
			}
		}
		
		//set trace variables after execution and write the information into trace file
		if(pcb.traceFlag == 1 && (flag || ProcessManager.IO || ProcessManager.hault)){
			TraceFile.tosAfter = CPU.getStack().getTOS();
			if(TraceFile.tosAfter<0){
				TraceFile.valueAtTOSAfter = "";
			}else{
				TraceFile.valueAtTOSAfter = CPU.getStack().valueAtTOS();
			}
			
			boolean traceflag = TraceFile.traceFileWriter();
			if(!traceflag){
				flag = false;
				ProcessManager.error = true;
			}
		}
		
		return flag;
	}
}

/*
 * This routing is to gather trace file information and generate the trace file
 */
class TraceFile{
	/* Variables for logging trace values*/
	static String jobId = "";
	static String pc = "";
	static String br = "";
	static String ir = "";
	static int tosBefore = -1;
	static String valueAtTOSBefore = "";
	
	static int tosAfter = -1;
	static String valueAtTOSAfter = "";
	
	static String eaBeforeExecution = "";
	static String valueAtEABeforeExecution = "";
	
	static String eaAfterExecution = "";
	static String valueAtEAAfterExecution = "";
	/*END of variable*/
	
	static StringBuffer trace = new StringBuffer();
	public static boolean traceFileWriter(){
		boolean flag = true;
		trace.setLength(0);
		trace.append(String.format("%4s %5s %8s "
				+ "%5s %7s %5s %8s "
				+ "%5s %7s %5s %8s", 
								((pc.length() == 0 ? "-":pc.toUpperCase()) + " | "),
								((br.length() == 0 ? "-":br) + " | "),
								((ir.length() == 0 ? "-":ir) + " || "),
								
								((Integer.toString(tosBefore).length() == 0 ? "-": Integer.toString(tosBefore)) + " | "),
								((Utility.binaryToHex(valueAtTOSBefore, 4).length() == 0 ? "-":(Utility.binaryToHex(valueAtTOSBefore, 4))) + " | "),
								((eaBeforeExecution.length() == 0 ? "-":eaBeforeExecution) + " | "),
								((valueAtEABeforeExecution.length() == 0 ? "-":valueAtEABeforeExecution) + " || "),
					
								((Integer.toString(tosAfter).length() == 0 ? "-": Integer.toString(tosAfter)) + " | "),
								((Utility.binaryToHex(valueAtTOSAfter, 4).length() == 0 ? "-":(Utility.binaryToHex(valueAtTOSAfter, 4))) + " | "),
								((eaAfterExecution.length() == 0 ? "-":eaAfterExecution) + " | "),
								((valueAtEAAfterExecution.length() == 0 ? "-":valueAtEAAfterExecution) + " || ")
								)
						);
		
		flag = writeToTraceFile(jobId,trace);
		return flag;
	}
	
	/*
	 * This method is to generate the trace file, if the trace flag is set.
	 */
	public static boolean writeToTraceFile(String jobId,StringBuffer trace){
		boolean flag = true;
		try {
			File traceFile = new File(jobId+"_trace_file_"+InputSpooler.batchName+".txt");
			
			if(traceFile.exists()){
				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(traceFile,true));
				fileWriter.write(trace+"\n");
				fileWriter.flush();
				fileWriter.close();
			}else{ //If file does not exist create one.
					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(traceFile));
					traceFile.createNewFile();
					
					//Header line1
					fileWriter.write(String.format("%49s", "||      BEFORE EXECUTION     || ")
							+ String.format("%25s", "      AFTER EXECUTION     ||\n")
							);
					
					//Underline
					StringBuffer underLine = new StringBuffer();
					for(int i=0;i<77;i++){
						underLine.append("-");
					}
					fileWriter.write(underLine+"\n");
					
					//Header line2
					fileWriter.write(String.format("%5s", "PC | ")
										+ String.format("%6s", "BR | ")
										+ String.format("%9s", "IR || ")
										
										+ String.format("%6s", "TOS | ")
										+ String.format("%8s", "(TOS) | ")
										+ String.format("%6s", "EA | ")
										+ String.format("%9s", "(EA) || ")
										
										+ String.format("%6s", "TOS | ")
										+ String.format("%8s", "(TOS) | ")
										+ String.format("%6s", "EA | ")
										+ String.format("%9s", "(EA) ||\n")
										);
					fileWriter.write(String.format("%5s", "(H)| ")
							+ String.format("%6s", "(H) | ")
							+ String.format("%9s", "(H) || ")
							
							+ String.format("%6s", "(D) | ")
							+ String.format("%8s", "(H) | ")
							+ String.format("%6s", "(H) | ")
							+ String.format("%9s", "(H) || ")
							
							+ String.format("%6s", "(D) | ")
							+ String.format("%8s", "(H) | ")
							+ String.format("%6s", "(H) | ")
							+ String.format("%9s", "(H) ||\n")
							);
					
					//Underline
					underLine.setLength(0);
					for(int i=0;i<77;i++){
						underLine.append("-");
					}
					fileWriter.write(underLine+"\n");
					
					fileWriter.write(trace+"\n");
					fileWriter.flush();
					fileWriter.close();
			}
			
		} catch (IOException e) {
			//IO Error
			ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(106));
			ProcessManager.error = true;
			flag = false;
		}
		return flag;
	}
}