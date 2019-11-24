#include "SmartBuffer.h"

SmartBuffer::SmartBuffer(){

}

SmartBuffer::SmartBuffer(String key){
	this->key = key;
}

SmartBuffer::~SmartBuffer(){}

void SmartBuffer::setKey(String key){
	this->key = key;
}

void SmartBuffer::put(char c){
	
	//Already full
	if(index == key.length()){
		return;
	}
	
	//Middle of key
	else if(c == key[index]){
		index++;
	}
	
	//Start of key
	else if(c == key[0]){
		index = 1;
	}
	
	//Incorrect character
	else {
		reset();
	}
}

boolean SmartBuffer::full(){
	return index >= key.length();
}

void SmartBuffer::reset(){
	index = 0;
}

void SmartBuffer::setFull(){
	index = key.length();
}

int SmartBuffer::getIndex(){
	return index;
}

void SmartBuffer::set(int index){
	int size = key.length();
	this->index = (index > size) ? size : index;
}