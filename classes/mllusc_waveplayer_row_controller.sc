// Responsibilities:
// gets messages from row view, and sends instructions to models
// gets messages from models and sends instructions to row view
MlluscWavePlayerRowController {
	var id;
	var wave_player;
	var metronome;
	var muter;
	var monomes;
	var waveplayer_row_view;
	var monome_mute_group_view;
		
	*new{|i,w,mtrnm,mtr,mnms,wrv,mmgv|
		^super.new.init(i,w,mtrnm,mtr,mnms,wrv,mmgv);
	} 
	
	init {|i,w,mtrnm,mtr,mnms,wrv,mmgv|
		waveplayer_row_view=wrv;
		id=i;
		monomes=mnms;
		metronome=mtrnm; // contains master tempo_clock
		wave_player=w;
		monome_mute_group_view=mmgv;
		
		muter=mtr; // manages mute groups, decorates objecs to be muteable
	}
	
	update { arg theChanged, theChanger, more;

		// From view
		case 
		{ (more[0] == \event) } {
			if(more[1].wave_player_id==(id),
			{wave_player.play_this_event(more[1])});
			^true;
		}
		{ (more[0] == \file_load_request) } {
			"controller received file_load_request message".postln;
			wave_player.load_sound(more[1]);
			^true;
		};
	}
}