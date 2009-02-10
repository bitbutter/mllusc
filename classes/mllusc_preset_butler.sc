MlluscPresetButler{
var <object_proxies;
var <current_directory;
var <preset_dictionary;
	*new{
    		^super.new.init();
	} 
	
	init {
		object_proxies=Dictionary.new();
		current_directory=""; // default to the supercollider dir
		preset_dictionary=Dictionary.new();
	}
	
	register_object{|o,i,a|
		this.register_object_proxy(MlluscStorableObjectProxy.new(o,i,a));
	}
	
	load_preset_by_name{|name|
		^(this.load_preset_from_path(preset_dictionary.at(name)));
	}
	
	load_preset_from_path{|path|
	var string;

		if(path.pathExists==\file,
			{},
			{^(false)}
		);
		string=Object.readArchive(path);
		string.split($|).do({|o|
			if((o.notNil && o!=""),{
				var name_and_attr=o.split($*);
				object_proxies.at(name_and_attr[0]).load_state_from_string(name_and_attr[1]);
			});
		});
	}
	
	store_preset{|name|
		var stored_name;
		stored_name=(""++current_directory++"/"++name++"."++this.get_timestamp());
		(""++this.get_big_string()).writeArchive(stored_name);
		this.populate_preset_dictionary();
		^(stored_name);
	}
	
	save_preset_at_path{|p|
		this.get_big_string().writeArchive(p);
		^(p);
	}
	
	set_directory{|path|
		current_directory=path;
		this.populate_preset_dictionary();
	}
	
	delete_preset{|name|
		var f=preset_dictionary.at(name);
		if(this.file_exists(f),
		{File.delete(f)},
		{^false});
	}
	
	// ---------------------------
	get_unique_file_path{|name|
		var temppath=current_directory++"/"++name++"."++this.get_timestamp();
		var matches = (current_directory++"/"++name++".").pathMatch;
		if(matches.size > 0,{
			^(this.get_unique_file_path(name.realNextName));
		},{^(temppath)});
	}
	
	populate_preset_dictionary{
		var itemsindir = (current_directory++"/"++"*").pathMatch; 
		itemsindir.do({|it,in|
			preset_dictionary.put(PathName(it).fileName.split($.)[0],it);
		});
	}
	file_exists{|path|
		^(path.pathExists==\file);
	}
	get_timestamp{
	 ^(Date.localtime().stamp);
	}
	
	register_object_proxy{|p|
		object_proxies.put(p.identifier_string, p);
	}
	
	get_big_string{
		var string="";
		object_proxies.do({|it,k|
			string=string++(it.get_string_representation());
		});
		^string;
	}
}