package agency.highlysuspect.packages.craftful.net;

import agency.highlysuspect.packages.craftful.content.PLatches;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

public enum PackageAction {
	//Insert one item into the package.
	INSERT_ONE,
	
	//Insert up to one stack of items into the package.
	INSERT_STACK,
	
	//Insert all matching items in the player's inventory into the package.
	INSERT_ALL,
	
	//Take one item out of the package.
	TAKE_ONE,
	
	//Take a stack of items out of the package.
	TAKE_STACK,
	
	//Take everything out of the package.
	TAKE_ALL,
	;
	
	public boolean isInsert() {
		return this == INSERT_ONE || this == INSERT_STACK || this == INSERT_ALL;
	}
	
	public void write(FriendlyByteBuf buf) {
		buf.writeByte(ordinal());
	}
	
	public static PackageAction read(FriendlyByteBuf buf) {
		return get(buf.readByte());
	}
	
	public static PackageAction get(int netValue) {
		if(netValue >= 0 && netValue < PackageAction.values().length) return PackageAction.values()[netValue];
		else return TAKE_ONE; //shrug
	}
	
	public SoundEvent getSoundEvent() {
		return (switch(this) {
			case INSERT_ONE -> PLatches.SoundEvents.INSERT_ONE;
			case TAKE_ONE -> PLatches.SoundEvents.TAKE_ONE;
			case INSERT_STACK -> PLatches.SoundEvents.INSERT_STACK;
			case TAKE_STACK -> PLatches.SoundEvents.TAKE_STACK;
			case INSERT_ALL -> PLatches.SoundEvents.INSERT_ALL;
			case TAKE_ALL -> PLatches.SoundEvents.TAKE_ALL;
		}).get();
	}
	
	public float getSoundVolume() {
		return 1f; //baked in to the sound event
	}
	
	public float getSoundPitch(Level level) {
		//reh reh...
		return switch(this) {
			case TAKE_ONE -> (level.random.nextFloat() - level.random.nextFloat()) * 1.4f + 2f; //vanilla item pickup sound variation
			case TAKE_STACK -> level.random.nextFloat() * 0.1f + 0.7f; //lower pitch
			default -> 1;
		};
	}
}