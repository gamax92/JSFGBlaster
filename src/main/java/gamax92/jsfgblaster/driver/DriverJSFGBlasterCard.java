package gamax92.jsfgblaster.driver;

import java.io.IOException;

import pl.asie.lib.network.Packet;
import gamax92.jsfgblaster.JSFGBlaster;
import gamax92.jsfgblaster.network.Packets;
import gamax92.jsfgblaster.reference.Config;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.EnvironmentHost;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import sfxr.SFXRData;

public class DriverJSFGBlasterCard extends DriverItem {
	public DriverJSFGBlasterCard() {
		super(new ItemStack(JSFGBlaster.jsfgblasterCard));
	}

	@Override
	public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost container) {
		if (container instanceof TileEntity)
			return new Environment((TileEntity) container);
		return null;
	}

	@Override
	public String slot(ItemStack stack) {
		return Slot.Card;
	}

	public class Environment extends li.cil.oc.api.prefab.ManagedEnvironment {
		protected final TileEntity container;

		private SFXRData[] sfxr = new SFXRData[4];
		private int[] clocks = new int[] { 0, 0, 0, 0 };
		private int[] codecID = new int[] { 0, 0, 0, 0 };
		private int[] packetID = new int[] { 0, 0, 0, 0 };

		public Environment(TileEntity container) {
			System.out.println(container.getClass().getSimpleName());
			this.container = container;
			this.setNode(Network.newNode(this, Visibility.Neighbors).withComponent("jsfgblaster").create());
			for (int i = 0; i < 4; i++) {
				sfxr[i] = new SFXRData((int) (System.currentTimeMillis() + i));
				sfxr[i].playing_sample = false;
			}
		}

		@Callback
		public Object[] playChan(Context context, Arguments args) {
			int snd = args.checkInteger(0) - 1;
			if (snd > 3 || snd < 0)
				return new Object[] { false, "no channel " + (snd + 1) };
			//System.out.println("PLAYCHAN PLAYING: " + sfxr[snd].playing_sample);
			if (!sfxr[snd].playing_sample) {
				//System.out.println("PLAYCHAN PLAYING: " + sfxr[snd].playing_sample);
				codecID[snd] = JSFGBlaster.instance.audio.newPlayer();
				JSFGBlaster.instance.audio.getPlayer(codecID[snd]);
				packetID[snd] = 0;
			}
			sfxr[snd].resetSample(false);
			//System.out.println("RESET PLAYING: " + sfxr[snd].playing_sample);
			sfxr[snd].playing_sample = true;
			//System.out.println("FORCE PLAYING: " + sfxr[snd].playing_sample);
			clocks[snd] = 0;
			return new Object[] { true };
		}

		@Callback
		public Object[] stopChan(Context context, Arguments args) {
			int snd = args.checkInteger(0) - 1;
			if (snd > 3 || snd < 0)
				return new Object[] { false, "no channel " + (snd + 1) };
			if (sfxr[snd].playing_sample) {
				sfxr[snd].playing_sample = false;
				JSFGBlaster.instance.audio.removePlayer(codecID[snd]);
				try {
					Packet packet = JSFGBlaster.packet.create(Packets.PACKET_CHAN_STOP).writeInt(codecID[snd]);
					JSFGBlaster.packet.sendToAll(packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return new Object[] { true };
		}

		@Callback
		public Object[] resetChan(Context context, Arguments args) {
			int snd = args.checkInteger(0) - 1;
			if (snd > 3 || snd < 0)
				return new Object[] { false, "no channel " + (snd + 1) };
			if (args.count() >= 2)
				sfxr[snd].random(args.checkInteger(1));
			else
				sfxr[snd].resetParams();

			if (sfxr[snd].playing_sample) {
				sfxr[snd].playing_sample = false;
				JSFGBlaster.instance.audio.removePlayer(codecID[snd]);
				try {
					Packet packet = JSFGBlaster.packet.create(Packets.PACKET_CHAN_STOP).writeInt(codecID[snd]);
					JSFGBlaster.packet.sendToAll(packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return new Object[] { true };
		}

		private void generateMusicPacket(int snd) {
			int packetSize = Config.SAMPLES_PER_TICK * Config.PACKETS_EVERY_TICK;
			byte[] data = new byte[packetSize];
			for (int i = 0; i < packetSize; i++) {
				data[i] = (byte) ((sfxr[snd].synthSample() + 1.0D) * (255.0D / 2.0D));
			}
			try {
				Packet pkt = JSFGBlaster.packet.create(Packets.PACKET_CHAN_DATA);
				pkt.writeInt(this.container.getWorldObj().provider.dimensionId);
				pkt.writeInt(this.container.xCoord);
				pkt.writeInt(this.container.yCoord);
				pkt.writeInt(this.container.zCoord);
				pkt.writeInt(packetID[snd]++);
				pkt.writeInt(codecID[snd]);
				pkt.writeShort((short) packetSize);
				pkt.writeByte((byte) 127);
				pkt.writeByteArrayData(data);

				JSFGBlaster.packet.sendToAllAround(pkt, this.container, 64.0D);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!sfxr[snd].playing_sample) {
				try {
					Packet packet = JSFGBlaster.packet.create(Packets.PACKET_CHAN_STOP).writeTileLocation(this.container).writeInt(snd);
					//JSFGBlaster.packet.sendToAllAround(packet, this.container, 64.0D);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public boolean canUpdate() {
			return true;
		}

		@Override
		public void update() {
			super.update();
			if (!this.container.getWorldObj().isRemote) {
				for (int i = 0; i < 4; i++) {
					//System.out.println("TEST PLAYING: " + sfxr[i].playing_sample);
					if (sfxr[i].playing_sample) {
						if (clocks[i] % Config.PACKETS_EVERY_TICK == 0)
							generateMusicPacket(i);
						clocks[i]++;
					}
				}
			}
		}
	}
}
