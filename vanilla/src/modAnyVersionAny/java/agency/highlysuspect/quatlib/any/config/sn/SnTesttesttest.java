package agency.highlysuspect.quatlib.any.config.sn;

public class SnTesttesttest {
	public static void main(String... args) throws SnException {
		SnMap map = Sn.map(
			"foo", "bar",
			"baz", Sn.list(
				Sn.list(
					"wow"
				),
				"jadjaskda"
			)
		);
		
		SnView view = map.view();
		
		//traversal
		view = view.asMap().get("baz");
		view = view.asList().get(0);
		view = view.asList().get(0);
		
		//where am I?
		System.out.println("i am at " + view.path());
		// -> i am at baz.0.0
		
		//view.asMap();
		// -> SnException: Expected a map, but there was a string at 'baz.0.0'
		
		main2();
	}
	
	public static void main2() throws SnException {
		Sn<?> sn = new SnParser("""
			modules {
			  gaming {
			    enabled = false
			    virus = Very Yes
			  }
			  fruits {
			    enabled = false
			  }
			}
		""").parseTopLevel();
		
		SnView view = sn.view();
		
		view = view.asMap().get("modules");
		view = view.asMap().get("gaming");
		view = view.asMap().get("virus");
		
		String virusStatus = view.asString();
		System.out.println(virusStatus);
		//-> Very Yes
		System.out.println(view.path());
		//-> modules.gaming.virus
		view.asMap();
		//-> SnException: Expected a map, but there was a string at 'modules.gaming.virus'
	}
}
