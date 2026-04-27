package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MapData extends WorldSavedData
{
    public int xCenter;
    public int zCenter;
    public byte dimension;
    public boolean trackingPosition;
    public boolean unlimitedTracking;
    public byte scale;

    /**
     * A flattened 128x128 grid representing the contents of the map. Each byte has format ([index into MapColor.COLORS]
     * << 4 | brightness flag)
     */
    public byte[] colors = new byte[16384];
    public List<MapData.MapInfo> playersArrayList = Lists.<MapData.MapInfo>newArrayList();
    private final Map<EntityPlayer, MapData.MapInfo> playersHashMap = Maps.<EntityPlayer, MapData.MapInfo>newHashMap();
    public Map<String, MapDecoration> mapDecorations = Maps.<String, MapDecoration>newLinkedHashMap();

    public MapData(String mapname)
    {
        super(mapname);
    }

    public void calculateMapCenter(double x, double z, int mapScale)
    {
        int i = 128 * (1 << mapScale);
        int j = MathHelper.floor((x + 64.0D) / (double)i);
        int k = MathHelper.floor((z + 64.0D) / (double)i);
        xCenter = j * i + i / 2 - 64;
        zCenter = k * i + i / 2 - 64;
    }

    /**
     * reads in data from the NBTTagCompound into this MapDataBase
     */
    public void readFromNBT(NBTTagCompound nbt)
    {
        dimension = nbt.getByte("dimension");
        xCenter = nbt.getInteger("xCenter");
        zCenter = nbt.getInteger("zCenter");
        scale = nbt.getByte("scale");
        scale = (byte)MathHelper.clamp(scale, 0, 4);

        if (nbt.hasKey("trackingPosition", 1))
        {
            trackingPosition = nbt.getBoolean("trackingPosition");
        }
        else
        {
            trackingPosition = true;
        }

        unlimitedTracking = nbt.getBoolean("unlimitedTracking");
        int i = nbt.getShort("width");
        int j = nbt.getShort("height");

        if (i == 128 && j == 128)
        {
            colors = nbt.getByteArray("colors");
        }
        else
        {
            byte[] abyte = nbt.getByteArray("colors");
            colors = new byte[16384];
            int k = (128 - i) / 2;
            int l = (128 - j) / 2;

            for (int i1 = 0; i1 < j; ++i1)
            {
                int j1 = i1 + l;

                if (j1 >= 0 || j1 < 128)
                {
                    for (int k1 = 0; k1 < i; ++k1)
                    {
                        int l1 = k1 + k;

                        if (l1 >= 0 || l1 < 128)
                        {
                            colors[l1 + j1 * 128] = abyte[k1 + i1 * i];
                        }
                    }
                }
            }
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound.setByte("dimension", dimension);
        compound.setInteger("xCenter", xCenter);
        compound.setInteger("zCenter", zCenter);
        compound.setByte("scale", scale);
        compound.setShort("width", (short)128);
        compound.setShort("height", (short)128);
        compound.setByteArray("colors", colors);
        compound.setBoolean("trackingPosition", trackingPosition);
        compound.setBoolean("unlimitedTracking", unlimitedTracking);
        return compound;
    }

    /**
     * Adds the player passed to the list of visible players and checks to see which players are visible
     */
    public void updateVisiblePlayers(EntityPlayer player, ItemStack mapStack)
    {
        if (!playersHashMap.containsKey(player))
        {
            MapData.MapInfo mapdata$mapinfo = new MapData.MapInfo(player);
            playersHashMap.put(player, mapdata$mapinfo);
            playersArrayList.add(mapdata$mapinfo);
        }

        if (!player.inventory.hasItemStack(mapStack))
        {
            mapDecorations.remove(player.getName());
        }

        for (int i = 0; i < playersArrayList.size(); ++i)
        {
            MapData.MapInfo mapdata$mapinfo1 = playersArrayList.get(i);

            if (!mapdata$mapinfo1.player.isDead && (mapdata$mapinfo1.player.inventory.hasItemStack(mapStack) || mapStack.isOnItemFrame()))
            {
                if (!mapStack.isOnItemFrame() && mapdata$mapinfo1.player.dimension == dimension && trackingPosition)
                {
                    updateDecorations(MapDecoration.Type.PLAYER, mapdata$mapinfo1.player.world, mapdata$mapinfo1.player.getName(), mapdata$mapinfo1.player.posX, mapdata$mapinfo1.player.posZ, (double)mapdata$mapinfo1.player.rotationYaw);
                }
            }
            else
            {
                playersHashMap.remove(mapdata$mapinfo1.player);
                playersArrayList.remove(mapdata$mapinfo1);
            }
        }

        if (mapStack.isOnItemFrame() && trackingPosition)
        {
            EntityItemFrame entityitemframe = mapStack.getItemFrame();
            BlockPos blockpos = entityitemframe.getHangingPosition();
            updateDecorations(MapDecoration.Type.FRAME, player.world, "frame-" + entityitemframe.getEntityId(), (double)blockpos.getX(), (double)blockpos.getZ(), (double)(entityitemframe.facingDirection.getHorizontalIndex() * 90));
        }

        if (mapStack.hasTagCompound() && mapStack.getTagCompound().hasKey("Decorations", 9))
        {
            NBTTagList nbttaglist = mapStack.getTagCompound().getTagList("Decorations", 10);

            for (int j = 0; j < nbttaglist.tagCount(); ++j)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(j);

                if (!mapDecorations.containsKey(nbttagcompound.getString("id")))
                {
                    updateDecorations(MapDecoration.Type.byIcon(nbttagcompound.getByte("type")), player.world, nbttagcompound.getString("id"), nbttagcompound.getDouble("x"), nbttagcompound.getDouble("z"), nbttagcompound.getDouble("rot"));
                }
            }
        }
    }

    public static void addTargetDecoration(ItemStack map, BlockPos target, String decorationName, MapDecoration.Type type)
    {
        NBTTagList nbttaglist;

        if (map.hasTagCompound() && map.getTagCompound().hasKey("Decorations", 9))
        {
            nbttaglist = map.getTagCompound().getTagList("Decorations", 10);
        }
        else
        {
            nbttaglist = new NBTTagList();
            map.setTagInfo("Decorations", nbttaglist);
        }

        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setByte("type", type.getIcon());
        nbttagcompound.setString("id", decorationName);
        nbttagcompound.setDouble("x", (double)target.getX());
        nbttagcompound.setDouble("z", (double)target.getZ());
        nbttagcompound.setDouble("rot", 180.0D);
        nbttaglist.appendTag(nbttagcompound);

        if (type.hasMapColor())
        {
            NBTTagCompound nbttagcompound1 = map.getOrCreateSubCompound("display");
            nbttagcompound1.setInteger("MapColor", type.getMapColor());
        }
    }

    private void updateDecorations(MapDecoration.Type type, World worldIn, String decorationName, double worldX, double worldZ, double rotationIn)
    {
        int i = 1 << scale;
        float f = (float)(worldX - (double) xCenter) / (float)i;
        float f1 = (float)(worldZ - (double) zCenter) / (float)i;
        byte b0 = (byte)((int)((double)(f * 2.0F) + 0.5D));
        byte b1 = (byte)((int)((double)(f1 * 2.0F) + 0.5D));
        int j = 63;
        byte b2;

        if (f >= -63.0F && f1 >= -63.0F && f <= 63.0F && f1 <= 63.0F)
        {
            rotationIn = rotationIn + (rotationIn < 0.0D ? -8.0D : 8.0D);
            b2 = (byte)((int)(rotationIn * 16.0D / 360.0D));

            if (dimension < 0)
            {
                int l = (int)(worldIn.getWorldInfo().getWorldTime() / 10L);
                b2 = (byte)(l * l * 34187121 + l * 121 >> 15 & 15);
            }
        }
        else
        {
            if (type != MapDecoration.Type.PLAYER)
            {
                mapDecorations.remove(decorationName);
                return;
            }

            int k = 320;

            if (Math.abs(f) < 320.0F && Math.abs(f1) < 320.0F)
            {
                type = MapDecoration.Type.PLAYER_OFF_MAP;
            }
            else
            {
                if (!unlimitedTracking)
                {
                    mapDecorations.remove(decorationName);
                    return;
                }

                type = MapDecoration.Type.PLAYER_OFF_LIMITS;
            }

            b2 = 0;

            if (f <= -63.0F)
            {
                b0 = -128;
            }

            if (f1 <= -63.0F)
            {
                b1 = -128;
            }

            if (f >= 63.0F)
            {
                b0 = 127;
            }

            if (f1 >= 63.0F)
            {
                b1 = 127;
            }
        }

        mapDecorations.put(decorationName, new MapDecoration(type, b0, b1, b2));
    }

    @Nullable
    public Packet<?> getMapPacket(ItemStack mapStack, World worldIn, EntityPlayer player)
    {
        MapData.MapInfo mapdata$mapinfo = playersHashMap.get(player);
        return mapdata$mapinfo == null ? null : mapdata$mapinfo.getPacket(mapStack);
    }

    public void updateMapData(int x, int y)
    {
        super.markDirty();

        for (MapData.MapInfo mapdata$mapinfo : playersArrayList)
        {
            mapdata$mapinfo.update(x, y);
        }
    }

    public MapData.MapInfo getMapInfo(EntityPlayer player)
    {
        MapData.MapInfo mapdata$mapinfo = playersHashMap.get(player);

        if (mapdata$mapinfo == null)
        {
            mapdata$mapinfo = new MapData.MapInfo(player);
            playersHashMap.put(player, mapdata$mapinfo);
            playersArrayList.add(mapdata$mapinfo);
        }

        return mapdata$mapinfo;
    }

    public class MapInfo
    {
        public final EntityPlayer player;
        private boolean isDirty = true;
        private int minX;
        private int minY;
        private int maxX = 127;
        private int maxY = 127;
        private int tick;
        public int step;

        public MapInfo(EntityPlayer player)
        {
            this.player = player;
        }

        @Nullable
        public Packet<?> getPacket(ItemStack stack)
        {
            if (isDirty)
            {
                isDirty = false;
                return new SPacketMaps(stack.getMetadata(), scale, trackingPosition, mapDecorations.values(), colors, minX, minY, maxX + 1 - minX, maxY + 1 - minY);
            }
            else
            {
                return tick++ % 5 == 0 ? new SPacketMaps(stack.getMetadata(), scale, trackingPosition, mapDecorations.values(), colors, 0, 0, 0, 0) : null;
            }
        }

        public void update(int x, int y)
        {
            if (isDirty)
            {
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
            else
            {
                isDirty = true;
                minX = x;
                minY = y;
                maxX = x;
                maxY = y;
            }
        }
    }
}
