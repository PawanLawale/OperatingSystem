package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 25-Feb-2014
 * 
 * @Routine : CPUManager
 *	1. Description:
 *			CPUManager is a routine written to take care of task which need CPU to perform
 *			action. CPUManager interacts directly with CPU. Its major task is to execute 
 *			the program loaded into the memory. To do this, it performs activities like:
 *				a. Read PC
 *				b. Load IR with instruction at address in PC
 *				c. Decode the instruction
 *				d. Execute the instruction
 *			It also generates the trace file, when required. It also interacts with MemoryManager
 *			to read/write data into memory.
 *
 *	2. Global Variables:
 *			a. MemoryManager memoryManager:
 *					This is the instance of MemoryManager used to connect to it.
 *			b. boolean traceFlag:
 *					This variable indicated whether to generate trace file or not.
 * 			c. infinityCheck:
 * 					This is a variable to keep check on the possibility of infinite loop.
 */

public class CPUManager {
	
	public MemoryManager memoryManager = null;
	public StringBuffer trace = null;
	boolean traceFlag = false;
	private int infinityCheck = 0;
	
	/*
	 * This is a constructor of CPUManager.
	 * It initializes the CPU and creates the instance of MemoryManager.
	 */
	public CPUManager(){
		CPU.initializeCPU();
		memoryManager = new MemoryManager();
	}
	
	/*
	 * This method initializes the CPU registers as described
	 * in the input program file.
	 */
	public void initializeCPURegisters(String baseRegister,String programCounter) throws ErrorHandler{
		try{
			CPU.setBaseRegister(Utility.hexToBinary(baseRegister, 8));
			CPU.setProgramCounter(Utility.hexToBinary(programCounter, 8));
		}catch(Exception e){
			throw new ErrorHandler();
		}
		
	}
	
	/*
	 * This is the function which begins with the execution of the program.
	 * It performs below operations:
	 * 	a. Checks if trace file needs to be generated.
	 * 	b. If trace file flag is invalid it give warning but continues to execute.
	 * 	c. Then it loops to read every instruction as per the PC value.
	 * 	d. It loads instruction into IR.
	 * 	e. Decodes the instruction.
	 * 	f. Execute the instruction.
	 * 	g. It also keeps track on the possibility of infinite loop. If the loop
	 * 		runs more than 50,000 iterations. It terminates, suspecting infinite loop.
	 * 	h. When the program execution terminates, it returns false to let OS know that
	 * 		Job is complete and to terminate itself.
	 */
	public boolean executeProgram(String jobId, String initialization) throws ErrorHandler{
		boolean flag = true;
		String info[] = initialization.split(" ");
		
		//If trace flag is not 0 or 1, then its invalid.
		if(!info[4].equals("1") && !info[4].equals("0")){
			String msg = "WARNING: Invalid trace flag. Trace file will not generate.";
			System.out.println(msg);
			ErrorHandler.warningMessages.add(msg);
		}
		
		//If trace flag is 1, set variables to generate trace file.
		if(info[4].equals("1")){
			traceFlag = true;
			trace = new StringBuffer();
		}
		
		//Loop to execute the loaded program in memory. 
		while(flag){
			
			//Infinity Check
			if(infinityCheck>50000){
				throw new ErrorHandler(110);
			}
			
			//Load next instruction to execute.
			loadInstructionIntoIR(jobId);
			
			//Decode the loaded instruction and execute it.
			flag = decodeAndExecuteInstruction(jobId);
			infinityCheck++;
		}
		return flag;
	}
	
	/*
	 * This method reads value from PC. Calculates actual memory address
	 * by adding base address to effective address. Retrieves the instruction
	 * from actual address and loads it into the IR.
	 */
	private void loadInstructionIntoIR(String jobId) throws ErrorHandler{
		String nextAddress = CPU.getProgramCounter();
		String actualAddress = Utility.binaryAddition(CPU.getBaseRegister(), nextAddress, 8);
		String instruction = memoryManager.getInstructionAtLocation(jobId, actualAddress);
		CPU.setInstructionRegister(instruction);
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
	private boolean decodeAndExecuteInstruction(String jobId) throws ErrorHandler{
		boolean flag = false;
		
		String instruction = CPU.getInstructionRegister();	
		
		/* Variables for logging trace values*/
		String pc = "";
		String br = "";
		String ir = "";
		int tosBefore = -1;
		String valueAtTOSBefore = "";
		
		int tosAfter = -1;
		String valueAtTOSAfter = "";
		
		String eaBeforeExecution = "";
		String valueAtEABeforeExecution = "";
		
		String eaAfterExecution = "";
		String valueAtEAAfterExecution = "";
		/*END of variable*/
		
		//Set trace file variables
		if(traceFlag){
			pc = CPU.getProgramCounter();
			br = CPU.getBaseRegister();
			ir = CPU.getInstructionRegister();
			tosBefore = CPU.getStack().getTOS();
			if(tosBefore<0){
				valueAtTOSBefore = "";
			}else{
				valueAtTOSBefore = CPU.getStack().valueAtTOS();
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
			
			String actualAddress = Utility.binaryAddition(CPU.getBaseRegister(), effectiveAddress, 8);
			
			eaBeforeExecution = effectiveAddress;
			valueAtEABeforeExecution = memoryManager.getValueAtEA(jobId, actualAddress);
			
			// Call Instruction Executor to execute the instruction
			flag = InstructionExecutor.oneAddressInstrutions(opCode, effectiveAddress, actualAddress, jobId, memoryManager);
			
			eaAfterExecution = effectiveAddress;
			valueAtEAAfterExecution = memoryManager.getValueAtEA(jobId, actualAddress);;
			
		}else{
			//its a Zero address instruction
			
			if(instruction.charAt(8) == '0'){
				String opCode1 = instruction.substring(3, 8);
				String opCode2 = instruction.substring(11,16);
				
				//Call Instruction executor
				flag = InstructionExecutor.zeroAddressInstrutions(opCode1);
				
				//Call second instruction only if 1st one was not HLT or RTN or invalid Op Code.
				if(flag && Integer.parseInt(opCode1, 2) != 21){
					flag = InstructionExecutor.zeroAddressInstrutions(opCode2);
				}
				
				//Increment program counter by 1
				CPU.setProgramCounter(Utility.binaryAddition(CPU.getProgramCounter(), "00000001", 8));
				
			}else{
				//Raise error. Invalid instruction
				throw new ErrorHandler(101);
			}
		}
		if(traceFlag){
			tosAfter = CPU.getStack().getTOS();
			if(tosAfter<0){
				valueAtTOSAfter = "";
			}else{
				valueAtTOSAfter = CPU.getStack().valueAtTOS();
			}
			traceFileWriter(pc,br,ir,
					tosBefore,valueAtTOSBefore,eaBeforeExecution,valueAtEABeforeExecution,
					tosAfter,valueAtTOSAfter,eaAfterExecution,valueAtEAAfterExecution);
		}
		return flag;
	}
	
	/*
	 * This method is to generate a trace of the recently executed instruction.
	 * This trace is then passed to trace file generator to write into a file.
	 */
	public void traceFileWriter(String pc,String br,String ir,
			int tosBefore,String valueAtTOSBefore,
			String eaBeforeExecution, String valueAtEABeforeExecution,
			int tosAfter, String valueAtTOSAfter,
			String eaAfterExecution, String valueAtEAAfterExecution) throws ErrorHandler{
		trace.setLength(0);
		trace.append(String.format("%4s %5s %8s %5s %14s %7s %14s %5s %14s %7s %14s", 
								((Utility.binaryToHex(pc, 2).length() == 0 ? "-":(Utility.binaryToHex(pc, 2))) + " | "),
								((Utility.binaryToHex(br, 2).length() == 0 ? "-":(Utility.binaryToHex(br, 2))) + " | "),
								((Utility.binaryToHex(ir, 4).length() == 0 ? "-":(Utility.binaryToHex(ir, 4))) + " || "),
								
								((Integer.toString(tosBefore).length() == 0 ? "-": Integer.toString(tosBefore)) + " | "),
								((Utility.binaryToHex(valueAtTOSBefore, 4).length() == 0 ? "-":(Utility.binaryToHex(valueAtTOSBefore, 4))) + " | "),
								((Utility.binaryToHex(eaBeforeExecution, 4).length() == 0 ? "-":(Utility.binaryToHex(eaBeforeExecution, 4))) + " | "),
								((Utility.binaryToHex(valueAtEABeforeExecution, 4).length() == 0 ? "-":(Utility.binaryToHex(valueAtEABeforeExecution, 4))) + " || "),
					
								((Integer.toString(tosAfter).length() == 0 ? "-": Integer.toString(tosAfter)) + " | "),
								((Utility.binaryToHex(valueAtTOSAfter, 4).length() == 0 ? "-":(Utility.binaryToHex(valueAtTOSAfter, 4))) + " | "),
								((Utility.binaryToHex(eaAfterExecution, 4).length() == 0 ? "-":(Utility.binaryToHex(eaAfterExecution, 4))) + " | "),
								((Utility.binaryToHex(valueAtEAAfterExecution, 4).length() == 0 ? "-":(Utility.binaryToHex(valueAtEAAfterExecution, 4))) + " || ")
								)
						);
		
		Loader.writeToTraceFile(trace);
	}
	
}
