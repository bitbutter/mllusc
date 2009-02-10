// display radio button group for selecting which mute group a thing belongs to
MlluscRadioButtonView {
	var window;
	var buttons;
	var id;
	var callback_function;
	*new{|i,w,x,y,n,c|
    		^super.new.init(i,w,x,y,n,c);
	} 
	
	init {|i,w,x,y,n,c|
		id=i;
		window=w;
		callback_function=c;
		// draw the buttons
		n.do({|num,i|
			var new_butt=MlluscToggleButtonView.new(window,num.asString,40@40,8,
			e {
				callback_function.value(num);
			});
			buttons=buttons.add(new_butt);
		});
		this.deactivate_all();
	}

	
	activate {|index|
		this.deactivate_all();
		buttons[index].activate(false);
		window.refresh;
	}
	
	deactivate_all {
		buttons.do({|b,i|
			b.deactivate(false);
		});
		window.refresh;
	}
	
}