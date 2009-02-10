MlluscEvent{
var <synth;
var <step;
var <wave_player_id;
var <priority;
var <quantize;
*new{|wid,st,sy,p,q|
    		^super.new.init(wid,st,sy,p,q);
	} 
	
	init {|wid,st,sy,p,q=true|
		wave_player_id=wid;
		step=st;
		synth=sy;
		priority=p; // low pririty events are more likely to be overwritted
		quantize=q;
	}
	
	should_replace_event{|e|
		if(e.isNil,{^true});
		^(this.dump;e.dump;priority>=e.priority); // replace rival event if this priority is greater or equal to the other event's priority
	}
	
	set_low_priority{
		priority=1;
	}
	
}