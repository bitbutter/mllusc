MlluscStorableObjectProxy{
var <object;
var <identifier_string;
var <params_list;
*new{|o,i,a|
    		^super.new.init(o,i,a);
	} 
	
	init {|o,i,a|
		object=o;
		identifier_string=i;
		params_list=List.new();
		a.do({|it,in|
			params_list.add(it);
		})
	}
	
	load_state_from_string{|s|
		var attrs=s.split($;);  // att, value pairs
		attrs.do({|a|
			if (a!="",{
				var last_split=a.split($:);
				var set_method_string=("set_"++last_split[0]);
				var as_type_string=("as"++last_split[2]);
				var val;
				if (as_type_string=="asNil",{val=nil},{
					if (as_type_string=="asFalse",{val=false},{
						if (as_type_string=="asTrue",{val=true},{
							val=last_split[1].perform(as_type_string.asSymbol);
						});
					});
				});
				object.perform(set_method_string.asSymbol,val);
			});
		});

		try {object.perform(\storable_attributes_loaded)};
	}
	
	get_string_representation{
		var string="";
		params_list.do({|it,k|
			var val=object.perform(("get_"++it).asSymbol);
			var type=val.class;
			string=(string++""++it++":"++val++":"++type++";");
		});
		^("|"++identifier_string++"*"++string); // |idstring*attname:value:type;attname:value:type;
	}
}