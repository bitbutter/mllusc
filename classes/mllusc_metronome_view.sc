MlluscMetronomeView {
	var window, slider, numberbox,metronome,min_bpm,max_bpm, control_spec;
	*new{|w,x,y,m|
    		^super.new.init(w,x,y,m);
	} 
	
	init {|w,x,y,m|
		var val;
		var label;
		window=w;
		metronome=m;
		min_bpm=10;
		max_bpm=200;
		control_spec=ControlSpec.new(min_bpm,max_bpm);
		label=SCStaticText(window,40@40);
		label.stringColor=~button_text_colour;
		label.align=\center;
		label.string = "BPM";
		slider = SmoothSlider(window, 300@40);
		slider.canFocus=false;
		slider.knobColor=~button_text_colour;
		slider.knobSize=0.1;
		slider.background=~statebutton_bg_colour;
		slider.hilightColor=~highlight_1_colour;
		slider.action_({
			var val=control_spec.map(slider.value).round(0.01);
			numberbox.value_(val);
			metronome.set_bpm(val);
// round the float so it will fit in the SCNumberBox
		});
		numberbox = SCNumberBox(window, 60@40);
		numberbox.font = Font("Helvetica", 20);
		numberbox.boxColor=~button_bg_colour;
		numberbox.stringColor=~button_text_colour;
		numberbox.align=\center;
	}
	
	update { arg theChanged, theChanger, more ;
		case 
		{ (more[0] == \tempo_change) && (more[1].notNil) } {slider.value_(control_spec.unmap(more[1].get_bpm()));numberbox.value_(more[1].get_bpm().asStringPrec(3))}
	}

}