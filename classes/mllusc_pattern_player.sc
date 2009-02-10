MlluscPatternPlayer{
var <list;
var metronome;
var <status;
var <step_index;
var quantize_ticks_per_beat;
var pattern_recorder;
var <>play_callback_function;
var <>stop_callback_function;
var <>looping_to_the_beginning_callback_function;
var on_every_quantize_interval_func;
*new{|m|
    		^super.new.init(m);
	} 
	
	init {|m|
		metronome=m;
		quantize_ticks_per_beat=4; // 4 ticks p/b == 16ths
		status=\stopped;
		on_every_quantize_interval_func={};
		// start on half beats, 280degrees out of phase with event command evaluation
		metronome.tempo_clock.sched(metronome.tempo_clock.timeToNextBeat(1), 
		{
			this.on_every_quantize_interval_func_wrapper();
			1/quantize_ticks_per_beat;
		});
	}
	
	play {|a|
		"got play command".postln;
		if(a.notNil,{this.set_event_list(a)});
		step_index=nil; // nil value will get set to 0 on the first iteration of on_every_quantize_interval
		status=\playing;
		play_callback_function.value();
		this.set_on_every_quantize_interval_func();
	}
	
	stop {
		"got stop command".postln;
		status=\stopped;
		on_every_quantize_interval_func={};
		stop_callback_function.value();
	}
	
	set_event_list{|l|
		list=l;
	}
	
	get_status{
		^status;
	}
	
	merge_list_with{|other_player|
		var tobeadded;
		var new_list=this.get_synched_list_from_other_player(other_player);
		list.do({|it,in|
			if(new_list[in].isNil,
			{tobeadded=[]},
			{tobeadded=new_list[in]});
			it.addAll(tobeadded);
		});list
	}
	
	get_synched_list_from_other_player{|other_player|
		var diff=step_index-other_player.step_index;
		^other_player.list.rotate(diff);
	}
	
	on_every_quantize_interval_func_wrapper{
		on_every_quantize_interval_func.value();
	}
	set_on_every_quantize_interval_func{
		on_every_quantize_interval_func={
			if (status==\playing,{
		
			// queue the next slot
			if (step_index.isNil,
				{this.reset_pointer()},
				{
					if(this.should_loop_back(),
						{
							this.reset_pointer();
							looping_to_the_beginning_callback_function.value();
						},
						{
							this.advance_to_next_slot();
						}
					);
				}
			);
		
			this.send_events_from_current_slot();
		
			});
		}
		
	}
	
	should_loop_back{
		^(step_index==((list.size)-1));
	}
	
	advance_to_next_slot{
		step_index=step_index+1;
	}
	
	reset_pointer{
		step_index=0;
	}
	
	send_events_from_current_slot{
		list[step_index].do({|e|
			this.changed(this,[\event,e])
		})
	}
	
	get_number_of_events_stored{
		^(list.flat.size);
	}
}