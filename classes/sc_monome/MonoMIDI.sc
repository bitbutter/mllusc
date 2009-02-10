//-----------------------------------------------------------------
//
// Bridge between Monome OSC and MIDI messaging.
//  __ Daniel Jones
//     http://www.erase.net
//
// Run serialio with:
//    ./serialio localhost:57120
//
// In Audio MIDI Setup, configure the MIDI IAC device with
// two ports named "A" and "B", each with one input and output.
// MonoMIDI writes to port A and reads from port B, and so your
// MIDI host program must read from A and write to B.
//
// To create a bridge, simply execute:
//    m = MonoMIDI.new;
//
//
//-----------------------------------------------------------------

MonoMIDI {
	var <>monome;
	var <>midiOut;
	var <>midiPort = 0;
	
	*new { |monome|
		^super.newCopyArgs(monome).init;
	}
	
	init {
		monome = monome ?? Monome.new;
		MIDIClient.init;
		midiOut = MIDIClient.destinations.detect({ |x| x.name == "A" }).uid;
		midiOut = MIDIOut(0, midiOut);
		MIDIIn.connect(0, MIDIClient.sources.detect({ |x| x.name == "B" }));
		MIDIIn.noteOn = { |src, chan, num, value|
			var x, y;
			[ src, chan, num, value ].postln;
			x = num % 8;
			y = div(num, 8);
			if ((x < 8) && (y < 8))
			{
				monome.led(x, y, (value > 0).binaryValue);
			};
		};
		MIDIIn.noteOff = { |src, chan, num, value|
			var x, y;
			x = num % 8;
			y = div(num, 8);
			if ((x < 8) && (y < 8))
			{
				monome.led(x, y, 0);
			};
		};
		monome.clear;
		monome.action = { |x, y, value|
			if (value == 1) {
				midiOut.noteOn(0, (y * 8) + x, 127);
			};
			if (value == 0) {
				midiOut.noteOff(0, (y * 8) + x, 127);
			};
		};
	}
}
