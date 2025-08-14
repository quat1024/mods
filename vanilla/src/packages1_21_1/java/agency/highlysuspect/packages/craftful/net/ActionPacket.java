package agency.highlysuspect.packages.craftful.net;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ActionPacket {
	public static final ResourceLocation LONG_ID = Packages.rl("action"); //used on fabric
	public static final byte SHORT_ID = 0; //used on forge
	
	public ActionPacket() {}
	
	public ActionPacket(BlockPos pos, InteractionHand hand, PackageAction action) {
		this.pos = pos;
		this.hand = hand;
		this.action = action;
	}
	
	public BlockPos pos;
	public InteractionHand hand;
	public PackageAction action;
	
	public static <T extends ActionPacket> StreamCodec<FriendlyByteBuf, T> createStreamCodec(Supplier<T> s) {
		return new StreamCodec<>() {
			@Override
			public T decode(FriendlyByteBuf buf) {
				T pk = s.get();
				pk.readMutate(buf);
				return pk;
			}
			
			@Override
			public void encode(FriendlyByteBuf buf, T pk) {
				pk.write(buf);
			}
		};
	}
	
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
		action.write(buf);
	}
	
	public void readMutate(FriendlyByteBuf buf) {
		pos = buf.readBlockPos();
		hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		action = PackageAction.read(buf);
	}
	
	public void handle(ServerPlayer sender) {
		sender.server.submit(() -> {
			PackageBlockEntity be = getPackageChecked(sender.level(), sender, pos);
			if(be != null) be.performAction(sender, hand, action, false);
		});
	}
	
	@SuppressWarnings("deprecation") //hasChunkAt
	private static @Nullable PackageBlockEntity getPackageChecked(Level level, Player player, BlockPos pos) {
		if(!level.hasChunkAt(pos) || player.blockPosition().distSqr(pos) > 8 * 8) return null;
		if(!(level.getBlockState(pos).getBlock() instanceof PackageBlock)) return null;
		return level.getBlockEntity(pos) instanceof PackageBlockEntity pbe ? pbe : null;
	}
}
