// Responsibilities:
// gets messages from row controller, and updates interface elements
// gets messages from interface elements and notifies controller
MlluscWavePlayerRowView {
	var window;
	var id;
	var y_pos;
	var <radio_button_view;
	var use_as_tempo_button;
	var wave_player;
	var next_x_coords=0;
	var callback_for_radio_buttons;
	var level_slider;
	var waveform_view;
	var inherit_button;
	*new{|i,w,c,wp|
		^super.new.init(i,w,c,wp);
	} 
	
	init {|i,w,c,wp|
		var load_sample_button, octave_down_button, octave_up_button;
		id=i;
		window=w;
		y_pos=i*20;
		wave_player=wp;
		callback_for_radio_buttons=c;
		
		// Draw the interface elements for the row
		radio_button_view=MlluscRadioButtonView.new(id,window,this.get_next_x_coord(5*20),y_pos,5,callback_for_radio_buttons);
		
		load_sample_button=RoundButton(window, 80@40);
		load_sample_button.border=2;
		load_sample_button.inverse=true;
		load_sample_button.extrude=false;
		load_sample_button.action={
			File.openDialog("", {|path|
				this.changed(this, [\file_load_request, path])
				}) ;
		};
		load_sample_button.states=[["Choose",~button_text_colour,~button_bg_colour]];
		
		waveform_view=GUI.soundFileView.new(window, 150@40);
		waveform_view.waveColors=[~button_text_colour];
		waveform_view.gridColor = ~highlight_1_colour;
		waveform_view.background=~highlight_1_colour_dark;
		waveform_view.gridResolution = 100;
		
		octave_down_button=RoundButton(window, 40@40);
		~format_button.value(octave_down_button);
 		octave_down_button.states=[["-",~button_text_colour,~button_bg_colour]];
		octave_down_button.action={
			wave_player.change_octave(0);
		};
		
		octave_up_button=RoundButton(window, 40@40);
		~format_button.value(octave_up_button);
 		octave_up_button.states=[["+",~button_text_colour,~button_bg_colour]];
		octave_up_button.action={
			wave_player.change_octave(1);
		};
		
		level_slider = SmoothSlider(window, 60@40);
		level_slider.canFocus=false; // looks better without the focus rectangle
		level_slider.knobColor=~button_text_colour;
		level_slider.knobSize=0.1;
		level_slider.background=~statebutton_bg_colour;
		level_slider.hilightColor=~highlight_1_colour;
		level_slider.action_({
		(level_slider.value).postln;
			wave_player.set_level(level_slider.value);
		});
		
		use_as_tempo_button=RoundButton(window, 80@40);
		~format_rounded_button.value(use_as_tempo_button);
 		use_as_tempo_button.states=[["Get tempo",~button_text_colour,~button_bg_colour]];
		use_as_tempo_button.action={
			wave_player.match_tempo_to_file();
		};
		
		inherit_button=MlluscToggleButtonView.new(window,"Inherit",80@40,20,
			e {
				// button on func
				wave_player.set_inherit_wave(1);
				inherit_button.activate();
			},
			e{
				// button off func
				wave_player.set_inherit_wave(0);
				inherit_button.deactivate();
			});		
		
		// Listeners
		radio_button_view.addDependant(this);
	}
	
	get_next_rectangle{|width|
		^(Rect(this.get_next_x_coord(width), y_pos, width, 20));
	}

	get_next_x_coord{|width|
		var old_x=next_x_coords;
		var spacing=5;
		next_x_coords=next_x_coords+width+spacing;
		^old_x;
	}
	
	update { arg theChanged, theChanger, more;
		case 
		{ (more[0] == \radio_button_clicked) } {
			this.changed(this, [\radio_button_clicked, more[1]]);		}
		{ (more[0] == \added_to_group) } {
			this.activate_radio_button(more[2]);
		}
	}
	
	activate_radio_button{|r|
		radio_button_view.activate(r);
	}
	
	move_level_slider{|l|
		level_slider.value=l;
	}
	
	redraw_waveform{|a|
		{
			if (a.size==0,{waveform_view.setData([0]);},
			{
	       	  	waveform_view.data = a;
	        		waveform_view.xZoom = a.size / 150;
        	 	});
        	 	waveform_view.refresh;
         }.defer;
	}
	
	set_inherit_button{|b|
		if(b==1,
		{inherit_button.activate()},
		{inherit_button.deactivate()});
	}

}