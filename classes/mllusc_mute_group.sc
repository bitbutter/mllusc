MlluscMuteGroup{
var index;
var <mutables;

*new{|i|
    		^super.new.init(i);
	} 
	
	init {|i|
		index=i;
	}
	
	mute{
		mutables.do({|m|
			m.mute();
		});
		this.changed(this, [\group_deactivated, index]);
	}
	
	mute_except{|o|
		mutables.do({|m|
			if (m.get_status()!="stopped",
			{
				if(m===o,
				{},
				{m.mute()});
			});
		})
	}
	
	get_index{
		^(index);
	}
	
	get_number_of_mutables{
		^(mutables.size);
	}
	
	add_mutable{|m|
		mutables=mutables.add(m);
	}
	
	remove_mutable{|m|
		mutables=mutables.takeThese({ arg item, index; item===m; });
	}
	
	all_mutables_have_stopped{
		mutables.do({|m|
			if (m.get_status()!="stopped",
			{^(false)});
		});
		^(true);
	}
	
	//------
	
	
}