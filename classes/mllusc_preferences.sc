MlluscPreferences{
var <items;
var preference_file_path;
	*new{
    		^super.new.init();
	} 
	
	init {
		items=[];
		preference_file_path="glofu_preferences";
	}
	
	register_item{|i,d|
		items=items.add([i,d]);
	}
	
	set_preference_file_path{|p|
		preference_file_path=p;
	}
	
	load{
		var string;
		if(preference_file_path.pathExists==\file,
			{},
			{^(false)}
		);
		string=Object.readArchive(preference_file_path);
		string.split($|).do({|o|
			if((o.notNil && o!=""),{
				var name_and_attr=o.split($*);
				var index;
				var item=items.detectIndex({ arg item, i; item[0]==name_and_attr[0];index=i;});
				name_and_attr.postln;
				if(name_and_attr!=nil,
				{items[index][1]=name_and_attr[1]});
				"after".postln;
			});
			"--".postln;
		});
	}
	
	save{
		var string="";
		items.do({|item,index|
			string=(string++""++item[0]++":"++item[1]++";");
		});
		preference_file_path.postln;
		string.writeArchive(preference_file_path);
	}
	
	get_value{|name|
		^items.detect({ arg item, i; item[0]==name })[1];
	}
	
	set_value{|name,value|
		items.detect({ arg item, i; item[0]==name })[1]=value;
		this.save();
	}
}