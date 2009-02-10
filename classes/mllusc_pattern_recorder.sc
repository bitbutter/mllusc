MlluscPatternRecorder{
var <list;
var metronome;
var <status;
var step_index;
var quantize_ticks_per_beat;
var length_in_beats;
var length_in_quantized_events;
var <>armed_callback_function;
var <>recording_callback_function;
var <>recording_complete_callback_function;
var <>stop_callback_function;
*new{|m|
    		^super.new.init(m);
	} 
	
	init {|m|
		metronome=m;
		quantize_ticks_per_beat=4; // 4 ticks p/b == 16ths
		status=\stopped;
		length_in_beats=8;
		length_in_quantized_events=length_in_beats*quantize_ticks_per_beat;
		metronome.tempo_clock.sched(metronome.tempo_clock.timeToNextBeat(1),
		{this.on_every_quantize_interval();1/quantize_ticks_per_beat});
	}
	
	arm_for_recording {
		// recording will start when the next row event is received
		status=\armed;
		armed_callback_function.value();
	}
	
	stop{
		status=\stopped;
		stop_callback_function.value();
	}
		
	update { arg theChanged, theChanger, more ;
		case 
		{ (more[0] == \event)} {
			if (status==\stopped,{^false});
			if (status==\armed,{this.setup_new_recording()});
			this.store_event(more[1]);
		}
	}
	
	on_every_quantize_interval{
		if (status!=\recording,{^false}); // skip if we're not recording
		
		if(this.list_complete(),{
			recording_complete_callback_function.value();
			status=\stopped;
			this.changed(this,[\finished_recording]);
		},
		{
			this.advance_to_next_slot();
		});
	}
	
	store_event{|event|
		event.set_low_priority();
		list[list.size-1].add(event);
		("*** in store event, list size is now "++list.size).postln;
	}
	
	get_number_of_events_stored{
		var count=0;
		("inget_number_of_events_stored, list size reports as "++list.size).postln;
		list.do({|i1|
			i1.do({|i2|
				count=count+1;
			})
		})
		^count;
	}
	
	setup_new_recording{
		status=\recording;
		list=List.new(); // initialise empty 2d list	
		this.advance_to_next_slot();
		this.changed(this,[\started_recording]);
		recording_callback_function.value();
	}
	
	// create a new slot ready to store events
	advance_to_next_slot{
		list.add(List.new());
		("*** advance to next slot modified list size, it's now "++list.size).postln;
	}
	
	list_complete{
		^(list.size==length_in_quantized_events);
	}
}