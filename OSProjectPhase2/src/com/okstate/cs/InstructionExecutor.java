package com.okstate.cs;

import java.util.ArrayList;

/**
 * @author : Pawan Lawale
 * @Date : 31-Mar-2014
 * 
 * @Routine : InstructionExecutor
 *	1. Description:
 *			This routine is to execute the instruction as per the Op Code sent to it.
 *			It executes both One Address and Zero Address instructions.
 *
 *	2. Global Variables:
 *			a. inputDisplacement :
 *					Keeps track of the input segment displacement.
 *			b. outputDisplacement:
 *					Keeps track of the output segment displacement.
 *			
 */

public class InstructionExecutor{
	
	public static int inputDisplacement = 0;
	public static int outputDisplacement = 0;
	
	
	/*
	 * This method executes the valid zero address Op Codes.
	 * If invalid Op Code is passed, it throws ERROR message.
	 */
	public static boolean zeroAddressInstrutions(String jobId, String opCode) throws ErrorHandler{
		boolean flag = true;
		String value1 = null;
		String value2 = null;
		String result = null;
		Integer physicalAddress = null;
		int dec1 = 0;
		int dec2 = 0;
		int code = Integer.parseInt(opCode, 2);
		switch(code){
			case 0://NOP
				break;
				
			case 1://OR
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binaryLogicalOperation(value1, value2, code);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 2://AND
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binaryLogicalOperation(value1, value2, code);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 3://NOT
				value1 = CPU.getStack().pop();
				result = Utility.binaryLogicalOperation(value1, null, code);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 4://XOR
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binaryLogicalOperation(value1, value2, code);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 5://ADD
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binaryAddition(value1, value2, 16);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 6://SUB
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binarySubtraction(value2, value1);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 7://MUL
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binaryMultipilcation(value1, value2);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 8://DIV
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binaryDivision(value2, value1);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 9://MOD
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().pop();
				result = Utility.binaryMod(value2, value1);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 10://SL
				value1 = CPU.getStack().pop();
				dec1 = Integer.parseInt(value1,2) << 1;
				result = Utility.decimalToBinary(dec1, 16);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 11://SR
				value1 = CPU.getStack().pop();
				dec1 = Integer.parseInt(value1,2) >> 1;
				result = Utility.decimalToBinary(dec1, 16);
				CPU.getStack().push(result);
				OperatingSystem.systemClock++;
				break;
				
			case 12://CPG
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().valueAtTOS();
				if(value2 == null || value2.equals("")){
					throw new ErrorHandler(104);
				}
				dec1 = (short)Integer.parseInt(value1,2);
				dec2 = (short)Integer.parseInt(value2,2);
				if(dec2 > dec1){
					CPU.getStack().push("1111111111111111");
				}else{
					CPU.getStack().push("0000000000000000");
				}
				OperatingSystem.systemClock++;
				break;
				
			case 13://CPL
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().valueAtTOS();
				if(value2 == null || value2.equals("")){
					throw new ErrorHandler(104);
				}
				dec1 = (short)Integer.parseInt(value1,2);
				dec2 = (short)Integer.parseInt(value2,2);
				if(dec2 < dec1){
					CPU.getStack().push("1111111111111111");
				}else{
					CPU.getStack().push("0000000000000000");
				}
				OperatingSystem.systemClock++;
				break;
				
			case 14://CPE
				value1 = CPU.getStack().pop();
				value2 = CPU.getStack().valueAtTOS();
				if(value2 == null || value2.equals("")){
					throw new ErrorHandler(104);
				}
				dec1 = (short)Integer.parseInt(value1,2);
				dec2 = (short)Integer.parseInt(value2,2);
				if(dec2 == dec1){
					CPU.getStack().push("1111111111111111");
				}else{
					CPU.getStack().push("0000000000000000");
				}
				OperatingSystem.systemClock++;
				break;
				
			case 19://RD
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, inputDisplacement, 1);
				if(physicalAddress != null){
					value1 = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					CPU.getStack().push(Utility.hexToBinary(value1, 16));
					OperatingSystem.systemClock += 15;
					OperatingSystem.totalIoTime += 15;
					inputDisplacement++;
					if(value1!=null){
						OutputSpooler.inputSegment.add(Integer.parseInt(value1,16)+"");
					}
					
				}else{
					flag = false;
				}
				
				break;
				
			case 20://WR
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, outputDisplacement, 2);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					if((short)Integer.parseInt(value1, 2)> 8191 || (short)Integer.parseInt(value1, 2) < -8192){
						ErrorHandler.warningMessages.add("WARNING: Potential Loss of bits");
					}
					if(value1.charAt(0)=='1'){
						StringBuilder temp = new StringBuilder(value1);
						for(int i=0;i<3;i++){
							temp.setCharAt(i, '1');
						}
						value1 = temp.toString();
					}else{
						StringBuilder temp = new StringBuilder(value1);
						for(int i=0;i<3;i++){
							temp.setCharAt(i, '0');
						}
						value1 = temp.toString();
					}
					short out = (short)Integer.parseInt(value1, 2);
					Memory.writeMemory(Utility.decimalToHex(physicalAddress,2), Utility.decimalToHex(out, 4));
					
					int frame = physicalAddress/MemoryManager.PAGE_SIZE;
					PCB pcb = OperatingSystem.pcbs.get(jobId);
					for(int i=0;i<pcb.smt.segment[2].pmt.length;i++){
						if(pcb.smt.segment[2].pmt[i][0] == frame){
							pcb.smt.segment[2].pmt[i][4] = 1;
							break;
						}
					}
					
					OutputSpooler.outputSegment.add(out+"");
					
					OperatingSystem.systemClock += 15;
					OperatingSystem.totalIoTime += 15;
					outputDisplacement++;
				}else{
					flag = false;
				}
				
				break;
				
			case 21://RTN
				value1 = CPU.getStack().pop();
				CPU.setProgramCounter(Utility.binaryToHex(value1,2));
				OperatingSystem.systemClock++;
				break;
				
			case 24://HLT
				OperatingSystem.pageFault = false;
				OperatingSystem.segmentFault = false;
				flag = false;
				//System.out.println("O: END");
				OperatingSystem.systemClock++;
				break;
				
			default:
				flag = false;
				//Raise exception of Invalid Op Code
				throw new ErrorHandler(113);
		}
		return flag;
	}
	
	/*
	 * This method executes the valid one address Op Codes.
	 * If invalid Op Code is passed, it throws ERROR message.
	 */
	public static boolean oneAddressInstrutions(String opCode,int effectiveAddress, String jobId) throws ErrorHandler{
		boolean flag = true;
		String value1 = null;
		String result = null;
		int dec1 = 0;
		int dec2 = 0;
		String valueAtAddress = "";
		int code = Integer.parseInt(opCode, 2);
		Integer physicalAddress = null;
		switch(code){
			case 0://NOP
				break;
				
			case 1://OR
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryLogicalOperation(value1, Utility.hexToBinary(valueAtAddress, 16), code);
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 2://AND
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryLogicalOperation(value1, Utility.hexToBinary(valueAtAddress, 16), code);
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 4://XOR
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryLogicalOperation(value1, Utility.hexToBinary(valueAtAddress, 16), code);
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page fault
					flag = false;
				}
				
				break;
				
			case 5://ADD
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryAddition(value1, Utility.hexToBinary(valueAtAddress, 16), 16);
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page fault
					flag = false;
				}
				break;
				
			case 6://SUB
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binarySubtraction(value1, Utility.hexToBinary(valueAtAddress, 16));
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 7://MUL
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryMultipilcation(value1, Utility.hexToBinary(valueAtAddress, 16));
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				break;
				
			case 8://DIV
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryDivision(value1, Utility.hexToBinary(valueAtAddress, 16));
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				break;
				
			case 9://MOD
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryMod(value1, Utility.hexToBinary(valueAtAddress, 16));
					CPU.getStack().push(result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				break;
				
			case 12://CPG
				value1 = CPU.getStack().valueAtTOS();
				if(value1 == null || value1.equals("")){
					throw new ErrorHandler(104);
				}
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					dec1 = (short)Integer.parseInt(value1,2);
					dec2 = (short)Integer.parseInt(Utility.hexToBinary(valueAtAddress, 16),2);
					if(dec1 > dec2){
						CPU.getStack().push("1111111111111111");
					}else{
						CPU.getStack().push("0000000000000000");
					}
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 13://CPL
				value1 = CPU.getStack().valueAtTOS();
				if(value1 == null || value1.equals("")){
					throw new ErrorHandler(104);
				}
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					dec1 = (short)Integer.parseInt(value1,2);
					dec2 = (short)Integer.parseInt(Utility.hexToBinary(valueAtAddress, 16),2);
					if(dec1 < dec2){
						CPU.getStack().push("1111111111111111");
					}else{
						CPU.getStack().push("0000000000000000");
					}
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 14://CPE
				value1 = CPU.getStack().valueAtTOS();
				if(value1 == null || value1.equals("")){
					throw new ErrorHandler(104);
				}
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					dec1 = (short)Integer.parseInt(value1,2);
					dec2 = (short)Integer.parseInt(Utility.hexToBinary(valueAtAddress, 16),2);
					if(dec2 == dec1){
						CPU.getStack().push("1111111111111111");
					}else{
						CPU.getStack().push("0000000000000000");
					}
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 15://BR
				CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				//valueAtAddress = memoryManager.getInstructionAtLocation(jobId, actualAddress);
				OperatingSystem.systemClock += 4;
				break;
				
			case 16://BRT
				value1 = CPU.getStack().pop();
				if(value1.equals("1111111111111111")){
					CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				}else{
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
				}
				OperatingSystem.systemClock += 4;
				break;
				
			case 17://BRF
				value1 = CPU.getStack().pop();
				if(value1.equals("0000000000000000")){
					CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				}else{
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
				}
				OperatingSystem.systemClock += 4;
				break;
				
			case 18://CALL
				CPU.getStack().push(Utility.hexToBinary(CPU.getProgramCounter(),16));
				CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				OperatingSystem.systemClock += 4;
				break;
				
			case 22://PUSH
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					CPU.getStack().push(Utility.hexToBinary(valueAtAddress, 16));
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 23://POP
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop();
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					Memory.writeMemory(Utility.decimalToHex(physicalAddress,2), Utility.binaryToHex(value1,4));
					
					//Memory page modified, therefore set dirty bit.
					int frame = physicalAddress/MemoryManager.PAGE_SIZE;
					PCB pcb = OperatingSystem.pcbs.get(jobId);
					for(int i=0;i<pcb.smt.segment[0].pmt.length;i++){
						if(pcb.smt.segment[0].pmt[i][0] == frame){
							pcb.smt.segment[0].pmt[i][4] = 1;
							break;
						}
					}

					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));

					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			default:
				flag = false;
				//Raise exception of Invalid Op Code
				throw new ErrorHandler(113);
		}
		
		return flag;
	}
	
}
