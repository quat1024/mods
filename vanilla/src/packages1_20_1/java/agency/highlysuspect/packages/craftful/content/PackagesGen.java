package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.quatlib.craftless.facet.Gen;

import java.util.function.Consumer;

public class PackagesGen implements Gen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		more.accept(new PackageGen());
		more.accept(new PackageMakerGen());
	}
}
