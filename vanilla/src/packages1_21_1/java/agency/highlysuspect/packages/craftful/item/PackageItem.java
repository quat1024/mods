package agency.highlysuspect.packages.craftful.item;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.PropsCommon;
import agency.highlysuspect.packages.craftful.junk.ImmutablePackageContents;
import agency.highlysuspect.packages.craftful.junk.PackageRules;
import agency.highlysuspect.packages.craftful.junk.PackageStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PackageItem extends BlockItem {
	public PackageItem(Block block, Properties settings) {
		super(block, settings);
	}
	
	public ItemStack createCustomizedStack(Block frame, Block inner, DyeColor color) {
		ItemStack i = new ItemStack(this);
		i.set(PackageStyle.DATA_COMPONENT_TYPE, new PackageStyle(frame, inner, color));
		return i;
	}
	
	public @NotNull ImmutablePackageContents getContents(ItemStack stack) {
		return Objects.requireNonNull(stack.get(ImmutablePackageContents.DATA_COMPONENT_TYPE), "PackageItem should have ImmutablePackageContents data component");
	}
	
	private int nameReentrancy = 0;
	
	@Override
	public Component getName(ItemStack stack) {
		ImmutablePackageContents contents = stack.get(ImmutablePackageContents.DATA_COMPONENT_TYPE);
		if(contents == null) return super.getName(stack);
		
		ItemStack contained = contents.stack();
		if(contained.isEmpty()) return super.getName(stack);
		
		try {
			nameReentrancy++;
			MutableComponent contentsComponent = Component.translatable("block.packages.package.nonempty.contents", contents.count(), contained.getHoverName())
				.withStyle(s -> s.withColor(switch(nameReentrancy) {
					case 1 -> 0xD0D0D0;
					case 2 -> 0xA0A0A0;
					case 3 -> 0x858585;
					default -> 0x666666;
				}));
			
			if(nameReentrancy == 1) {
				return Component.translatable("block.packages.package.nonempty", super.getName(stack), contentsComponent);
			} else {
				return Component.translatable("block.packages.package.nonempty.reentrant", contentsComponent);
			}
		} finally {
			nameReentrancy--;
		}
	}
	
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag mistake) {
		ImmutablePackageContents contents = getContents(stack);
		ImmutablePackageContents.TooltipStats stats = contents.computeTooltipStats();
		//If there's at least one layer of nontrivial nesting going on, advertise how many items there are if the entire package was unrolled.
		if(stats.amplified() && !stats.rootContents().isEmpty()) {
			tooltip.add(
				Component.translatable("packages.contents_tooltip.utimately",
					Component.translatable("block.packages.package.nonempty.contents",
						stats.fullyMultipliedCount(),
						stats.rootContents().getHoverName()
					).withStyle(ChatFormatting.DARK_RED)
				).withStyle(ChatFormatting.DARK_GRAY)
			);
		}
		
		if(Packages.inst().proxy.hasShiftDownForTooltip()) {
			PackageStyle style = stack.getOrDefault(PackageStyle.DATA_COMPONENT_TYPE, PackageStyle.ERROR_LOL);
			Block frameBlock = style.frameBlock();
			Block innerBlock = style.innerBlock();
			if(frameBlock == innerBlock) {
				tooltip.add(Component.translatable("packages.style_tooltip.both", frameBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			} else {
				tooltip.add(Component.translatable("packages.style_tooltip.frame", frameBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
				tooltip.add(Component.translatable("packages.style_tooltip.inner", innerBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			}
			
			DyeColor color = style.color();
			tooltip.add(Component.translatable("packages.style_tooltip.color",
				Component.translatable("color.minecraft." + color.getSerializedName())
					.withStyle(s -> s.withColor((color == DyeColor.BLACK ? DyeColor.GRAY : color).getTextColor()).withItalic(true))
			));
		} else {
			//Lifting this stylization from Create, not out of trying to steal their thunder, more so a modpack has fewer unique kinds of "hold shift for xx" tooltips lol
			tooltip.add(Component.translatable("packages.style_tooltip.hold_for_composition",
				Component.translatable("packages.style_tooltip.shift").withStyle(ChatFormatting.GRAY)
			).withStyle(ChatFormatting.DARK_GRAY));
		}
		
		super.appendHoverText(stack, ctx, tooltip, mistake);
	}
	
	@Override
	public boolean isBarVisible(ItemStack stack) {
		if(stack.getCount() != 1) return false; //Clips with the number and looks bad, and stacked packages aren't interactable anyway.
		return !getContents(stack).isEmpty();
	}
	
	@Override
	public int getBarWidth(ItemStack stack) {
		ImmutablePackageContents contents = getContents(stack);
		return Math.min((int) (1 + 12 * contents.fillPercentage(PackageRules.DEFAULT)), 13);
	}
	
	@Override
	public int getBarColor(ItemStack stack) {
		ImmutablePackageContents contents = getContents(stack);
		if(Packages.inst().proxy.useRedBarWhenFull() && contents.isFull(PackageRules.DEFAULT)) return 0xD5636A; //Nice tomato-ey red color
		else return 0x6666FF; //Same color as the bundle's bar
	}
	
	@Override
	public boolean overrideStackedOnOther(ItemStack me, Slot slot, ClickAction clickAction, Player player) {
		if(clickAction != ClickAction.SECONDARY || !Packages.inst().config.get(PropsCommon.INVENTORY_INTERACTIONS)) {
			return super.overrideStackedOnOther(me, slot, clickAction, player);
		}
		
		if(me.getCount() != 1) return false;
		ItemStack other = slot.getItem();
		
		//These are two stackable packages. Presumably, the player wants to stack them,
		//instead of putting one inside the other. They can always be nested in-world.
		if(ItemStack.isSameItemSameComponents(me, other)) return false;
		
		//NEW LOGIC
		
		ImmutablePackageContents myContents = getContents(me);
		PackageRules rules = PackageRules.DEFAULT; //TODO (far future): Different packages with different rules?
		
		if(other.isEmpty() && !myContents.isEmpty()) {
			//The package contains items, but the slot is empty. Take one stack of items from the package and deposit it into the slot.
			return dropIntoSlot2(player, slot, me, myContents, rules);
		} else if(!other.isEmpty()) {
			//Slot is not empty, try to sponge up items from the slot into the package.
			boolean absorbSuccess = absorbFromSlot2(player, slot, me, myContents, rules);
			if(absorbSuccess) return true;
			
			//If !absorbSuccess, no package insertion was done so myContents is not stale.
			//If we're here, we're in a situation where the player clicked a slot that has an item matching the package's contents,
			//but we couldn't draw any of the items into the package because it was full.
			//In this case, we should replenish the slot's contents with more items from the package.
			if(myContents.canStackWith(other) && myContents.isFull(rules)) {
				if(other.getMaxStackSize() - other.getCount() > 0) { //If there's room in the slot to drop items
					return dropIntoSlot2(player, slot, me, myContents, rules);
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean overrideOtherStackedOnMe(ItemStack me, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		if(clickAction != ClickAction.SECONDARY || !Packages.inst().config.get(PropsCommon.INVENTORY_INTERACTIONS)) {
			return super.overrideOtherStackedOnMe(me, other, slot, clickAction, player, slotAccess);
		}
		
		if(me.getCount() != 1) return false;
		
		//These are two stackable packages. Presumably, the player wants to stack them,
		//instead of putting one inside the other. They can always be nested in-world.
		if(ItemStack.isSameItemSameComponents(me, other)) return false;
		
		//NEW LOGIC
		ImmutablePackageContents myContents = getContents(me);
		PackageRules rules = PackageRules.DEFAULT;
		
		//here "other" is the itemstack on the player's cursor. Can't modify it but can change the size
		if(myContents.canStackWith(other)) {
			ImmutablePackageContents.InsertionResult insertionResult = myContents.withInsertion(other, Integer.MAX_VALUE, rules);
			if(insertionResult.insertedAmount() > 0) {
				other.shrink(insertionResult.insertedAmount());
				player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
				me.set(ImmutablePackageContents.DATA_COMPONENT_TYPE, insertionResult.newContents());
				return true;
			}
		}
		return false;
	}
	
	//Mop up items from a slot into the PackageContainer. Always grabs as much as it can.
	private boolean absorbFromSlot2(Player player, Slot slot, ItemStack me, ImmutablePackageContents myOldContents, PackageRules rules) {
		if(slot.getItem().isEmpty() || !myOldContents.canStackWith(slot.getItem()) || !rules.allowedInPackageAtAll(slot.getItem()))
			return false;
		
		int remainingSpaceInPackage = rules.maxInPackageTotal(slot.getItem()) - myOldContents.count();
		if(remainingSpaceInPackage <= 0) return false;
		
		//pull it out of the slot... this happens for real, no take backsies past this point
		//for a slot with !allowModification (which, in practice, is crafting slots) the second argument is used as a threshold.
		//you can't take less than arg#2 items from an !allowModification slot. this covers cases like, there being one remaining
		//spot in the package, but the crafting output slot has 4 items in it - it just wont take at all
		//for all slots the smaller of the two numeric arguments is used as the maximum amount to take. overspecifying won't dupe
		ItemStack grabbedFromSlot = slot.safeTake(remainingSpaceInPackage, remainingSpaceInPackage, player);
		if(grabbedFromSlot.isEmpty()) return false;
		
		ImmutablePackageContents.InsertionResult insertionResult = myOldContents.withInsertion(grabbedFromSlot, Integer.MAX_VALUE, rules);
		
		//check that all of the items were actually inserted
		if(grabbedFromSlot.getCount() != insertionResult.insertedAmount()) {
			Packages.LOG.warn("In an absorbFromSlot action from {}, I picked up {} items from a slot but {} fit in the package. Item loss or duplication may have occured. Can you file an issue about what caused this? Thanks.", player.getScoreboardName(), grabbedFromSlot.getCount(), insertionResult.insertedAmount());
		}
		
		//sound
		player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
		
		//update my data component
		me.set(ImmutablePackageContents.DATA_COMPONENT_TYPE, insertionResult.newContents());
		return true;
	}
	
	//Drop a stack of items from a PackageContainer into a slot.
	//Returns whether it did anything
	private boolean dropIntoSlot2(Player player, Slot slot, ItemStack me, ImmutablePackageContents myOldContents, PackageRules rules) {
		if(myOldContents.isEmpty() || !myOldContents.canStackWith(slot.getItem())) return false;
		
		int remainingSpaceInSlot;
		if(slot.getItem().isEmpty()) {
			remainingSpaceInSlot = slot.getMaxStackSize(myOldContents.stack());
		} else {
			remainingSpaceInSlot = slot.getMaxStackSize(slot.getItem()) - slot.getItem().getCount();
		}
		if(remainingSpaceInSlot <= 0) return false; //No more space in slot
		
		//Intentionally using stack().getMaxStackSize() here, instead of using the PackageRules,
		//because for cases where you have a package of packages, I want to deposit the slot-side concept of "one stack" (all of them)
		//and not the package's idea of "one stack" (one of them).
		int oneStackFromPackage = Math.min(myOldContents.stack().getMaxStackSize(), myOldContents.count());
		
		int amountToDrop = Math.min(remainingSpaceInSlot, oneStackFromPackage);
		
		ImmutablePackageContents.TakeResult takeResult = myOldContents.withTake(amountToDrop, rules);
		if(takeResult.takenAmount() > 0 && slot.mayPlace(myOldContents.stack())) {
			//add the item into the slot for real
			ItemStack slotInsertionLeftover = slot.safeInsert(myOldContents.stack().copyWithCount(takeResult.takenAmount()));
			
			//check that the entire stack was inserted
			if(!slotInsertionLeftover.isEmpty()) {
				//TODO: what happens if `safeInsert` returns nonempty stack? what cases might this come up in?
				Packages.LOG.warn("Non-empty stack (" + slotInsertionLeftover + ") appeared in dropIntoSlot action from player " + player.getScoreboardName() + ". Item loss or duplication may have happened. Can you file an issue about what caused this? Thanks.");
			}
			
			//sound
			player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
			
			//update my data component
			me.set(ImmutablePackageContents.DATA_COMPONENT_TYPE, takeResult.newContents());
			return true;
		}
		return false;
	}
}
