#ifndef TIMEOUT_H
#define TIMEOUT_H

#include <Arduino.h>

class Timeout{
	
	public:
		Timeout(int duration);
		~Timeout();
		void set(int duration);
		int get();
		bool expired();
		void reset();
	
	private:
		long time;
		int duration;
};

#endif