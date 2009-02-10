MlluscMonomeButtonPresses {
	var <>callback_function;
	*new{|m|
    		^super.new.init(m);
	} 
	
	init {|m|
		m.do({|monome|
			monome.action = { |x, y, on|
				if(on==1,
					{this.changed(this,[\monome_button_press,x,y])},
					{this.changed(this,[\monome_button_release,x,y])}
					
				);
				callback_function.value(x,y,on);
			};
		});
	}
}