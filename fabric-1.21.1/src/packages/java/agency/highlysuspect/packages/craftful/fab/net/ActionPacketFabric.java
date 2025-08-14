package agency.highlysuspect.packages.craftful.fab.net;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftful.net.PackageAction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public class ActionPacketFabric extends ActionPacket implements CustomPacketPayload {
	public ActionPacketFabric() {
	}
	
	public ActionPacketFabric(BlockPos pos, InteractionHand hand, PackageAction action) {
		super(pos, hand, action);
	}
	
	public ActionPacketFabric(ActionPacket cpy) {
		this(cpy.pos, cpy.hand, cpy.action);
	}
	
	public static final Type<ActionPacketFabric> TYPE = new Type<>(Packages.rl("a"));
	public static final StreamCodec<FriendlyByteBuf, ActionPacketFabric> STREAM_CODEC = ActionPacket.createStreamCodec(ActionPacketFabric::new);
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
