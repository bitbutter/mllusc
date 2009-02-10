MlluscMonomeLedGrid {
	var monomes;
	var width;
	var height;
	var led_array;
	
	*new{|m,w,h|
    		^super.new.init(m,w,h);
	} 
	
	init {|m,w,h|
		monomes=m;
		width=w;
		height=h;
		led_array=Array.fill2D(width, height, {|c, r|MlluscMonomeBlinkingLight.new(monomes,c,r).off()});
		
	}
	
	get_led_array{
		^led_array;
	}
	
	on{|x,y|
		led_array[x][y].on();
	}
	
	off{|x,y|
		led_array[x][y].off();
	}
	
	blink{|x,y,rate=0.1|
		led_array[x][y].blink(rate);
	}
}