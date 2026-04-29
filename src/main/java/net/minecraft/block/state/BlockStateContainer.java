package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class BlockStateContainer {

	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
	private static final Function<IProperty<?>, String> GET_NAME_FUNC = new Function<>() {
		
		public String apply(IProperty<?> p_apply_1_) {

			return p_apply_1_ == null ? "<NULL>" : p_apply_1_.getName();
		}
	};
	private final Block block;
	private final ImmutableSortedMap<String, IProperty<?>> properties;
	private final ImmutableList<IBlockState> validStates;

	public BlockStateContainer(Block blockIn, IProperty<?>... properties) {

		block = blockIn;
		Map<String, IProperty<?>> map = Maps.newHashMap();

		for (IProperty<?> iproperty : properties) {
			validateProperty(blockIn, iproperty);
			map.put(iproperty.getName(), iproperty);
		}

		this.properties = ImmutableSortedMap.copyOf(map);
		Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> map2 = Maps.newLinkedHashMap();
		List<BlockStateContainer.StateImplementation> list1 = Lists.newArrayList();

		for (List<Comparable<?>> list : Cartesian.cartesianProduct(getAllowedValues())) {
			Map<IProperty<?>, Comparable<?>> map1 = MapPopulator.createMap(this.properties.values(), list);
			BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation = new BlockStateContainer.StateImplementation(blockIn, ImmutableMap.copyOf(map1));
			map2.put(map1, blockstatecontainer$stateimplementation);
			list1.add(blockstatecontainer$stateimplementation);
		}

		for (BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation1 : list1) {
			blockstatecontainer$stateimplementation1.buildPropertyValueTable(map2);
		}

		validStates = ImmutableList.copyOf(list1);
	}

	public static <T extends Comparable<T>> String validateProperty(Block block, IProperty<T> property) {

		String s = property.getName();

		if (!NAME_PATTERN.matcher(s).matches()) {
			throw new IllegalArgumentException("Block: " + block.getClass() + " has invalidly named property: " + s);
		} else {
			for (T t : property.getAllowedValues()) {
				String s1 = property.getName(t);

				if (!NAME_PATTERN.matcher(s1).matches()) {
					throw new IllegalArgumentException("Block: " + block.getClass() + " has property: " + s + " with invalidly named value: " + s1);
				}
			}

			return s;
		}
	}

	public ImmutableList<IBlockState> getValidStates() {

		return validStates;
	}

	private List<Iterable<Comparable<?>>> getAllowedValues() {

		List<Iterable<Comparable<?>>> list = Lists.newArrayList();
		ImmutableCollection<IProperty<?>> immutablecollection = properties.values();

		for (IProperty<?> iProperty : immutablecollection) {
			list.add(((IProperty) iProperty).getAllowedValues());
		}

		return list;
	}

	public IBlockState getBaseState() {

		return validStates.getFirst();
	}

	public Block getBlock() {

		return block;
	}

	public Collection<IProperty<?>> getProperties() {

		return properties.values();
	}

	public String toString() {

		return MoreObjects.toStringHelper(this).add("block", Block.REGISTRY.getNameForObject(block)).add("properties", Iterables.transform(properties.values(), GET_NAME_FUNC)).toString();
	}

	
	public IProperty<?> getProperty(String propertyName) {

		return properties.get(propertyName);
	}

	static class StateImplementation extends BlockStateBase {

		private final Block block;
		private final ImmutableMap<IProperty<?>, Comparable<?>> properties;
		private ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> propertyValueTable;

		private StateImplementation(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn) {

			block = blockIn;
			properties = propertiesIn;
		}

		public Collection<IProperty<?>> getPropertyKeys() {

			return Collections.unmodifiableCollection(properties.keySet());
		}

		public <T extends Comparable<T>> T getValue(IProperty<T> property) {

			Comparable<?> comparable = properties.get(property);

			if (comparable == null) {
				throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + block.getBlockState());
			} else {
				return property.getValueClass().cast(comparable);
			}
		}

		public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {

			Comparable<?> comparable = properties.get(property);

			if (comparable == null) {
				throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + block.getBlockState());
			} else if (comparable == value) {
				return this;
			} else {
				IBlockState iblockstate = propertyValueTable.get(property, value);

				if (iblockstate == null) {
					throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(block) + ", it is not an allowed value");
				} else {
					return iblockstate;
				}
			}
		}

		public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {

			return properties;
		}

		public Block getBlock() {

			return block;
		}

		public int hashCode() {

			return properties.hashCode();
		}

		public void buildPropertyValueTable(Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> map) {

			if (propertyValueTable != null) {
				throw new IllegalStateException();
			} else {
				Table<IProperty<?>, Comparable<?>, IBlockState> table = HashBasedTable.create();

				for (Entry<IProperty<?>, Comparable<?>> iPropertyComparableEntry : properties.entrySet()) {
					IProperty<?> iproperty = iPropertyComparableEntry.getKey();

					for (Comparable<?> comparable : iproperty.getAllowedValues()) {
						if (comparable != iPropertyComparableEntry.getValue()) {
							table.put(iproperty, comparable, map.get(getPropertiesWithValue(iproperty, comparable)));
						}
					}
				}

				propertyValueTable = ImmutableTable.copyOf(table);
			}
		}

		private Map<IProperty<?>, Comparable<?>> getPropertiesWithValue(IProperty<?> property, Comparable<?> value) {

			Map<IProperty<?>, Comparable<?>> map = Maps.newHashMap(properties);
			map.put(property, value);
			return map;
		}

		public Material getMaterial() {

			return block.getMaterial(this);
		}

		public boolean isFullBlock() {

			return block.isFullBlock(this);
		}

		public boolean canEntitySpawn(Entity entityIn) {

			return block.canEntitySpawn(this, entityIn);
		}

		public int getLightOpacity() {

			return block.getLightOpacity(this);
		}

		public int getLightValue() {

			return block.getLightValue(this);
		}

		public boolean isTranslucent() {

			return block.isTranslucent(this);
		}

		public boolean useNeighborBrightness() {

			return block.getUseNeighborBrightness(this);
		}

		public MapColor getMapColor(IBlockAccess p_185909_1_, BlockPos p_185909_2_) {

			return block.getMapColor(this, p_185909_1_, p_185909_2_);
		}

		public IBlockState withRotation(Rotation rot) {

			return block.withRotation(this, rot);
		}

		public IBlockState withMirror(Mirror mirrorIn) {

			return block.withMirror(this, mirrorIn);
		}

		public boolean isFullCube() {

			return block.isFullCube(this);
		}

		public boolean hasCustomBreakingProgress() {

			return block.hasCustomBreakingProgress(this);
		}

		public BlockRenderType getRenderType() {

			return block.getRenderType(this);
		}

		public int getPackedLightmapCoords(IBlockAccess source, BlockPos pos) {

			return block.getPackedLightmapCoords(this, source, pos);
		}

		public float getAmbientOcclusionLightValue() {

			return block.getAmbientOcclusionLightValue(this);
		}

		public boolean isBlockNormalCube() {

			return block.isBlockNormalCube(this);
		}

		public boolean isNormalCube() {

			return block.isNormalCube(this);
		}

		public boolean canProvidePower() {

			return block.canProvidePower(this);
		}

		public int getWeakPower(IBlockAccess blockAccess, BlockPos pos, Facing side) {

			return block.getWeakPower(this, blockAccess, pos, side);
		}

		public boolean hasComparatorInputOverride() {

			return block.hasComparatorInputOverride(this);
		}

		public int getComparatorInputOverride(World worldIn, BlockPos pos) {

			return block.getComparatorInputOverride(this, worldIn, pos);
		}

		public float getBlockHardness(World worldIn, BlockPos pos) {

			return block.getBlockHardness(this, worldIn, pos);
		}

		public float getPlayerRelativeBlockHardness(EntityPlayer player, World worldIn, BlockPos pos) {

			return block.getPlayerRelativeBlockHardness(this, player, worldIn, pos);
		}

		public int getStrongPower(IBlockAccess blockAccess, BlockPos pos, Facing side) {

			return block.getStrongPower(this, blockAccess, pos, side);
		}

		public PushReaction getMobilityFlag() {

			return block.getMobilityFlag(this);
		}

		public IBlockState getActualState(IBlockAccess blockAccess, BlockPos pos) {

			return block.getActualState(this, blockAccess, pos);
		}

		public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {

			return block.getSelectedBoundingBox(this, worldIn, pos);
		}

		public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, Facing facing) {

			return block.shouldSideBeRendered(this, blockAccess, pos, facing);
		}

		public boolean isOpaqueCube() {

			return block.isOpaqueCube(this);
		}

		
		public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos) {

			return block.getCollisionBoundingBox(this, worldIn, pos);
		}

		public void addCollisionBoxToList(World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185908_6_) {

			block.addCollisionBoxToList(this, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
		}

		public AxisAlignedBB getBoundingBox(IBlockAccess blockAccess, BlockPos pos) {

			return block.getBoundingBox(this, blockAccess, pos);
		}

		public RayTraceResult collisionRayTrace(World worldIn, BlockPos pos, Vec3d start, Vec3d end) {

			return block.collisionRayTrace(this, worldIn, pos, start, end);
		}

		public boolean isTopSolid() {

			return block.isTopSolid(this);
		}

		public Vec3d getOffset(IBlockAccess access, BlockPos pos) {

			return block.getOffset(this, access, pos);
		}

		public boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param) {

			return block.eventReceived(this, worldIn, pos, id, param);
		}

		public void neighborChanged(World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

			block.neighborChanged(this, worldIn, pos, blockIn, fromPos);
		}

		public boolean causesSuffocation() {

			return block.causesSuffocation(this);
		}

		public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockPos pos, Facing facing) {

			return block.getBlockFaceShape(worldIn, this, pos, facing);
		}

	}

}
