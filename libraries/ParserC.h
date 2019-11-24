/*
Stores received characters in a string
Splits string into substrings based on delimeter
*/

#ifndef PARSERC_H
#define PARSERC_H

#include <Arduino.h>
#include <SmartBuffer.h>

class ParserC{
	
	public:
		ParserC(String key, int size, char del, int* slotSize, void(*handle)(char**, int, int*));
		~ParserC();
		void put(char c);
		void reset();
		void test();
	
	private:
		int size;
		String key;
		char** buffer;
		char del;
		int delCount, index;
		int* sizes;
		
		SmartBuffer rcvBuffer, tempBuffer;
		
		int* slotSize;
		void(*handle)(char**, int, int*);
};

#endif