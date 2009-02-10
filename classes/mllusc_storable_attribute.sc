MlluscStorableAttribute{
var <object;
var <attribute_name;
var <value_type;
*new{|o,n,t|
    		^super.new.init(o,n,t);
	} 
	
	init {|o,n,t|
		object=o;
		attribute_name=n;
		value_type=t;
	}
	
	get_string_representation{
		var value=(object.perform("get_"++attribute_name));
		^(""++attribute_name.asString++":"++value++";");
	}
	
	load_state_from_string{|s|
		var array=s.split(':');
		var value=array[1].perform("as"++value_type);
		object.perform("set_"++attribute_name,value);
	}
}
