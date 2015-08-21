package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : PCB
 *	1. Description:
 *			This is a structure of PCB.
 *			
 */

import java.util.ArrayList;

public class PCB {
	
	String jobId;
	MemorySMT smt;
	ArrayList<Integer> allocatedFrames;
	int programCounter;
	String instructionRegister;
	int baseAddress;
	int TOS;
	String[] stack;
	int arrivalTime;
	int completionTime;
	int expectedReadyTime;
	int traceFlag;
	int infinityCheck;
	int inputDisplacement;
	int outputDisplacement;
	
	int executionTime;
	int pageFaultTime;
	int segmentFaultTime;
	int totalIoTime;
	int errorHandlingTime;
	int cpuShots;
	int noOfIO;
	
	public PCB(){
		smt = new MemorySMT();
		allocatedFrames = new ArrayList<Integer>();
		stack = new String[7];
		TOS = -1;
	}
}

/*
 * This is a structure of SMT for memory
 */
class MemorySMT{
	MemoryPMT[] segment;
	public MemorySMT(){
		segment = new MemoryPMT[3];
	}
}

/*
 * This is a structure of PMT for memory
 */
class MemoryPMT{
	Integer[][] pmt;
	public MemoryPMT(int frames){
		pmt = new Integer[frames][6];
	}
	
	public Integer getNextAvailableFrameIndex(){
		Integer index = null;
		for(int i=0;i<pmt.length;i++){
			if(pmt[i][0] == null){
				index = i;
				break;
			}
		}
		return index;
	}
}