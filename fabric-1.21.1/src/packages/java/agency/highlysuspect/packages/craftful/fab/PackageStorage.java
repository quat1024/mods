package agency.highlysuspect.packages.craftful.fab;

import agency.highlysuspect.packages.craftful.junk.ImmutablePackageContents;
import agency.highlysuspect.packages.craftful.junk.PackageContainer2;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

public class PackageStorage extends SnapshotParticipant<ImmutablePackageContents> implements SingleSlotStorage<ItemVariant> {
	public PackageStorage(PackageContainer2 container) {
		this.container = container;
	}
	
	public final PackageContainer2 container;
	
	@Override
	protected ImmutablePackageContents createSnapshot() {
		return container.getContents();
	}
	
	@Override
	protected void readSnapshot(ImmutablePackageContents snapshot) {
		container.setContentsNonCommitted(snapshot);
	}
	
	@Override
	protected void onFinalCommit() {
		container.commitContents();
	}
	
	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		int maxAmountSmall = (int) QuatUtil.clampL(maxAmount, 0, Integer.MAX_VALUE);
		ItemStack stackToInsert = resource.toStack(maxAmountSmall);
		
		ImmutablePackageContents.InsertionResult result = container.getContents().withInsertion(stackToInsert, maxAmountSmall, container.getRules());
		if(result.insertedAmount() > 0) {
			updateSnapshots(transaction);
			container.setContentsNonCommitted(result.newContents());
		}
		return result.insertedAmount();
	}
	
	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		int maxAmountSmall = (int) QuatUtil.clampL(maxAmount, 0, Integer.MAX_VALUE);
		ItemStack stackToExtract = resource.toStack(maxAmountSmall);
		
		ImmutablePackageContents.TakeResult result = container.getContents().withFilteredTake(stackToExtract, maxAmountSmall, container.getRules());
		if(result.takenAmount() > 0) {
			updateSnapshots(transaction);
			container.setContentsNonCommitted(result.newContents());
		}
		return result.takenAmount();
	}
	
	@Override
	public boolean isResourceBlank() {
		return container.getContents().isEmpty();
	}
	
	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(container.getContents().stack());
	}
	
	@Override
	public long getAmount() {
		return container.getContents().count();
	}
	
	@Override
	public long getCapacity() {
		ImmutablePackageContents contents = container.getContents();
		// "or an estimated upper bound on the number of resources that could be stored if this view has a blank resource."
		return contents.isEmpty() ? 512 : container.getRules().maxInPackageTotal(contents.stack());
	}
}
