MlluscMuteGroupableDecorator{
var object;
var <mute_group;
var playing_status;

	// decorated objects must implement a mute method
	mute{
		object.mute();
	}

	*new{|o|
    		^super.new.init(o);
	} 
	
	init {|o|
		object=o;
	}
	
	=={|a|
		^(object==a);
	}
	
	addDependant{|o|
		object.addDependant(o);
	}
	
	doesNotUnderstand {|selector,args|
		if(args.notNil,
		{^(object.perform(selector,args))},
		{^(object.perform(selector))});
	}	
	
	set_mute_group {|g|
		if (mute_group!==g,
		{
			if (mute_group.notNil,
				{mute_group.remove_mutable(this)}); // remove from array in the old group object
			mute_group=g;
			mute_group.add_mutable(this); // add to an array in the new group object
		});
	}
	
	mute_my_siblings{
		mute_group.mute_except(this);
	}
	
	set_playing_status{|s|
		playing_status=s;
	}
	
	get_mute_group_index{
		^(mute_group.get_index());
	}
	
}