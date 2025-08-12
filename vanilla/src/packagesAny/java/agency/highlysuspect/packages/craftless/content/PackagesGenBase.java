package agency.highlysuspect.packages.craftless.content;

import agency.highlysuspect.quatlib.craftless.facet.Gen;

import java.util.function.Consumer;

public abstract class PackagesGenBase implements Gen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		more.accept(packageGen());
	}
	
	protected abstract PackageGenBase<?> packageGen();
}
