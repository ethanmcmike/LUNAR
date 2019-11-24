/*
Acts as a state machine which receives characters.
Returns true when full() is called only if a sequence of characters matched the key.
Once full, the buffer remains full until reset.
*/

#ifndef SMARTBUFFER_H
#define SMARTBUFFER_H

#include <Arduino.h>

class SmartBuffer{
	
	public:
		SmartBuffer();
		SmartBuffer(String key);
		~SmartBuffer();
		void setKey(String key);
		void put(char c);
		boolean full();
		void reset();
		void setFull();
		void set(int index);
		int getIndex();
	
	private:
		String key;
		int index;
};

#endif