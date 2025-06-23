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
		
		SnView view = new SnView.Impl(map);
		
		//traversal
		view = view.asMap().get("baz");
		view = view.asList().get(0);
		view = view.asList().get(0);
		
		//where am I?
		System.out.println("i am at " + view.path());
		// -> i am at baz.0.0
		
		view.asMap();
		// -> SnException: Expected a map, but there was a string at 'baz.0.0'
	}
}
