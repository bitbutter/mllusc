//-----------------------------------------------------------------
//
// Clean interactions with the Monome 40h.
//  __ Daniel Jones
//     http://www.erase.net
//
// Run serialio with:
//    ./serialio localhost:57120
//
// Usage:
//    m = Monome.new(host, port);
//    m = Monome.new;
//    m.action = { |x, y, on|
//      [x, y, on].postln;
//    };
//    m.led(5, 6, 1);
//    m.led_row(4, 255);
//    m.intensity(0.5);
//    m.clear;
//
//-----------------------------------------------------------------

Monome
{
	var <host, <port;
	var <>osc, <>target;
	var <>action;
	
	var <pressed;

	*new { |host = "127.0.0.1", port = 8080|
		^super.newCopyArgs(host, port).init;
	}
	
	*emu { |port = 57120|
		// spawn emulator
		MonomEm.new(port: port);
		^this.new(port: port).init;
	}
	
	init {
		pressed = [];
		8.do({ pressed = pressed.add(Array.fill(8, 0)) });
		
		osc = OSCresponder.new(nil, "/box/press", { |time, resp, msg|
			pressed[msg[2]][msg[1]] = msg[3];
			if (action.notNil)
			   { action.value(msg[1], msg[2], msg[3]); };
		});
		osc.add;

		target = NetAddr(host, port);
	}

	led { |x, y, on = 1|
		target.sendMsg("/box/led", x.asInteger, y.asInteger, on.asInteger);
	}

	led_row { |y, on = 255|
		target.sendMsg("/box/led_row", y.asInteger, on.asInteger);
	}

	led_col { |x, on = 255|
		target.sendMsg("/box/led_col", x.asInteger, on.asInteger);
	}
	
	intensity { |i|
		target.sendMsg("/box/intensity", i);
	}

	clear { |on|
		on = on ?? 0;
		8.do({ |i| target.sendMsg("/box/led_row", i, on * 255); });
	}
	
	test { |on|
		target.sendMsg("/box/test", on.asInteger);
	}
}
