/*  this is a class for testing
	TODO: replace this
*/

#ifndef RELAY_TEST
#define RELAY_TEST
#include "arfcDefines.hpp"
#include "dataTypes.hpp"
#include "subSystem.hpp"
#include <iostream>	
#include <vector>
#include <string>
class Command {
public:
	std::string name, val;
	bool hashed;
	int hash;
	Command() {}
	Command(std::string s) {
		int f = s.find(":");
		val  = s.substr(f+1);
		name = s.substr(0,f); 
	}
	int Hash() {
		if( hashed ) return hash;
		hash = name[0];
		for (int i = 1; i < name.length(); ++i)
			hash ^= name[i];
		hashed = true;
		return hash;
	}
	pid_t getValue() {
		try {
			return std::stof(val);
		}
		catch( int e ) {
			return 0;
		}
	}
};

bool valid(std::string s) {
	int f = s.find(":");
	int l = s.length();
	if( l < 3 ) return false;
	return (f < l && f > 0 && f < l-1 );
}

class Relay : public AsyncWorker {
public:
	std::vector<Command> cmds;
	Relay()  {}
	~Relay() {}

	void Update(Motorgroup& motors, PIDctrl* PID ) {
		if(  !cmds.empty() && access.try_lock() ) {
			for (std::vector<Command>::iterator i = cmds.begin(); i != cmds.end(); ++i) {
				std::cout << ">" << (*i).name << std::endl;
				switch( (*i).Hash() ) {
					case 'P'^'P':
						PID->pitch.kp = (*i).getValue();
					break;
					case 'P'^'I':
						PID->pitch.ki = (*i).getValue();
					break;
					case 'P'^'D':
						PID->pitch.kd = (*i).getValue();
					break;
					case 'R'^'P':
						PID->roll.kp = (*i).getValue();
					break;
					case 'R'^'I':
						PID->roll.ki = (*i).getValue();
					break;
					case 'R'^'D':
						PID->roll.kd = (*i).getValue();
					break;
					case 'Y'^'P':
						PID->yaw.kp = (*i).getValue();
					break;
					case 'Y'^'I':
						PID->yaw.ki = (*i).getValue();

					break;
					case 'Y'^'D':
						PID->yaw.kd = (*i).getValue();
					break;
					case 'T':
						motors.All( (*i).getValue()/100.0f );
					break;
				}
			}
			access.unlock();
		}
	}

private:
	virtual void *worker_run() {
		std::cout << "> Now accepting commands on stdin " << std::endl << std::flush;
		while(true) {
			std::string buf;
			std::cin >> buf;
			std::cout << "> Processing " << buf << std::endl << std::flush;
			if( valid(buf) ) {
				access.lock();
				Command c = Command(buf);
				cmds.push_back(c);
				access.unlock();
			}
			if( Disposed() ) return 0;
		}
	}
};
#endif