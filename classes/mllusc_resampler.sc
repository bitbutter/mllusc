MlluscResampler{
var <resampler_synth, <buffer, metronome, bus_to_record_from, out_bus, on_next_quantize_interval_function, resampling_done_callback_function, <status, response_func, length_in_beats;
classvar <synth_defs;

	*initClass {
         synth_defs = [
         SynthDef("MlluscResampler", { arg record_to_buffer,bus_to_record_from;
              var phasor=Phasor.ar(0, BufRateScale.kr(record_to_buffer), 0, BufFrames.kr(record_to_buffer));
              var started_recording=BinaryOpUGen('>', phasor, 0);
              var started_recording_latch=Latch.ar(started_recording,started_recording);
         		var phasor_is_zero=BinaryOpUGen('==', phasor, 0);
         		var recording_done=if(started_recording_latch>0, phasor_is_zero>0, 0);
         		SendTrig.ar(recording_done,0,666); // Send a message when the pointer reached the end of the buffer
         		FreeSelf.kr(recording_done); // free self when the recording is done
         		BufWr.ar(In.ar(bus_to_record_from,2), record_to_buffer, phasor, 0); // Record from bus_to_record_from
         	})];
	}
	
	*init { |server|
     	server = server ? Server.default;
         synth_defs.do(_.send(server));
         server.sync;
     }	


	*new{|btrf,out,m|
    		^super.new.init(btrf,out,m);
	} 
	
	init {|btrf,out,m|
		var quantize_ticks_per_beat, response;
		bus_to_record_from=btrf; out_bus=out; metronome=m; status=\stopped;
		quantize_ticks_per_beat=4; // 4 ticks p/b == 16ths
		metronome.tempo_clock.sched(
			metronome.tempo_clock.timeToNextBeat(1),
			{
				this.on_every_quantize_interval();
				1/quantize_ticks_per_beat
			}
		);
		metronome.addDependant(this);
	}
	
	get_buffer_copy{
		var new_buf=this.set_up_buffer();
		buffer.copyData(new_buf);
		^(new_buf);
	}
	
	set_resampling_done_callback_function{|f|
		resampling_done_callback_function=f;
	}
	
	start_resampling{
	"** resampler start_resampling".postln;
	buffer=this.set_up_buffer();
		on_next_quantize_interval_function= {
			var nodeID, triggerID, commandpath, osc_path_responder;
			"** resampler executing on_next_quantize_interval_function".postln;
			status=\resampling;
			resampler_synth=Synth.tail(
				Server.default,
				"MlluscResampler",
				[\record_to_buffer,buffer,\bus_to_record_from,bus_to_record_from]
			);
			response_func = { arg time, responder, message;
				status=\stopped;("time="++time++" responder="++responder++" message="++message).postln;
				this.resampling_done()
			};
			// listen to only to the resampler_synth synth's messages
			osc_path_responder = OSCpathResponder(
				Server.default.addr,
				['/tr', resampler_synth.nodeID, 0],
				response_func
			);
			osc_path_responder.add;
		}
	}
	
	get_buffer_duration{
		^(buffer.duration);
	}
	
// Private -------------------------------
	
	on_every_quantize_interval{
	if(on_next_quantize_interval_function.notNil,
		{
			Server.default.makeBundle(0.03,
				on_next_quantize_interval_function.value();
				on_next_quantize_interval_function=nil;
			);
		});
	}
	
	// TODO: resampled audio plays back more quietly than 'direct' audio, why?
	resampling_done{
		var copy=this.get_buffer_copy();
		resampling_done_callback_function.value(buffer,length_in_beats);
		resampler_synth.free;
	}
	
	set_up_buffer{|len=8|
		var target_length;
		length_in_beats=len;
		target_length=length_in_beats*metronome.get_spb();
		^(Buffer.alloc(Server.default, Server.default.sampleRate * target_length, 2));
	}
	
	update { arg theChanged, theChanger, more;
		case
		{more[0] == \tempo_change} {buffer=this.set_up_buffer()};
	}
}