package agency.highlysuspect.packages.craftful.junk;

//TODO: better name lol
public interface PackageContainer2 {
	ImmutablePackageContents getContents();
	
	//Basically fabric-transfer-api has this transaction system. On Fabric my PackageStorage can end up calling
	//setContentsNonCommitted a few times in the middle of a transaction; mid-transaction state ends up "living"
	//in the BlockEntity for a while as the transfer system figures out a solution that works for all parties to
	//the transfer. This is supposed to be safe because if the transaction is rolled back, it will re-call
	//setContentsNonCommitted with the pre-transaction value and it's like nothing ever happened. If the transaction is
	//accepted, it will call commitContents and the last setContentsNonCommitted call becomes the new state.
	//That's how I think it works anyway.
	//N.b. Anything else computed from the package (like the comparator fill level or w/e) also needs to be delayed
	//until the commitContents call.
	void setContentsNonCommitted(ImmutablePackageContents newContents);
	void commitContents();
	
	PackageRules getRules();
}
