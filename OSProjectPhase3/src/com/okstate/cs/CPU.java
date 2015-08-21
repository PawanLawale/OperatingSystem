package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 28-Apr-2014
 * 
 * @Routine : CPU
 *	1. Description:
 *			This routine is the implementation of virtual CPU. It contains
 *			various registers like IR, PC, BR. It also has a stack implementation
 *			which also acts as a accumulator during program execution.
 *
 *	2. Global Variables:
 *			a. programCounter :
 *					It is the implementation of PC.
 *			b. baseRegister:
 *					It is the implementation of BR.
 *			c. instructionRegister:
 *					It is the implementation of IR
 *			d. stack: 
 *					It is the reference variable of Stack.
 */

public class CPU {
	 private static String programCounter;
	 private static String baseRegister;
	 private static String instructionRegister;
	 private static Stack stack;
	 
	public static String getProgramCounter() {
		return programCounter;
	}
	public static void setProgramCounter(String programCounter) {
		CPU.programCounter = programCounter;
	}
	public static String getBaseRegister() {
		return baseRegister;
	}
	public static void setBaseRegister(String baseRegister) {
		CPU.baseRegister = baseRegister;
	}
	public static String getInstructionRegister() {
		return instructionRegister;
	}
	public static void setInstructionRegister(String instructionRegister) {
		CPU.instructionRegister = instructionRegister;
	}
	
	/*
	 * This the initialization of CPU registers and Stack.
	 * At any point in time, there will be always one instance
	 * of all of the registers and Stack.
	 */
	public static void initializeCPU(){
		if(programCounter == null){
			programCounter = new String();
		}
		if(baseRegister == null){
			baseRegister = new String();
		}
		if(instructionRegister == null){
			instructionRegister = new String();
		}
		if(stack == null){
			stack = new Stack();
			stack.initializeStack();
		}
	}
	
	public static Stack getStack() {
		return stack;
	}
	 
	
}

/*
 * This is the implementation of Stack used by the CPU.
 * Stack size is of 7 elements and TOS is initially -1
 * i.e. there is no data in it.
 */
class Stack{
	static String []stack;
	static int TOS;
	
	public void initializeStack(){
		if(stack == null){
			stack = new String[7];
			TOS = -1;
		}
	}
	
	public void push(String jobId,String data){
		if(TOS < 6){
			stack[TOS+1] = data;
			TOS++;
		}else{
			//Raise Error/Exception like Stack overflow.
			ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
		}
	}
	
	public String pop(String jobId){
		String data = null;
		if(TOS >= 0){
			data = stack[TOS];
			stack[TOS] = null;
			TOS--;
		}else{
			//Raise ERROR, Illegal access to stack.
			ErrorHandler.jobErrors.put(jobId, ErrorHandler.recordError(104));
		}
		return data;
	}
	
	public int getTOS(){
		return TOS;
	}
	
	public String valueAtTOS(){
		String data = "";
		if(TOS >= 0){
			data = stack[TOS];
		}
		return data;
	}
	
	public void showStack(){
		System.out.println("Stack Contents");
		System.out.println("Top of Stak: "+TOS);
		if(TOS != -1){
			for(int i = TOS; i >= 0; i--){
				System.out.println(i+" -> "+stack[i]);
			}
		}else{
			System.out.println("Stack Empty");
		}
		
	}
}