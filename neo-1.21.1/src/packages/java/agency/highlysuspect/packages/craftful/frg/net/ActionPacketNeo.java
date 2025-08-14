package agency.highlysuspect.packages.craftful.frg.net;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ActionPacketNeo extends ActionPacket implements CustomPacketPayload {
	public ActionPacketNeo() {
	}
	
	public ActionPacketNeo(ActionPacket base) {
		super(base.pos, base.hand, base.action);
	}
	
	public static final Type<ActionPacketNeo> TYPE = new Type<>(Packages.rl("a"));
	public static final StreamCodec<FriendlyByteBuf, ActionPacketNeo> STREAM_CODEC = ActionPacket.createStreamCodec(ActionPacketNeo::new);
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
