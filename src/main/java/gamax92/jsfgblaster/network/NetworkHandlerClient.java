package gamax92.jsfgblaster.network;

import gamax92.jsfgblaster.JSFGBlaster;
import gamax92.jsfgblaster.reference.Config;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import pl.asie.lib.audio.StreamingAudioPlayer;
import pl.asie.lib.network.MessageHandlerBase;
import pl.asie.lib.network.Packet;
import pl.asie.lib.util.WorldUtils;

public class NetworkHandlerClient extends MessageHandlerBase {

	@Override
	public void onMessage(Packet packet, INetHandler handler, EntityPlayer player, int command) throws IOException {
		System.out.println("CLIENT PACKET: " + command);
		switch (command) {
		case Packets.PACKET_CHAN_DATA: {
			int dimId = packet.readInt();
			int x = packet.readInt();
			int y = packet.readInt();
			int z = packet.readInt();
			int packetId = packet.readInt();
			int codecId = packet.readInt();
			short packetSize = packet.readShort();
			short volume = packet.readByte();
			byte[] data = packet.readByteArrayData(packetSize);

			System.out.println("DIMID: " + dimId);
			System.out.println("X: " + x);
			System.out.println("Y: " + y);
			System.out.println("Z: " + z);
			System.out.println("PACKETID: " + packetId);
			System.out.println("CODECID: " + codecId);
			System.out.println("PACKETSIZE: " + packetSize);
			System.out.println("VOLUME: " + volume);
			System.out.println("DATA: " + data.length);
			
			System.out.println("CLIENTID: " + WorldUtils.getCurrentClientDimension());
			
			if (dimId != WorldUtils.getCurrentClientDimension())
				return;
			
			StreamingAudioPlayer codec = JSFGBlaster.instance.audio.getPlayer(codecId);

			if ((codec.lastPacketId + 1) != packetId) {
				codec.reset();
			}
			//codec.setSampleRate(packetSize * 32);
			codec.setSampleRate(44100);
			codec.setDistance((float) Config.SOUNDCARD_DIST);
			codec.setVolume(volume / 127.0F);
			codec.playPacket(data, x, y, z);
			codec.lastPacketId = packetId;
			break;
		}
		case Packets.PACKET_CHAN_STOP: {
			int codecId = packet.readInt();
			JSFGBlaster.instance.audio.removePlayer(codecId);
			break;
		}
		}
	}
}
