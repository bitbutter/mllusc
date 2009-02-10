// Contains a pattern player and a pattern recorder
MlluscPatternUnit{
var index;
var <>player;
var metronome;
var <>recorder;
var <status; // stopped, armed, recording, playing
var <>armed_callback_function;
var <>started_recording_callback_function;
var <>play_callback_function;
var <>stop_callback_function;
*new{|i,m|
    		^super.new.init(i,m);
	} 
	
	init {|i,m|
		index=i;
		metronome=m;
		player=MlluscPatternPlayer.new(metronome);
		recorder=MlluscPatternRecorder.new(metronome);
		
		recorder.addDependant(this);
		status=\stopped;
	}
	
	// the button associated with this unit has been pressed, now figure out what to do about that
	do_it{
		status.switch(
			\stopped, {recorder.arm_for_recording();status=\armed;},
			\armed, {recorder.stop();status=\stopped;},
			\recording, {recorder.arm_for_recording();status=\armed;},
			\playing, {player.stop();status=\stopped;}
		);
	}
	
	stop{
		recorder.stop();
		player.stop();
		status=\stopped;
	}
	
	update { arg theChanged, theChanger, more ;
		case 
		{ (more[0] == \started_recording)} {
			status=\recording;
			started_recording_callback_function.value();
		}
		{ (more[0] == \finished_recording)} {
			status=\playing;
			play_callback_function.value();
		}
	}
}