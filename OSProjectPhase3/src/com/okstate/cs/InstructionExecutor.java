package com.okstate.cs;

import java.util.ArrayList;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : InstructionExecutor
 *	1. Description:
 *			This routine is to execute the instruction as per the Op Code sent to it.
 *			It executes both One Address and Zero Address instructions.
 *
 */

public class InstructionExecutor{
	
	/*
	 * This method executes the valid zero address Op Codes.
	 * If invalid Op Code is passed, it throws ERROR message.
	 */
	public static boolean zeroAddressInstrutions(String jobId, String opCode){
		boolean flag = true;
		String value1 = null;
		String value2 = null;
		String result = null;
		Integer physicalAddress = null;
		int dec1 = 0;
		int dec2 = 0;
		int code = Integer.parseInt(opCode, 2);
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		switch(code){
			case 0://NOP
				break;
				
			case 1://OR
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binaryLogicalOperation(value1, value2, code);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				CPUManager.timeslice++;
				pcb.executionTime++;
				break;
				
			case 2://AND
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binaryLogicalOperation(value1, value2, code);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 3://NOT
				value1 = CPU.getStack().pop(jobId);
				result = Utility.binaryLogicalOperation(value1, null, code);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 4://XOR
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binaryLogicalOperation(value1, value2, code);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 5://ADD
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binaryAddition(value1, value2, 16);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 6://SUB
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binarySubtraction(value2, value1);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 7://MUL
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binaryMultipilcation(value1, value2);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 8://DIV
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binaryDivision(jobId,value2, value1);
				if(result == null){
					flag = false;
				}else{
					CPU.getStack().push(jobId,result);
				}
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 9://MOD
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().pop(jobId);
				result = Utility.binaryMod(value2, value1);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 10://SL
				value1 = CPU.getStack().pop(jobId);
				dec1 = Integer.parseInt(value1,2) << 1;
				result = Utility.decimalToBinary(dec1, 16);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 11://SR
				value1 = CPU.getStack().pop(jobId);
				dec1 = Integer.parseInt(value1,2) >> 1;
				result = Utility.decimalToBinary(dec1, 16);
				CPU.getStack().push(jobId,result);
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 12://CPG
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().valueAtTOS();
				CPU.getStack().push(jobId,value1);
				if(value2 == null || value2.equals("")){
					//ErrorHandler.recordError(104);
					ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
					ProcessManager.error = true;
					flag = false;
				}
				dec1 = (short)Integer.parseInt(value1,2);
				dec2 = (short)Integer.parseInt(value2,2);
				if(dec2 > dec1){
					CPU.getStack().push(jobId,"1111111111111111");
				}else{
					CPU.getStack().push(jobId,"0000000000000000");
				}
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 13://CPL
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().valueAtTOS();
				CPU.getStack().push(jobId,value1);
				if(value2 == null || value2.equals("")){
					//ErrorHandler.recordError(104);
					ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
					ProcessManager.error = true;
					flag = false;
				}
				dec1 = (short)Integer.parseInt(value1,2);
				dec2 = (short)Integer.parseInt(value2,2);
				if(dec2 < dec1){
					CPU.getStack().push(jobId,"1111111111111111");
				}else{
					CPU.getStack().push(jobId,"0000000000000000");
				}
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 14://CPE
				value1 = CPU.getStack().pop(jobId);
				value2 = CPU.getStack().valueAtTOS();
				CPU.getStack().push(jobId,value1);
				if(value2 == null || value2.equals("")){
					//ErrorHandler.recordError(104);
					ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
					ProcessManager.error = true;
					flag = false;
				}
				dec1 = (short)Integer.parseInt(value1,2);
				dec2 = (short)Integer.parseInt(value2,2);
				if(dec2 == dec1){
					CPU.getStack().push(jobId,"1111111111111111");
				}else{
					CPU.getStack().push(jobId,"0000000000000000");
				}
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 19://RD
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, pcb.inputDisplacement, 1);
				if(physicalAddress != null){
					value1 = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					
					if(value1!=null){
						CPU.getStack().push(jobId,Utility.hexToBinary(value1, 16));
						//OperatingSystem.systemClock += 15;
						pcb.totalIoTime += 15;
						pcb.inputDisplacement++;
						
						StringBuffer input = OutputSpooler.inputSegment.get(jobId);
						input = input == null?new StringBuffer(): input;
						OutputSpooler.inputSegment.put(jobId,input.append("\t"+Integer.parseInt(value1,16)+"\n"));
						ProcessManager.IO = true;
					}else{
						ProcessManager.error = true;
						ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(100));
					}
					pcb.expectedReadyTime = OperatingSystem.systemClock + 15;
					pcb.noOfIO++;
					flag = false;
				}else{
					flag = false;
					ProcessManager.IO = false;
				}
				
				break;
				
			case 20://WR
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, pcb.outputDisplacement, 2);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					if((short)Integer.parseInt(value1, 2)> 8191 || (short)Integer.parseInt(value1, 2) < -8192){
						StringBuffer warning = ErrorHandler.warningMessages.get(jobId);
						warning = warning == null?new StringBuffer(): warning;
						ErrorHandler.warningMessages.put(jobId,warning.append("WARNING: Potential Loss of bits.\n"));
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
					for(int i=0;i<pcb.smt.segment[2].pmt.length;i++){
						if(pcb.smt.segment[2].pmt[i][0] == frame){
							pcb.smt.segment[2].pmt[i][4] = 1;
							break;
						}
					}
					
					StringBuffer output = OutputSpooler.outputSegment.get(jobId);
					output = output == null?new StringBuffer(): output;
					OutputSpooler.outputSegment.put(jobId,output.append("\t"+out+"\n"));
					
					//OperatingSystem.systemClock += 15;
					pcb.totalIoTime += 15;
					pcb.outputDisplacement++;
					pcb.expectedReadyTime = OperatingSystem.systemClock + 15;
					ProcessManager.IO = true;
					pcb.noOfIO++;
					flag = false;
				}else{
					flag = false;
					ProcessManager.IO = false;
				}
				
				break;
				
			case 21://RTN
				value1 = CPU.getStack().pop(jobId);
				CPU.setProgramCounter(Utility.binaryToHex(value1,2));
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				break;
				
			case 24://HLT
				OperatingSystem.pageFault = false;
				OperatingSystem.segmentFault = false;
				flag = false;
				//.println("O: END");
				OperatingSystem.systemClock++;
				pcb.executionTime++;
				CPUManager.timeslice++;
				ProcessManager.hault = true;
				ProcessManager.IO = false;
				break;
				
			default:
				flag = false;
				//Raise exception of Invalid Op Code
				ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(113));
				ProcessManager.error = true;
		}
		return flag;
	}
	
	/*
	 * This method executes the valid one address Op Codes.
	 * If invalid Op Code is passed, it throws ERROR message.
	 */
	public static boolean oneAddressInstrutions(String opCode,int effectiveAddress, String jobId){
		boolean flag = true;
		String value1 = null;
		String result = null;
		int dec1 = 0;
		int dec2 = 0;
		String valueAtAddress = "";
		int code = Integer.parseInt(opCode, 2);
		Integer physicalAddress = null;
		PCB pcb = OperatingSystem.pcbs.get(jobId);
		switch(code){
			case 0://NOP
				break;
				
			case 1://OR
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryLogicalOperation(value1, Utility.hexToBinary(valueAtAddress, 16), code);
					CPU.getStack().push(jobId,result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 2://AND
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryLogicalOperation(value1, Utility.hexToBinary(valueAtAddress, 16), code);
					CPU.getStack().push(jobId,result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 4://XOR
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryLogicalOperation(value1, Utility.hexToBinary(valueAtAddress, 16), code);
					CPU.getStack().push(jobId,result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page fault
					flag = false;
				}
				
				break;
				
			case 5://ADD
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryAddition(value1, Utility.hexToBinary(valueAtAddress, 16), 16);
					CPU.getStack().push(jobId,result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page fault
					flag = false;
				}
				break;
				
			case 6://SUB
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binarySubtraction(value1, Utility.hexToBinary(valueAtAddress, 16));
					CPU.getStack().push(jobId,result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 7://MUL
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryMultipilcation(value1, Utility.hexToBinary(valueAtAddress, 16));
					CPU.getStack().push(jobId,result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				break;
				
			case 8://DIV
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryDivision(jobId,value1, Utility.hexToBinary(valueAtAddress, 16));
					if(result == null){
						flag = false;
					}else{
						CPU.getStack().push(jobId,result);
						CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					}
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				break;
				
			case 9://MOD
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					result = Utility.binaryMod(value1, Utility.hexToBinary(valueAtAddress, 16));
					CPU.getStack().push(jobId,result);
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				break;
				
			case 12://CPG
				value1 = CPU.getStack().valueAtTOS();
				if(value1 == null || value1.equals("")){
					//ErrorHandler.recordError(104);
					ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
					ProcessManager.error = true;
					flag = false;
				}else{
					physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
					if(physicalAddress != null){
						valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
						TraceFile.valueAtEABeforeExecution = valueAtAddress;
						
						dec1 = (short)Integer.parseInt(value1,2);
						dec2 = (short)Integer.parseInt(Utility.hexToBinary(valueAtAddress, 16),2);
						if(dec1 > dec2){
							CPU.getStack().push(jobId,"1111111111111111");
						}else{
							CPU.getStack().push(jobId,"0000000000000000");
						}
						CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
						
						valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
						TraceFile.valueAtEAAfterExecution = valueAtAddress;
						OperatingSystem.systemClock += 4;
						pcb.executionTime+=4;
						CPUManager.timeslice += 4;
					}else{
						//page or segment fault
						flag = false;
					}
				}
				
				break;
				
			case 13://CPL
				value1 = CPU.getStack().valueAtTOS();
				if(value1 == null || value1.equals("")){
					//ErrorHandler.recordError(104);
					ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
					ProcessManager.error = true;
					flag = false;
				}else{
					physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
					if(physicalAddress != null){
						valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
						TraceFile.valueAtEABeforeExecution = valueAtAddress;
						
						dec1 = (short)Integer.parseInt(value1,2);
						dec2 = (short)Integer.parseInt(Utility.hexToBinary(valueAtAddress, 16),2);
						if(dec1 < dec2){
							CPU.getStack().push(jobId,"1111111111111111");
						}else{
							CPU.getStack().push(jobId,"0000000000000000");
						}
						CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
						
						valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
						TraceFile.valueAtEAAfterExecution = valueAtAddress;
						OperatingSystem.systemClock += 4;
						pcb.executionTime+=4;
						CPUManager.timeslice += 4;
					}else{
						//page or segment fault
						flag = false;
					}
				}
				break;
				
			case 14://CPE
				value1 = CPU.getStack().valueAtTOS();
				if(value1 == null || value1.equals("")){
					//ErrorHandler.recordError(104);
					ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
					ProcessManager.error = true;
					flag = false;
				}else{
					physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
					if(physicalAddress != null){
						valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
						TraceFile.valueAtEABeforeExecution = valueAtAddress;
						
						dec1 = (short)Integer.parseInt(value1,2);
						dec2 = (short)Integer.parseInt(Utility.hexToBinary(valueAtAddress, 16),2);
						if(dec2 == dec1){
							CPU.getStack().push(jobId,"1111111111111111");
						}else{
							CPU.getStack().push(jobId,"0000000000000000");
						}
						CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
						
						valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
						TraceFile.valueAtEAAfterExecution = valueAtAddress;
						OperatingSystem.systemClock += 4;
						pcb.executionTime+=4;
						CPUManager.timeslice += 4;
					}else{
						//page or segment fault
						flag = false;
					}
				}
				break;
				
			case 15://BR
				CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				//valueAtAddress = memoryManager.getInstructionAtLocation(jobId, actualAddress);
				OperatingSystem.systemClock += 4;
				pcb.executionTime+=4;
				break;
				
			case 16://BRT
				value1 = CPU.getStack().pop(jobId);
				if(value1.equals("1111111111111111")){
					CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				}else{
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
				}
				OperatingSystem.systemClock += 4;
				pcb.executionTime+=4;
				CPUManager.timeslice += 4;
				break;
				
			case 17://BRF
				value1 = CPU.getStack().pop(jobId);
				if(value1.equals("0000000000000000")){
					CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				}else{
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
				}
				OperatingSystem.systemClock += 4;
				pcb.executionTime+=4;
				CPUManager.timeslice += 4;
				break;
				
			case 18://CALL
				CPU.getStack().push(jobId,Utility.hexToBinary(CPU.getProgramCounter(),16));
				CPU.setProgramCounter(Utility.decimalToHex(effectiveAddress,2));
				OperatingSystem.systemClock += 4;
				pcb.executionTime+=4;
				CPUManager.timeslice += 4;
				break;
				
			case 22://PUSH
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
				
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					CPU.getStack().push(jobId,Utility.hexToBinary(valueAtAddress, 16));
					CPU.setProgramCounter(Utility.decimalToHex((Integer.parseInt(CPU.getProgramCounter(),16)+1),2));
					
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEAAfterExecution = valueAtAddress;
					OperatingSystem.systemClock += 4;
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			case 23://POP
				
				physicalAddress = MemoryManager.virtualToPhysicalAddress(jobId, effectiveAddress, 0);
				if(physicalAddress != null){
					value1 = CPU.getStack().pop(jobId);
					valueAtAddress = Memory.readMemory(Utility.decimalToHex(physicalAddress,2));
					TraceFile.valueAtEABeforeExecution = valueAtAddress;
					
					Memory.writeMemory(Utility.decimalToHex(physicalAddress,2), Utility.binaryToHex(value1,4));
					
					//Memory page modified, therefore set dirty bit.
					int frame = physicalAddress/MemoryManager.PAGE_SIZE;
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
					pcb.executionTime+=4;
					CPUManager.timeslice += 4;
				}else{
					//page or segment fault
					flag = false;
				}
				
				break;
				
			default:
				flag = false;
				//Raise exception of Invalid Op Code
				ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(113));
				ProcessManager.error = true;
		}
		
		return flag;
	}
	
}
