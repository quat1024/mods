package agency.highlysuspect.quatlib.craftless.util;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum TwelveDirection implements StringRepresentable {
	UP_NORTH(Direction.UP, Direction.NORTH),
	UP_SOUTH(Direction.UP, Direction.SOUTH),
	UP_EAST(Direction.UP, Direction.EAST),
	UP_WEST(Direction.UP, Direction.WEST),
	NORTH(Direction.NORTH, null),
	SOUTH(Direction.SOUTH, null),
	EAST(Direction.EAST, null),
	WEST(Direction.WEST, null),
	DOWN_NORTH(Direction.DOWN, Direction.NORTH),
	DOWN_SOUTH(Direction.DOWN, Direction.SOUTH),
	DOWN_EAST(Direction.DOWN, Direction.EAST),
	DOWN_WEST(Direction.DOWN, Direction.WEST);
	
	TwelveDirection(@NotNull Direction primaryDirection, @Nullable Direction secondaryDirection) {
		this.primaryDirection = primaryDirection;
		this.secondaryDirection = secondaryDirection;
	}
	
	public final @NotNull Direction primaryDirection;
	public final @Nullable Direction secondaryDirection;
	
	public static TwelveDirection get(Direction primary, @Nullable Direction secondary) {
		//this is j17 language level, "case null" doesn't work yet...
		if(primary == Direction.UP && secondary == null) return UP_NORTH;
		if(primary == Direction.DOWN && secondary == null) return DOWN_NORTH;
		
		return switch(primary) {
			case NORTH -> TwelveDirection.NORTH;
			case SOUTH -> TwelveDirection.SOUTH;
			case EAST -> TwelveDirection.EAST;
			case WEST -> TwelveDirection.WEST;
			case UP -> switch(secondary) {
				case SOUTH -> TwelveDirection.UP_SOUTH;
				case EAST -> TwelveDirection.UP_EAST;
				case WEST -> TwelveDirection.UP_WEST;
				default -> TwelveDirection.UP_NORTH;
			};
			case DOWN -> switch (secondary) {
				case SOUTH -> TwelveDirection.DOWN_SOUTH;
				case EAST -> TwelveDirection.DOWN_EAST;
				case WEST -> TwelveDirection.DOWN_WEST;
				default -> TwelveDirection.DOWN_NORTH;
			};
		};
	}
	
	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
