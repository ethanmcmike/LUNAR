#include "Timeout.h"

Timeout::Timeout(int duration){
	Timeout::set(duration);
}

Timeout::~Timeout(){}

void Timeout::set(int duration){
	this->duration = duration;
	reset();
}

int Timeout::get(){
	return time - millis();
}

void Timeout::reset(){
	time = millis() + duration;
}

bool Timeout::expired(){
	return Timeout::get() < 0;
}