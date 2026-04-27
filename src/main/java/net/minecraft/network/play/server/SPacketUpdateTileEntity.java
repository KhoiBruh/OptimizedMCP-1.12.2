package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

public class SPacketUpdateTileEntity implements Packet<INetHandlerPlayClient>
{
    private BlockPos blockPos;

    /** Used only for vanilla tile entities */
    private int tileEntityType;
    private NBTTagCompound nbt;

    public SPacketUpdateTileEntity()
    {
    }

    public SPacketUpdateTileEntity(BlockPos blockPosIn, int tileEntityTypeIn, NBTTagCompound compoundIn)
    {
        blockPos = blockPosIn;
        tileEntityType = tileEntityTypeIn;
        nbt = compoundIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        blockPos = buf.readBlockPos();
        tileEntityType = buf.readUnsignedByte();
        nbt = buf.readCompoundTag();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeBlockPos(blockPos);
        buf.writeByte((byte) tileEntityType);
        buf.writeCompoundTag(nbt);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleUpdateTileEntity(this);
    }

    public BlockPos getPos()
    {
        return blockPos;
    }

    public int getTileEntityType()
    {
        return tileEntityType;
    }

    public NBTTagCompound getNbtCompound()
    {
        return nbt;
    }
}
