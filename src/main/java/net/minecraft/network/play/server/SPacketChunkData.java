package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class SPacketChunkData implements Packet<INetHandlerPlayClient>
{
    private int chunkX;
    private int chunkZ;
    private int availableSections;
    private byte[] buffer;
    private List<NBTTagCompound> tileEntityTags;
    private boolean fullChunk;

    public SPacketChunkData()
    {
    }

    public SPacketChunkData(Chunk chunkIn, int changedSectionFilter)
    {
        chunkX = chunkIn.x;
        chunkZ = chunkIn.z;
        fullChunk = changedSectionFilter == 65535;
        boolean flag = chunkIn.getWorld().provider.hasSkyLight();
        buffer = new byte[calculateChunkSize(chunkIn, flag, changedSectionFilter)];
        availableSections = extractChunkData(new PacketBuffer(getWriteBuffer()), chunkIn, flag, changedSectionFilter);
        tileEntityTags = Lists.<NBTTagCompound>newArrayList();

        for (Entry<BlockPos, TileEntity> entry : chunkIn.getTileEntityMap().entrySet())
        {
            BlockPos blockpos = entry.getKey();
            TileEntity tileentity = entry.getValue();
            int i = blockpos.getY() >> 4;

            if (isFullChunk() || (changedSectionFilter & 1 << i) != 0)
            {
                NBTTagCompound nbttagcompound = tileentity.getUpdateTag();
                tileEntityTags.add(nbttagcompound);
            }
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        fullChunk = buf.readBoolean();
        availableSections = buf.readVarInt();
        int i = buf.readVarInt();

        if (i > 2097152)
        {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        else
        {
            buffer = new byte[i];
            buf.readBytes(buffer);
            int j = buf.readVarInt();
            tileEntityTags = Lists.<NBTTagCompound>newArrayList();

            for (int k = 0; k < j; ++k)
            {
                tileEntityTags.add(buf.readCompoundTag());
            }
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(fullChunk);
        buf.writeVarInt(availableSections);
        buf.writeVarInt(buffer.length);
        buf.writeBytes(buffer);
        buf.writeVarInt(tileEntityTags.size());

        for (NBTTagCompound nbttagcompound : tileEntityTags)
        {
            buf.writeCompoundTag(nbttagcompound);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleChunkData(this);
    }

    public PacketBuffer getReadBuffer()
    {
        return new PacketBuffer(Unpooled.wrappedBuffer(buffer));
    }

    private ByteBuf getWriteBuffer()
    {
        ByteBuf bytebuf = Unpooled.wrappedBuffer(buffer);
        bytebuf.writerIndex(0);
        return bytebuf;
    }

    public int extractChunkData(PacketBuffer buf, Chunk chunkIn, boolean writeSkylight, int changedSectionFilter)
    {
        int i = 0;
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        int j = 0;

        for (int k = aextendedblockstorage.length; j < k; ++j)
        {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];

            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (!isFullChunk() || !extendedblockstorage.isEmpty()) && (changedSectionFilter & 1 << j) != 0)
            {
                i |= 1 << j;
                extendedblockstorage.getData().write(buf);
                buf.writeBytes(extendedblockstorage.getBlockLight().getData());

                if (writeSkylight)
                {
                    buf.writeBytes(extendedblockstorage.getSkyLight().getData());
                }
            }
        }

        if (isFullChunk())
        {
            buf.writeBytes(chunkIn.getBiomeArray());
        }

        return i;
    }

    protected int calculateChunkSize(Chunk chunkIn, boolean p_189556_2_, int p_189556_3_)
    {
        int i = 0;
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        int j = 0;

        for (int k = aextendedblockstorage.length; j < k; ++j)
        {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];

            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (!isFullChunk() || !extendedblockstorage.isEmpty()) && (p_189556_3_ & 1 << j) != 0)
            {
                i = i + extendedblockstorage.getData().getSerializedSize();
                i = i + extendedblockstorage.getBlockLight().getData().length;

                if (p_189556_2_)
                {
                    i += extendedblockstorage.getSkyLight().getData().length;
                }
            }
        }

        if (isFullChunk())
        {
            i += chunkIn.getBiomeArray().length;
        }

        return i;
    }

    public int getChunkX()
    {
        return chunkX;
    }

    public int getChunkZ()
    {
        return chunkZ;
    }

    public int getExtractedSize()
    {
        return availableSections;
    }

    public boolean isFullChunk()
    {
        return fullChunk;
    }

    public List<NBTTagCompound> getTileEntityTags()
    {
        return tileEntityTags;
    }
}
