package agency.highlysuspect.packages.craftful.junk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PackageStyle(@NotNull Block frameBlock, @NotNull Block innerBlock, @NotNull DyeColor color) {
	public static final PackageStyle ERROR_LOL = new PackageStyle(Blocks.PINK_CONCRETE, Blocks.BLACK_CONCRETE, DyeColor.RED);
	
	public static final Codec<PackageStyle> CODEC = RecordCodecBuilder.create(i -> i.group(
		ResourceLocation.CODEC.xmap(BuiltInRegistries.BLOCK::get, BuiltInRegistries.BLOCK::getKey).fieldOf("frame").forGetter(PackageStyle::frameBlock),
		ResourceLocation.CODEC.xmap(BuiltInRegistries.BLOCK::get, BuiltInRegistries.BLOCK::getKey).fieldOf("inner").forGetter(PackageStyle::innerBlock),
		DyeColor.CODEC.fieldOf("color").forGetter(PackageStyle::color)
	).apply(i, PackageStyle::new));
	
	public static final StreamCodec<? super RegistryFriendlyByteBuf, PackageStyle> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(RegistryFriendlyByteBuf buf, PackageStyle style) {
			//TODO: is there a StreamCodec for blocks? what's the best way to do this
			buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(style.frameBlock)));
			buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(style.innerBlock)));
			buf.writeByte(style.color.ordinal());
		}
		
		@Override
		public PackageStyle decode(RegistryFriendlyByteBuf buf) {
			return new PackageStyle(
				Objects.requireNonNull(BuiltInRegistries.BLOCK.get(buf.readResourceLocation())),
				Objects.requireNonNull(BuiltInRegistries.BLOCK.get(buf.readResourceLocation())),
				DyeColor.byId(buf.readByte()) //does the clamping itself
			);
		}
	};
	
	public static final DataComponentType<PackageStyle> DATA_COMPONENT_TYPE =
		new DataComponentType.Builder<PackageStyle>().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();
	
	//styled after PotDecorations
	public CompoundTag save(CompoundTag in) {
		in.put("PackageStyle", CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
		return in;
	}
	
	public static PackageStyle load(@Nullable CompoundTag in) {
		if(in == null) return ERROR_LOL;
		Tag t = in.get("PackageStyle");
		if(t == null) return null;
		else return CODEC.parse(NbtOps.INSTANCE, t).result().orElse(ERROR_LOL);
	}
}
