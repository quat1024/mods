package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftless.content.PackageGenBase;
import agency.highlysuspect.packages.craftless.content.PackagesGenBase;

public class PackagesGen extends PackagesGenBase {
	@Override
	protected PackageGenBase<?> packageGen() {
		return new PackageGen();
	}
}
