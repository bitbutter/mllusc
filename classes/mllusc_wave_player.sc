// TODO: code smell! this class does too much, probably should be split.
MlluscWavePlayer {
	var <id;
	var synth;
	var metronome;
	var <current_buffer; // reference to the audio buffer
	var control_bus; // tracks the playhead position
	var <last_file_path; //stores path of last file
	var steps; // number of steps the audio files should split accross
	var status; // keep track of whether its stopped or not
	var next_note_on_func; // store exactly one next note on command
	var level; 
	var next_event;
	var number_of_beats;
	var quantize_ticks_per_beat;
	var osc_path_responder;
	var rate_scale_multiplier;
	var play_event_callback_function;
	var step_change_callback_function;
	var stop_callback_function;
	var waveform_changed_callback_function;
	var storable_attributes_loaded_callback_function;
	var level_set_callback_function;
	var resampler_bus; // summing bus index
	var inherit_wave; 
	var pending_inherit_wave;
	var pending_wave;
	var contains_resampled_audio;
	classvar <synth_defs;
	
	*initClass {
         synth_defs = [
         SynthDef("MlluscWavePlayer", { arg resampler_bus,gate=1,start_frame=0,rate_scale,bufnum,steps,level;
         		var step_change_trigger, stepnum;
			var env=Env.asr(0.005, 0.5, 0.005, 1, 'linear');
			var env_gen=EnvGen.kr(env, gate: gate, doneAction: 2);
			
			// phasor 'playhead'
			var ph = Phasor.ar(
				Impulse.kr(0),
				BufRateScale.kr(bufnum)*rate_scale,
				0,
				BufFrames.kr(bufnum),
				start_frame
			); 
			var br = (BufRd.ar(2, bufnum, ph))*(env_gen);
			var e= EnvGen.ar(Env.perc(0, 0.1));

			stepnum=((steps*((ph).max(0)/BufFrames.kr(bufnum))).floor);
			step_change_trigger=Trig1.ar(HPZ1.ar(stepnum).abs)+Impulse.ar(0);
			SendTrig.ar(step_change_trigger,0,stepnum); // send a OSC trigger each time stepnum changes
			
			OffsetOut.ar([resampler_bus,resampler_bus+1],br*level); // to resampler
			OffsetOut.ar([0,1],br*level);
		}),
		SynthDef("MlluscWavePlayerPitchDrop", { arg resampler_bus,gate=1,start_frame=0,rate_scale,bufnum,steps,level;
         		var step_change_trigger, stepnum;
			var env=Env.linen(0.02, 0.03,0.01, 0.5);
			var env_gen=EnvGen.kr(env, 1, doneAction: 2);
			var pitch_drop_multiplier=XLine.ar( Rand(0.9, 1.9), Rand(0.5, 0.7), 0.1,1,0); 
			// phasor 'playhead'
			var ph = Phasor.ar(
				Impulse.kr(0),
				BufRateScale.kr(bufnum)*(rate_scale*pitch_drop_multiplier),
				0,
				BufFrames.kr(bufnum),
				start_frame
			); 
			var br = (BufRd.ar(2, bufnum, ph))*(env_gen);
			var e= EnvGen.ar(Env.perc(0, 0.1));

			stepnum=((steps*((ph).max(0)/BufFrames.kr(bufnum))).floor);
			step_change_trigger=Trig1.ar(HPZ1.ar(stepnum).abs)+Impulse.ar(0);
			SendTrig.ar(step_change_trigger,0,stepnum); 			
			OffsetOut.ar([resampler_bus,resampler_bus+1],br*level); // to resampler
			OffsetOut.ar([0,1],br*level);
		}),
		SynthDef("MlluscWavePlayerPitchDropReverse", { arg resampler_bus,gate=1,start_frame=0,rate_scale,bufnum,steps,level;
         		var step_change_trigger, stepnum;
			var env=Env.linen(0.03, 0.03,0.01, 0.5);
			var env_gen=EnvGen.kr(env, 1, doneAction: 2);

			// phasor 'playhead'
			var ph = Phasor.ar(
				Impulse.kr(0),
				(BufRateScale.kr(bufnum)*(rate_scale*Rand(0.9,1.9)))*(-1),
				start_frame+2000,
				start_frame-6000,
				start_frame+2000
			); 
			var br = (BufRd.ar(2, bufnum, ph)*(env_gen));

			stepnum=((steps*((ph).max(0)/BufFrames.kr(bufnum))).floor);
			step_change_trigger=Trig1.ar(HPZ1.ar(stepnum).abs)+Impulse.ar(0);
			SendTrig.ar(step_change_trigger,0,stepnum); 			
			OffsetOut.ar([resampler_bus,resampler_bus+1],br*level); // to resampler
			OffsetOut.ar([0,1],br*level);
		});
		];
	}
	
	*init { |server|
     	server = server ? Server.default;
         synth_defs.do(_.send(server));
         server.sync;
     } 
	
	*new{ |num,m,stps=8,o|
    		^super.new.init(num,m,stps,o);
	} 
	
	init {|num,m,stps,o|
		contains_resampled_audio=false;
		id=num;
		metronome=m;
		steps=stps;
		next_note_on_func=nil;
		level=1;
		quantize_ticks_per_beat=4; // 4 ticks p/b == 16ths
		rate_scale_multiplier=1;
		status="stopped";
		resampler_bus=o;
		inherit_wave=0;
		metronome.tempo_clock.sched(metronome.tempo_clock.timeToNextBeat(1),
		{this.on_every_quantize_interval();1/quantize_ticks_per_beat});
	}
	
	get_id{
		^id;
	}
	get_inherit_wave{
		^inherit_wave;
	}
	set_play_event_callback_function{|c|
		play_event_callback_function=c;
	}
	
	set_storable_attributes_loaded_callback_function{|c|
		storable_attributes_loaded_callback_function=c;
	}

	load_sound {arg path, tempo_master;
		this.stop();
		current_buffer.free;
		contains_resampled_audio=false;
		Routine.new({Server.default.sync;
		current_buffer=Buffer.read(Server.default,path, action: { arg buffer;
			number_of_beats=this.get_number_of_beats(); // eg. 8 beats if the loop is 2 bars long
			last_file_path=path;
			if (tempo_master==1,
				{this.match_tempo_to_file();}
			);			
			this.recalculate_waveform();
		});}).play;
	}
	
	clear_sound {
		current_buffer.free;
		last_file_path=nil;
		this.recalculate_waveform();
	}
	
	recalculate_waveform {
		if((last_file_path==nil) && (contains_resampled_audio==false),
			{
			 	waveform_changed_callback_function.value([0]);
			},
			{
				current_buffer.loadToFloatArray(action: { arg array;
					waveform_changed_callback_function.value(array.as(Array));
     			});
     		}
     	);
	}
	
	use_this_buffer{|b|
		current_buffer=b;
		number_of_beats=this.get_number_of_beats();
		metronome.tempo_clock.sched(metronome.tempo_clock.timeToNextBeat(1),
		{this.recalculate_waveform();nil});
	}
	
	set_resampled_audio_flag{|b|
		contains_resampled_audio=b;
	}
	
	set_quantize_ticks_per_beat{|qtpb|
		quantize_ticks_per_beat=qtpb;
	}
	
	match_tempo_to_file{
		if ((current_buffer.numFrames.isNil) || (current_buffer.sampleRate.isNil),
		{^false});
		metronome.set_bps(number_of_beats/this.get_buffer_duration());	}
		
	// Start a new synth voice, making sure that any old ones are stopped first
	play_this_event {arg event;
		
		var synth_name="MlluscWavePlayer";
		if (this.check_sound_is_loaded()==false,{"returning false, no sound loaded".postln; ^false}); // skip if no sound is loaded
		if (event.should_replace_event(next_event),{},{"skipping, priority too low";^false}); // skip if new event is of a lower priority than one that's already queued
		synth_name="MlluscWavePlayer";
		case
		{event.synth==\pitch_drop} {synth_name="MlluscWavePlayerPitchDrop"}
		{event.synth==\pitch_drop_reverse} {synth_name="MlluscWavePlayerPitchDropReverse"};
		
		next_event=event;
		status="queued_for_playing";
		next_note_on_func={
			var nodeID,triggerID, commandpath, response;
			status="playing";
			synth.set("gate", 0);
			synth=Synth(synth_name, [
			\resampler_bus,resampler_bus,
			\bufnum,current_buffer.bufnum,
			\gate,1,
			\start_frame,this.step_number_to_frame(next_event.step),
			\rate_scale,this.get_rate_scale,
			\steps,steps,
			\level,level],Server.default,\addToHead);
			next_event=nil; // clear the queued event
			play_event_callback_function.value(); // perform the play event callback func
			nodeID = synth.nodeID;
			triggerID = 0;
			commandpath = ['/tr', nodeID, triggerID]; // listen to only the synth that's just been created
			response = { arg time, responder, message; this.broadcast_slice_number(message[3]) };
			osc_path_responder = OSCpathResponder(Server.default.addr, commandpath, response);
			osc_path_responder.add;
		};
		if (event.quantize==false,
		{this.on_every_quantize_interval()});
	}
	
	stop{
		synth.set("gate", 0);
		status="stopped";
		next_note_on_func=nil;
		stop_callback_function.value();
	}
	
	mute{
		this.stop();
	}
	
	change_octave{|updown=1|
		if(updown==1,
		{rate_scale_multiplier=rate_scale_multiplier*2},
		{rate_scale_multiplier=rate_scale_multiplier/2});
		this.force_rate_scale_update();
	}
	
	set_length_in_beats{|beats|
		rate_scale_multiplier=this.get_buffer_duration()/metronome.get_nbeat_duration(beats);
	}
	
	get_status{
		^(status);
	}
	
	set_step_change_callback_function{|f|
		step_change_callback_function=f;
	}
	
	set_stop_callback_function{|f|
		stop_callback_function=f;
	}
	
	set_waveform_changed_callback_function{|f|
		waveform_changed_callback_function=f;
	}
	
	set_level_set_callback_function{|f|
		level_set_callback_function=f;
	}
	
	set_level{arg l;
		level=l;
		synth.set([\level,level]);
	}
	
	get_level{
		^level;
	}
	
	get_preset_wave{
		if(inherit_wave==true,{^("**inherit**")});
		^last_file_path;
	}
	
	set_preset_wave{|p|
		pending_wave=p;
	}
	
	set_inherit_wave{|b| 
		inherit_wave=b;
		
	}
	
	get_preset_inherit_wave{
		^(inherit_wave);
	}
	set_preset_inherit_wave{|b|
		pending_inherit_wave=b;
	}
	
	get_preset_rate_scale_multiplier{
		^(rate_scale_multiplier)
	}
	
	set_preset_rate_scale_multiplier{|m|
		rate_scale_multiplier=m;
		this.force_rate_scale_update();
	}
	
	set_preset_level{|l|
		this.set_level(l);
		level_set_callback_function.value(this);
		^true;
	}
	
	get_preset_level{
		^level
	}
	
	storable_attributes_loaded{
		if (pending_inherit_wave==0,
		{
			if(pending_wave.notNil,
				{this.load_sound(pending_wave)},
				{this.clear_sound;}
			);
		});
		this.set_inherit_wave(pending_inherit_wave);
		pending_inherit_wave=nil;
		pending_wave=nil;
		storable_attributes_loaded_callback_function.value(this);
	}
	
	// Private --------------------------------------------------------
	
	on_every_quantize_interval{
		if(next_note_on_func.notNil,
		{
			Server.default.makeBundle(0.03,
				next_note_on_func;
			);
			next_note_on_func=nil;
		});
	}
		
	update { arg theChanged, theChanger, more ;
		case 
		{ (more[0] == \tempo_change)} {
			this.force_rate_scale_update();
		}
	}
	
	check_sound_is_loaded {
		if (last_file_path.notNil || contains_resampled_audio==true,
		{^true},{^false});
	}
		
	quantized_stop{
		next_note_on_func={
			this.stop();
		}
	}
	
	step_number_to_frame {arg step_number=0;
		if (step_number==0,
		{^0},
		{
			if ((step_number.notNil) && (current_buffer.numFrames.notNil),
				{^(step_number*((current_buffer.numFrames)/steps))}
			)
		});
	}
	
	broadcast_slice_number{|slice|
		if (status!="playing",
		{^false}); 
		step_change_callback_function.value(slice.min(steps-1));
	}
	
	fraction_to_step_number {arg fraction;
		^((steps*fraction).floor); // returns a whole number between 0 and steps
	}
	
	// Used to change the rate scale for voices that are already playing
	force_rate_scale_update{
		if(status!="playing",
		{^false});
		synth.set([\rate_scale,this.get_rate_scale()]);
	}
	
	get_rate_scale{
		^((this.get_buffer_duration()/(metronome.get_nbeat_duration(number_of_beats))))*rate_scale_multiplier;
	}

	get_buffer_duration{
		^(current_buffer.numFrames/current_buffer.sampleRate);
	}
	
	// Tries to determine the number fo beats from the filename, or defaults to 8.
	get_number_of_beats{
		var arr;
		if(last_file_path.isNil,
		{^8});
		arr=last_file_path.split($.);
		if (arr.size!=3,
			{^8},
			{^(arr[1].asFloat)}
		)
	}
}