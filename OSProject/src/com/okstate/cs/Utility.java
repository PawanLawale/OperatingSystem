package com.okstate.cs;

/**
 * @author : Pawan Lawale
 * @Date : 25-Feb-2014
 * 
 * @Routine : Utility
 *	1. Description:
 *			This is a generic utility routine. It provide access to whole OS
 *			the general functionality like various conversion from binary to Hex
 *			and vice versa. It also provides methods for binary Arithmetic and 
 *			Logical operations.
 *
 *	2. Global Variables : NONE
 *			
 */

public class Utility {

	public static String decimalToBinary(int decimal,int numberOfBits){
		String binaryString = Integer.toBinaryString(decimal);
		String result = String.format("%"+numberOfBits+"s", binaryString).replace(" ", "0");
		if(result.length()>16){
			result = result.substring(result.length()-16, result.length());
		}
		return result;
	}
	
	public static String hexToBinary(String hex, int numberOfBits){
		int decimal = Integer.parseInt(hex,16);
		String binaryString = Integer.toBinaryString(decimal);
		String result = String.format("%"+numberOfBits+"s", binaryString).replace(" ", "0");
		if(result.length()>16){
			result = result.substring(result.length()-16, result.length());
		}
		return result;
	}
	
	public static String decimalToHex(int decimal, int numberOfChars){
		String result = String.format("%"+numberOfChars+"s", Integer.toHexString(decimal)).replace(" ", "0");
		if(result.length()>16){
			result = result.substring(result.length()-16, result.length());
		}
		return result;
	}
	
	public static String binaryToHex(String binary, int numberOfChars){
		String result = "";
		if(binary != null && !binary.equals("")){
			if(binary.length() > 16){
				binary = binary.substring(binary.length()-16, binary.length());
			}
			int decimal = Integer.parseInt(binary, 2);
			result = String.format("%"+numberOfChars+"s", Integer.toHexString(decimal)).replace(" ", "0").toUpperCase();
		}
		return result;
	}
	
	/*
	 * Biranry ARITHMATIC OPERATIONS
	 */
	public static String binaryAddition(String binary1,String binary2,int numberOfBits){
		int b1 = (short)Integer.parseInt(binary1, 2);
		int b2 = (short)Integer.parseInt(binary2, 2);
		int b3 = (short)(b1 + b2);
		String result = decimalToBinary(b3, numberOfBits);
		return result;
	}
	
	public static String binarySubtraction(String binary1,String binary2){
		int b1 = (short)Integer.parseInt(binary1, 2);
		int b2 = (short)Integer.parseInt(binary2, 2);
		int b3 = (short)(b1 - b2);
		String result = decimalToBinary(b3, 16);
		return result;
	}
	
	public static String binaryMultipilcation(String binary1,String binary2){
		int b1 = (short)Integer.parseInt(binary1, 2);
		int b2 = (short)Integer.parseInt(binary2, 2);
		int b3 = (short)(b1 * b2);
		String result = decimalToBinary(b3, 16);
		return result;
	}
	
	public static String binaryDivision(String binary1,String binary2) throws ErrorHandler{
		int b1 = (short)Integer.parseInt(binary1, 2);
		int b2 = (short)Integer.parseInt(binary2, 2);
		if(b2 == 0){
			throw new ErrorHandler(112);
		}
		int b3 = (short)(b1 / b2);
		String result = decimalToBinary(b3, 16);
		return result;
	}
	
	public static String binaryMod(String binary1,String binary2){
		int b1 = (short)Integer.parseInt(binary1, 2);
		int b2 = (short)Integer.parseInt(binary2, 2);
		int b3 = (short)(b1 % b2);
		String result = decimalToBinary(b3, 16);
		return result;
	}
	
	/*
	 * Binary LOGICAL OPERTATIONS
	 */
	
	public static String binaryLogicalOperation(String binary1, String binary2, int operation){
		String result = null;
		int d1=0;
		int d2=0;
		int d3=0;
		switch(operation){
			case 1:
				d1 = (short)Integer.parseInt(binary1,2);
				d2 = (short)Integer.parseInt(binary2,2);
				d3 = (short)(d1 | d2);
				result = Utility.decimalToBinary(d3, 16);
				break;
			case 2:
				d1 = (short)Integer.parseInt(binary1,2);
				d2 = (short)Integer.parseInt(binary2,2);
				d3 = (short)(d1 & d2);
				result = Utility.decimalToBinary(d3, 16);
				break;
			case 3:
				d1 = (short)Integer.parseInt(binary1,2);
				d3 = (short)~d1;
				result = Utility.decimalToBinary(d3, 16);
				break;
			case 4:
				d1 = (short)Integer.parseInt(binary1,2);
				d2 = (short)Integer.parseInt(binary2,2);
				d3 = (short)(d1 ^ d2);
				result = Utility.decimalToBinary(d3, 16);
				break;
		}
		return result;
	}
}
