package agency.highlysuspect.packages.craftful.junk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ImmutablePackageContents(ItemStack stack, int count) {
	public static ImmutablePackageContents EMPTY = new ImmutablePackageContents(ItemStack.EMPTY, 0);
	
	public record TooltipStats(ItemStack rootContents, int fullyMultipliedCount, boolean amplified) {}
	public TooltipStats computeTooltipStats() {
		List<ImmutablePackageContents> containers = new ArrayList<>();
		ImmutablePackageContents cont = this;
		do {
			containers.add(cont);
			cont = cont.stack.get(DATA_COMPONENT_TYPE);
		} while(cont != null && !cont.isEmpty());
		
		//what's at the middle?
		ImmutablePackageContents last = containers.getLast();
		ItemStack rootContents = last.stack;
		if(rootContents.isEmpty()) return new TooltipStats(rootContents, 0, false);
		
		//how many items are there, for real?
		int fullyMultipliedCount = last.count;
		boolean amplified = false;
		for(int i = 0; i < containers.size() - 1; i++) {
			ImmutablePackageContents container = containers.get(i);
			int c = container.count;
			fullyMultipliedCount *= count;
			amplified |= count > 1;
		}
		return new TooltipStats(rootContents, fullyMultipliedCount, amplified);
	}
	
	public int calcRecursionLevel() {
		ImmutablePackageContents recur = stack.get(DATA_COMPONENT_TYPE);
		if(recur == null) return 0;
		else return 1 + recur.calcRecursionLevel();
	}
	
	public boolean isFull(PackageRules rules) {
		int maxCount = rules.maxInPackageTotal(stack);
		return maxCount == 0 || count == maxCount;
	}
	
	public float fillPercentage(PackageRules rules) {
		int maxCount = rules.maxInPackageTotal(stack);
		if(maxCount == 0) return 1;
		else return count / (float) maxCount;
	}
	
	public boolean isEmpty() {
		return stack.isEmpty() || count == 0;
	}
	
	///
	
	//TODO: put insertion/removal methods in here too
	// right now i'm still piggying off the PackageContents implementation
	
	public boolean allowedToInsert(ItemStack other, PackageRules rules) {
		return rules.allowedToInsertInPackage(other) &&
			(isEmpty() || other.isEmpty() || ItemStack.isSameItemSameComponents(stack, other));
	}
	
	public record InsertionResult(ImmutablePackageContents newContents, int insertedAmount) {
		private static InsertionResult none(ImmutablePackageContents x) {
			return new InsertionResult(x, 0);
		}
	}
	public InsertionResult withInsertion(ItemStack other, int maxToInsert, PackageRules rules) {
		//allowedToInsert checks that 'stack' and 'other' are compatible
		if(other.isEmpty() || !allowedToInsert(other, rules)) return InsertionResult.none(this);
		
		//how much space is left in the package
		int remainingSpace = rules.maxInPackageTotal(stack) - count;
		if(remainingSpace <= 0) return InsertionResult.none(this); //no room
		
		//how much will actually be inserted
		int toInsert = Math.min(remainingSpace, Math.min(other.getCount(), maxToInsert));
		if(toInsert == 0) return InsertionResult.none(this);
		
		return new InsertionResult(
			new ImmutablePackageContents(stack, count + toInsert),
			toInsert
		);
	}
	
	public record TakeResult(ImmutablePackageContents newContents, int takenAmount) {
		private static TakeResult none(ImmutablePackageContents x) {
			return new TakeResult(x, 0);
		}
	}
	public TakeResult withTake(int maxToTake, PackageRules rules) {
		if(isEmpty() || maxToTake == 0) return TakeResult.none(this);
		
		int toTake = Math.min(count, maxToTake);
		int leftover = count - toTake;
		return new TakeResult(leftover == 0 ? ImmutablePackageContents.EMPTY : new ImmutablePackageContents(stack, leftover), toTake);
	}
	
	public TakeResult withFilteredTake(ItemStack filter, int maxToTake, PackageRules rules) {
		if(!isEmpty() && !filter.isEmpty() && !ItemStack.isSameItemSameComponents(stack, filter)) return TakeResult.none(this);
		else return withTake(maxToTake, rules);
	}
	
	///
	
	private Optional<ItemStack> stackForSerialization() {
		return stack.isEmpty() ? Optional.empty() : Optional.of(stack.copyWithCount(1));
	}
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static ImmutablePackageContents rehydrate(Optional<ItemStack> stackOpt, int realCount) {
		if(realCount == 0) return EMPTY;
		if(stackOpt.isEmpty()) return EMPTY;
		
		ItemStack stack = stackOpt.get();
		if(stack.isEmpty()) return EMPTY;
		
		return new ImmutablePackageContents(stack, realCount); //Not copying the stack should be fine, it was just deserialized from something
	}
	
	public static final Codec<ImmutablePackageContents> CODEC = RecordCodecBuilder.create(i -> i.group(
		ItemStack.CODEC.optionalFieldOf("stack").forGetter(ImmutablePackageContents::stackForSerialization),
		Codec.INT.fieldOf("count").forGetter(ImmutablePackageContents::count)
	).apply(i, ImmutablePackageContents::rehydrate));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, ImmutablePackageContents> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(RegistryFriendlyByteBuf buf, ImmutablePackageContents cont) {
			Optional<ItemStack> filt = cont.stackForSerialization();
			if(filt.isEmpty()) buf.writeInt(0);
			else {
				buf.writeInt(cont.count());
				ItemStack.STREAM_CODEC.encode(buf, filt.get());
			}
		}
		
		@Override
		public ImmutablePackageContents decode(RegistryFriendlyByteBuf buf) {
			int realCount = buf.readInt();
			if(realCount == 0) return ImmutablePackageContents.rehydrate(Optional.empty(), 0);
			
			ItemStack filt = ItemStack.STREAM_CODEC.decode(buf);
			return ImmutablePackageContents.rehydrate(Optional.of(filt), realCount);
		}
	};
	
	public static final DataComponentType<ImmutablePackageContents> DATA_COMPONENT_TYPE =
		new DataComponentType.Builder<ImmutablePackageContents>().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();
	
	//styled after PotDecorations
	public CompoundTag save(CompoundTag in) {
		in.put("PackageContents", CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
		return in;
	}
	
	public static ImmutablePackageContents load(@Nullable CompoundTag in) {
		if(in == null) return EMPTY;
		Tag t = in.get("PackageContents");
		if(t == null) return null;
		else return CODEC.parse(NbtOps.INSTANCE, t).result().orElse(EMPTY);
	}
	
	//Need to override equals() and hashCode() because of the ItemStack, it doesn't implement either one.
	
	@Override
	public boolean equals(Object o) {
		if(o == null || getClass() != o.getClass()) return false;
		
		ImmutablePackageContents other = (ImmutablePackageContents) o;
		return count == other.count && ItemStack.isSameItemSameComponents(stack, other.stack);
	}
	
	@Override
	public int hashCode() {
		return 31 * ItemStack.hashItemAndComponents(stack) + count;
	}
}
