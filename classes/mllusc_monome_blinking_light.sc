MlluscMonomeBlinkingLight {
	var monomes;
	var rate;
	var x;
	var y;
	var blink_routine;
	
	*new{|m,xp,yp|
    		^super.new.init(m,xp,yp);
	} 
	
	init {|m,xp,yp|
		monomes=m;
		x=xp;
		y=yp;
		blink_routine = Task({
			inf.do({
				this.light_on();
				rate.yield;
				this.light_off();
				rate.yield;
			});
		});
	}
	
	stop{
		this.blink_routine.stop();
	}
	
	on{
		blink_routine.stop;
		this.light_on();
	}
	
	blink{|r=0.1|
		blink_routine.stop;
		rate=r;
		blink_routine.play(SystemClock);
	}
	
	off{
		blink_routine.stop;
		this.light_off();
	}
	//-----
	light_on{
		monomes.do({|m|
			{m.led(x,y,1)}.defer;
		})
	}
	set_rate{|r|
		rate=r;
	}
	light_off{
		monomes.do({|m|
			{m.led(x,y,0)}.defer;
		})
	}
}