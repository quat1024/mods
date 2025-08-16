package agency.highlysuspect.packages.craftful.junk;

public interface PackageContainer2 {
	ImmutablePackageContents getContents();
	void setContentsNonCommitted(ImmutablePackageContents newContents);
	void commitContents();
	
	PackageRules getRules();
}
