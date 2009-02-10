// listen to presses, and generate events
MlluscEventGenerator{
var player_modifier_dict;
var current_player_modifier;
*new{
    		^super.new.init();
	} 
	
	init {
		player_modifier_dict=Dictionary.new;
		player_modifier_dict.add(0 -> \pitch_drop);
		player_modifier_dict.add(1 -> \time_stretch);
		player_modifier_dict.add(2 -> \scratch);
	}
	
	update { arg theChanged, theChanger, more;
		case
		{this.is_a_player_modifier_button(more)} {
			if(more[0] == \monome_button_press,
				{current_player_modifier=more[1]},
				{if (current_player_modifier==more[1],
					{current_player_modifier=nil;})
				;}
			);
		}
		{this.is_a_row_button(more)} {
			if(more[0] == \monome_button_press,
				{
					if (this.get_current_player_modifier()==\pitch_drop,
					{
						this.generate_event(more,\pitch_drop_reverse,false)},{this.generate_event(more)}
					);
				},
				{// its a release, do we need to send an event?
					"its a release".postln;
					("current_player_modifier="++current_player_modifier).postln;
					if(this.get_current_player_modifier()==\pitch_drop,
					{
						"generating reverse thing".postln;this.generate_event(more,\pitch_drop,false)
					});
				}
			);
		}
	}
	
	generate_event{|more,mod,q=true|
		var e,the_mod;
		if(mod.isNil,{the_mod=this.get_current_player_modifier()},{the_mod=mod});
		e=MlluscEvent.new(more[2]-1,more[1],the_mod,2,q);//wave_player_id,step,synth
		this.changed(this,[\event,e])
	}
	
	is_a_player_modifier_button{|more|
		^((more[2])==6);
	}
	is_a_row_button{|more|
		^(((more[2])>=1)&&((more[2])<=5));
	}
	get_current_player_modifier{
		if(current_player_modifier.notNil,
		{^(this.get_dict_value(current_player_modifier))},
		{^(\regular)}
		);
	}
	
	get_dict_value{|i|
		^(player_modifier_dict.matchAt(i));
	}
}