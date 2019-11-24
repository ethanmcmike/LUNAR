#include "ParserC.h"

ParserC::ParserC(String key, int size, char del, int* slotSize, void(*handle)(char**, int, int*)){
	
	this->key = key;
	rcvBuffer.setKey(key);
	tempBuffer.setKey(key);
	this->size = size;
	this->del = del;
	this->slotSize = slotSize;
	this->handle = handle;
	
	buffer = new char*[size];
	
	sizes = new int[size];

	for(int i=0; i<size; i++){
		
		sizes[i] = 0;

		buffer[i] = new char[slotSize[i]];
    
		for(int j=0; j<slotSize[i]; j++){
		  ((char*)buffer[i])[j] = 'v';
		}
	}
	
	delCount = 0;
	index = 0;
}

ParserC::~ParserC(){
	delete[] slotSize;
	delete[] buffer;
}

void ParserC::test(){
	
	
	/*
	
	Serial.println("TEST");
	
	
	for(int i=0; i<size; i++){

		char* t = buffer[i];
    
		for(int j=0; j<slotSize[i]; j++){
			Serial.println(t[j]);
		}
	}
	*/
}

void ParserC::put(char c){
	
	/*
	
	Serial.println("Char");
	Serial.println(c);
	
	index++;
	
	Serial.println("Data");
	for(int i=0; i<size; i++){

		char* t = buffer[i];
    
		for(int j=0; j<slotSize[i]; j++){
			Serial.println(t[j]);
		}
	}
	
	*/
	
	//Serial.println(c);
	
	//Fill key
	if(!rcvBuffer.full()){
		rcvBuffer.put(c);
	}

	//Fill data
	else{

		//Serial.println(c);
	
		tempBuffer.put(c);

		//Reset because of new key found
		if(tempBuffer.full()){
			reset();
			rcvBuffer.setFull();
			tempBuffer.reset();
			return;
		}
		
		//Move to next slot
		if(c == del){
			
			sizes[delCount] = index;
			delCount++;
			index = 0;
			
			//Serial.print("DelCount: ");
			//Serial.println(delCount);
		}
		
		//Not a delimeter
		else{

			//Add to slot
			char* chunk = buffer[delCount];
			chunk[index] = c;
			index++;
			
			//End of last slot
			if(delCount == size-1 && sizes[delCount] >= sizes[delCount]){
				
				//Serial.println("End of slot");
			
				
				if(tempBuffer.getIndex() > 0){
					reset();
					rcvBuffer.set(tempBuffer.getIndex());
					tempBuffer.reset();
					return;
				}
			
				handle(buffer, size, sizes);
				reset();
				tempBuffer.reset();
				
			}

			//Exceeded slot size
			if(index > slotSize[delCount]){
				  
				if(tempBuffer.getIndex() > 0){
					reset();
					rcvBuffer.set(tempBuffer.getIndex());
					tempBuffer.reset();
					return;
				}
				  
				reset();
				rcvBuffer.set(tempBuffer.getIndex());
			}
		}
	}
}

void ParserC::reset(){
	
	//Serial.println("Reset");
	
	//memset(buffer, 0, size);

	delCount = 0;
	index = 0;
	
	for(int i=0; i<size; i++){
		sizes[i] = 0;
	}

	rcvBuffer.reset();
}