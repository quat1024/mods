package agency.highlysuspect.quatlib.craftless.config.sn;

import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;

public class SnTesttesttest {
	public static void main(String... args) throws ReportedException {
		FailureRoot failures = new FailureRoot("SnTesttesttest")
			.addListener(new FailureLogger(LogFacade.Sysout.INSTANCE));
		CtxChain ctx = failures.context();
		
		SnMap map = Sn.map(
			"foo", "bar",
			"baz", Sn.list(
				Sn.list(
					"wow"
				),
				"jadjaskda"
			)
		);

		SnView view = map.view(ctx.detail("my cool file"));

		//traversal
		view = view.asMap().get("baz");
		view = view.asList().get(0);
		view = view.asList().get(0);

		//where am I?
		System.out.println("i am at " + view.ctx());
		// -> i am at baz.0.0

		//view.asMap();
		// -> SnException: Expected a map, but there was a string at 'baz.0.0'

		main2();
	}

	public static void main2() throws ReportedException {
		FailureRoot failures = new FailureRoot("SnTesttesttest#main2")
			.addListener(new FailureLogger(LogFacade.Sysout.INSTANCE));
		CtxChain ctx = failures.context();
		
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
		""").parseTopLevel(ctx);

		SnView view = sn.view(ctx);

		view = view.asMap().get("modules");
		view = view.asMap().get("gaming");
		view = view.asMap().get("virus");

		String virusStatus = view.asString();
		System.out.println(virusStatus);
		//-> Very Yes
		System.out.println(view.ctx());
		//-> myCoolFile.txt, modules.gaming.virus
		view.asMap();
		//-> Expected a map, but there was a string / at 'modules.gaming.virus' / myCoolFile.txt
	}
}
