MlluscToggleButtonView {
	var window;
	var button;
	var label;
	var callback_function;
	var switch_on_callback_function;
	var switch_off_callback_function;
	var current_state;
	*new{|w,l,p,r,con,coff|
    		^super.new.init(w,l,p,r,con,coff);
	} 
	
	init {|w,l,p,r=40,con,coff=nil|
		window=w;
		label=l;
		switch_on_callback_function=con;
		switch_off_callback_function=coff;
		if(p==nil,{p=40@40});
		// draw the button
		button=RoundButton(window, p);
		button.radius=r;
		button.extrude=false;
		button.border=2;
		button.inverse=true;
		button.action=e {
			callback_function.value();
			if(current_state==1,{
				switch_off_callback_function.value();
			},
			{
				switch_on_callback_function.value();
			});
		};
		this.deactivate();
	}

	
	activate {|refresh|
		button.states=[[""++label,~button_text_colour,~highlight_1_colour]];
		if(refresh!=false,{window.refresh});
		current_state=1;
	}
	
	deactivate{|refresh=true|
		button.states=[[""++label,~button_text_colour,~statebutton_bg_colour]];
		if(refresh!=false,{window.refresh});
		current_state=0;
	}
	
}