MlluscMonomeMuteGroupView {
	var monomes;
	*new{|m|
    		^super.new.init(m);
	} 
	
	init {|m|
		monomes=m;
		this.all_lights_off();
	}
	
	update { arg theChanged, theChanger, more ;
		// From controller
		case 
		{ (more[0] == \group_deactivated) && (more[1].notNil) } {
			this.light_off(more[1]);
		};
		// From button presses
		case 
		{ (more[0] == \monome_button_press)} {
			if(more[2]==0,
			{this.changed(this,[\mute_request,more[1]])})
		}		
	}
	
	light_on{|num|
		monomes.do({|m|
			{m.led(num,0,1)}.defer;
		})
	}
	
	light_off{|num|
		monomes.do({|m|
			{m.led(num,0,0)}.defer;
		})
	}
	
	all_lights_off{
		(0..5).do({|num|
			monomes.do({|m|
				{m.led(num,0,0)}.defer;
			})
		});
	}
	
	
}