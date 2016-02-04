package toast.specialMobs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageExplosion implements IMessage {

    public static enum ExplosionType {
        SAFE(0),
        NORMAL(1),
        LIGHTNING(2);

        private static final ExplosionType[] allTypes = new ExplosionType[ExplosionType.values().length];

        private final byte TYPE_ID;

        ExplosionType(int id) {
            this.TYPE_ID = (byte)id;
        }

        // Returns this type's id.
        public byte getId() {
            return this.TYPE_ID;
        }

        // Returns the explosion type with the given id.
        public static ExplosionType getType(byte id) {
            return ExplosionType.allTypes[id % ExplosionType.allTypes.length];
        }

        static {
            // Assign all enum types to an ordered array.
            ExplosionType[] types = ExplosionType.values();
            int length = types.length;

            for (int i = 0; i < length; i++) {
                ExplosionType type = types[i];
                ExplosionType.allTypes[type.getId()] = type;
            }
        }
    }

    // This explosion's type.
    public ExplosionType type;
    // The explosion radius.
    public float size;
    // The explosion coords.
    public double posX, posY, posZ;

    // Array of affected block relative coords. Only used for NORMAL type explosions.
    public byte[][] affectedBlocks;

    public MessageExplosion() {
    }

    public MessageExplosion(Explosion explosion) {
        if (explosion.isSmoking) {
            this.type = ExplosionType.NORMAL;
        }
        else {
            this.type = ExplosionType.SAFE;
        }
        this.size = explosion.explosionSize;
        this.posX = (float)explosion.explosionX;
        this.posY = (float)explosion.explosionY;
        this.posZ = (float)explosion.explosionZ;

        if (this.type == ExplosionType.NORMAL) {
            int count = explosion.affectedBlockPositions.size();
            this.affectedBlocks = new byte[count][];
            int blockX = (int)Math.floor(this.posX);
            int blockY = (int)Math.floor(this.posY);
            int blockZ = (int)Math.floor(this.posZ);
            for (int i = 0; i < count; i++) {
                ChunkPosition pos = (ChunkPosition) explosion.affectedBlockPositions.get(i);
                this.affectedBlocks[i] = new byte[] {
                        (byte)(pos.chunkPosX - blockX), (byte)(pos.chunkPosY - blockY), (byte)(pos.chunkPosZ - blockZ)
                };
            }
        }
    }

    public MessageExplosion(Entity exploder, float size, String type) {
        this(exploder.posX, exploder.posY, exploder.posZ, size, type);
    }
    public MessageExplosion(double posX, double posY, double posZ, float size, String type) {
        if ("lightning".equalsIgnoreCase(type)) {
            this.type = ExplosionType.LIGHTNING;
        }
        this.size = size;
        this.posX = (float) posX;
        this.posY = (float) posY;
        this.posZ = (float) posZ;
    }

    /*
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#fromBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void fromBytes(ByteBuf buf) {
		try {
			this.type = ExplosionType.getType(buf.readByte());
			this.size = buf.readFloat();
			this.posX = buf.readDouble();
			this.posY = buf.readDouble();
			this.posZ = buf.readDouble();

			if (this.type == ExplosionType.NORMAL) {
				int count = buf.readInt();
				this.affectedBlocks = new byte[count][];
				for (int i = 0; i < count; i++) {
					this.affectedBlocks[i] = new byte[] {
							buf.readByte(), buf.readByte(), buf.readByte()
					};
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }

    /*
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#toBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void toBytes(ByteBuf buf) {
		try {
			buf.writeByte(this.type.getId());
			buf.writeFloat(this.size);
			buf.writeDouble(this.posX);
			buf.writeDouble(this.posY);
			buf.writeDouble(this.posZ);

			if (this.type == ExplosionType.NORMAL) {
				int count = this.affectedBlocks.length;
				buf.writeInt(count);
				for (int i = 0; i < count; i++) {
					for (int d = 0; d < 3; d++) {
						buf.writeByte(this.affectedBlocks[i][d]);
					}
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }

    public static class Handler implements IMessageHandler<MessageExplosion, IMessage> {

        /*
         * @see cpw.mods.fml.common.network.simpleimpl.IMessageHandler#onMessage(cpw.mods.fml.common.network.simpleimpl.IMessage, cpw.mods.fml.common.network.simpleimpl.MessageContext)
         */
        @Override
        public IMessage onMessage(MessageExplosion message, MessageContext ctx) {
            World world = FMLClientHandler.instance().getWorldClient();
            if (message.type == ExplosionType.LIGHTNING) {
                if (message.size < 0.0F) {
                    message.size = 0.0F;
                }
                for (float x = -message.size; x <= message.size; x++) {
                    for (float z = -message.size; z <= message.size; z++) {
                        world.spawnEntityInWorld(new EntityLightningBolt(world, message.posX + x, message.posY, message.posZ + z));
                    }
                }
            }
            else {
                if (message.type == ExplosionType.NORMAL && message.size >= 2.0F) {
                    world.spawnParticle("hugeexplosion", message.posX, message.posY, message.posZ, 1.0, 0.0, 0.0);
                }
                else {
                    world.spawnParticle("largeexplode", message.posX, message.posY, message.posZ, 1.0, 0.0, 0.0);
                }

                if (message.type == ExplosionType.NORMAL && message.affectedBlocks != null) {
                    int count = message.affectedBlocks.length;
                    double[] relPos;
                    double fxPosX, fxPosY, fxPosZ;
                    for (int i = 0; i < count; i++) {
                        relPos = new double[3];
                        for (int d = 0; d < 3; d++) {
                            relPos[d] = message.affectedBlocks[i][d] + world.rand.nextFloat();
                        }
                        fxPosX = relPos[0] + message.posX;
                        fxPosY = relPos[1] + message.posY;
                        fxPosZ = relPos[2] + message.posZ;
                        double velo = Math.sqrt(relPos[0] * relPos[0] + relPos[1] * relPos[1] + relPos[2] * relPos[2]);
                        double mult = 0.5 / (velo / message.size + 0.1) * (world.rand.nextFloat() * world.rand.nextFloat() + 0.3F) / velo;
                        for (int d = 0; d < 3; d++) {
                            relPos[d] *= mult;
                        }
                        world.spawnParticle("explode", (fxPosX + message.posX) / 2.0, (fxPosY + message.posY) / 2.0, (fxPosZ + message.posZ) / 2.0, relPos[0], relPos[1], relPos[2]);
                        world.spawnParticle("smoke", fxPosX, fxPosY, fxPosZ, relPos[0], relPos[1], relPos[2]);
                    }
                }
            }
            return null;
        }

    }
}
