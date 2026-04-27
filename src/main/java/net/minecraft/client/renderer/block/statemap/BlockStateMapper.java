package net.minecraft.client.renderer.block.statemap;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BlockStateMapper {

	private final Map<Block, IStateMapper> blockStateMap = Maps.newIdentityHashMap();
	private final Set<Block> setBuiltInBlocks = Sets.newIdentityHashSet();

	public void registerBlockStateMapper(Block blockIn, IStateMapper stateMapper) {

		blockStateMap.put(blockIn, stateMapper);
	}

	public void registerBuiltInBlocks(Block... blockIn) {

		Collections.addAll(setBuiltInBlocks, blockIn);
	}

	public Map<IBlockState, ModelResourceLocation> putAllStateModelLocations() {

		Map<IBlockState, ModelResourceLocation> map = Maps.newIdentityHashMap();

		for (Block block : Block.REGISTRY) {
			map.putAll(getVariants(block));
		}

		return map;
	}

	public Set<ResourceLocation> getBlockstateLocations(Block blockIn) {

		if (setBuiltInBlocks.contains(blockIn)) {
			return Collections.emptySet();
		} else {
			IStateMapper istatemapper = blockStateMap.get(blockIn);

			if (istatemapper == null) {
				return Collections.singleton(Block.REGISTRY.getNameForObject(blockIn));
			} else {
				Set<ResourceLocation> set = Sets.newHashSet();

				for (ModelResourceLocation modelresourcelocation : istatemapper.putStateModelLocations(blockIn).values()) {
					set.add(new ResourceLocation(modelresourcelocation.getResourceDomain(), modelresourcelocation.getResourcePath()));
				}

				return set;
			}
		}
	}

	public Map<IBlockState, ModelResourceLocation> getVariants(Block blockIn) {

		return setBuiltInBlocks.contains(blockIn) ? Collections.emptyMap() : MoreObjects.firstNonNull(blockStateMap.get(blockIn), new DefaultStateMapper()).putStateModelLocations(blockIn);
	}

}
