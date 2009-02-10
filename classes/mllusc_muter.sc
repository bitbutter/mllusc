// Responsibilities:
// Decorates objectes that should be part of mute groups
// Creates group objects behind the scenes
// listens and reacts to events that should change the state of groups and their members
MlluscMuter {
	var registered_mutables;
	var mute_groups;
	var <>mutable_added_to_group_callback_function;
	var <>mute_group_became_inactive_callback_function;
	*new{|c|
		^super.new.init(c);
	} 
	
	init {|c|
		registered_mutables=Array.new();
		mute_groups=Array.new();
	}
	
	mute_all{
		mute_groups.do({|m|
			m.mute();
		})
	}
	
	mute_this_group{|index|
		this.get_group_by_index(index).mute();
	}
	
	make_mutables_group_inactive{|mutable|
		var m=this.find_decorated_version_of_mutable(mutable);
		m.set_playing_status(0);
		mute_group_became_inactive_callback_function.value(m.get_mute_group_index());
	}
	
	add_to_group{|mutable,group_index|
		var group=this.create_or_return_group(group_index);
		mutable=this.register_mutable(mutable); // register and add decorator if necessary
		mutable.mute();
		mutable.set_mute_group(group);
		mutable_added_to_group_callback_function.value(mutable,group_index);
		^(mutable); // returns the decorated object
	}
	
	get_preset_mutables{
		var string="";
		// the position of each mute group in the resultant string is significant
		registered_mutables.do({|m,i|
			string=string++m.get_mute_group_index()++"x";
		});
		^(string);
	}
	
	set_preset_mutables{|s|
		s.split($x).do({|it,in|
			if(it!="",
				{this.add_to_group(registered_mutables[in],it.asInteger)}
			);
		});
	}
	
	// Private methods ---------------------------------------------------------------------
	
	register_mutable{|mutable|
		mutable=this.find_decorated_version_of_mutable(mutable);
		if(this.mutable_already_registered(mutable),
		{^(mutable)},
		{
			registered_mutables=registered_mutables.add(mutable); // add to array
			^(mutable);
		});
	}
	
	find_decorated_version_of_mutable{|m|
		if (this.mutable_is_already_decorated(m),
		{^m});
		registered_mutables.do({|it,in|
			if(it===m,
			{^(it)}); // returns group object if found
			 // returns nil if group doesn't exist
		});
		^(this.decorate_with_mute_groupable(m));
	}
	
	mutable_already_registered{|mutable|
		registered_mutables.do({|m|
			if (mutable===m,{^true});
		})
		^(false);
	}
	
	decorate_with_mute_groupable{|m|
		if (this.mutable_is_already_decorated(m),
		{^m},
		{^(MlluscMuteGroupableDecorator.new(m))});
	}
	
	
	
	mutable_is_already_decorated{|m|
		^(m.respondsTo('mute_my_siblings'));
	}
	
	create_or_return_group{|i|
		var grp=this.get_group_by_index(i);
		if(grp.notNil,
		{grp=grp},
		{var ng=MlluscMuteGroup.new(i);
		ng.addDependant(this); // listen to events from this group
		mute_groups=mute_groups.add(ng);
		grp=ng});
		^grp;
	}
	
	get_group_by_index{|i|
		mute_groups.do({|it,in|
			if(it.get_index()==i,
			{^(it)}); // returns group object if found
		});
		^nil; // returns nil if group doesn't exist
	}
	
	get_group_index_by_mutable{|m|
		^(this.find_decorated_version_of_mutable(m).get_mute_group_index());
	}
}
