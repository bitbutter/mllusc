MlluscMonomeRowView {
	var row_number;
	var monomes;
	*new{arg offset=0, monome_array;
    		^super.new.init(offset, monome_array);
	} 
	
	init {arg offset, monome_array;
		row_number=offset;
		monomes=monome_array;
		this.all_lights_off();
	}
	
	update { arg theChanged, theChanger, more ;
		case 
		{ (more[0] == \looper_step_change) && (more[1].notNil) } {this.single_light_on(more[1])}
		{ (more[0] == \stop_event) && (more[1].notNil) } {("MlluscMonomeRowView turning all lights off for row "++theChanger.id).postln;this.all_lights_off()};
	}
	
	single_light_on {arg step;
		//this.light_index_to_binary_value.postln;
		monomes.do({|m|
			m.led_row(row_number,this.light_index_to_binary_value(step));
		})
	}
	
	all_lights_off {
		monomes.do({|m|
			{m.led_row(row_number,0)}.defer;
		})
	}
	
	light_index_to_binary_value{|index|
		var values=[1,2,4,8,16,32,64,128];
		^(values[index]);
	}
}