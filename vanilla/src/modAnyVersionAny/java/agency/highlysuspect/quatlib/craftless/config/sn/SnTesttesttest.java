package agency.highlysuspect.quatlib.craftless.config.sn;

import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.FailureSourceSink;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogReporter;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;

public class SnTesttesttest {
	public static void main(String... args) throws ReportedException {
		LogFacade log = new LogFacade.Sysout();
		FailureSourceSink failures = new FailureLogReporter(log);
		
		SnMap map = Sn.map(
			"foo", "bar",
			"baz", Sn.list(
				Sn.list(
					"wow"
				),
				"jadjaskda"
			)
		);

		SnView view = map.view(failures.detail("my cool file"));

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
		LogFacade log = new LogFacade.Sysout();
		FailureSourceSink failures = new FailureLogReporter(log);
		CtxChain ctx = failures.detail("myCoolFile.txt");
		
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
