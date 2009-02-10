MlluscMetronome {
	var ticktask;
	var <>metronome_bus;
	var <hz;
	var <tempo_clock;
	var <loop_length; // recieves this from tempo master
	var level; // level of the tick
	var queuesizearr;
	classvar <synth_defs;
	
	
	*initClass {
		synth_defs = [
				 SynthDef("tick", {arg level=1;
					var u;
					u = Impulse.ar(1);
					FreeSelf.kr(u);
					OffsetOut.ar(0,Impulse.ar(0, 0.0,level));
				})
                ]; 
	}
	
	*init { |server|
                server = server ? Server.default;
                synth_defs.do(_.send(server));
                server.sync;
     } 
	
	*new{|hz,qm|
		^super.new.init(hz);
	} 
	
	init {|h=1,qm=2|
		level=1;
		hz=h;
		queuesizearr=Array.new(20);
		tempo_clock=TempoClock(1);
	}
	
	set_bpm{arg bpm;
		this.set_bps(this.bpm_to_bps(bpm));
	}
	
	set_bps{arg bps;
		tempo_clock.tempo_(bps);
		this.changed(this, [\tempo_change, this]);
		("Set bps to "++bps++", bpm="++this.bps_to_bpm(bps)).postln;
	}
	
	bps_to_bpm{|bps|
		^(bps*60);
	}
	
	bpm_to_bps{|bpm|
		^(bpm/60);
	}
	
	// Seconds per beat
	get_spb{
		^(1/tempo_clock.tempo);
	}
	
	get_bpm{
		^(this.bps_to_bpm(tempo_clock.tempo));
	}
	
	get_bps{
		^(tempo_clock.tempo);
	}
	
	// Seconds needed for x beats
	get_nbeat_duration{|beats|
		^(this.get_spb()*beats);
	}
	
	adjust_bps{arg amount;
		this.set_bps(((tempo_clock.tempo)+amount).max(0));
	}
	
	mute{
		level=0;
	}
	
	unmute{
		level=1;
	}
	
	get_preset_bps{
		^(this.get_bps());
	}
	
	set_preset_bps{|b|
		this.set_bps(b);
	}
	
}
