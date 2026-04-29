package net.minecraft.block;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

/**
 * @param breakSound The sound played when a block gets broken.
 * @param stepSound  The sound played when walking on a block.
 * @param placeSound The sound played when a block gets placed.
 * @param hitSound   The sound played when a block gets hit (i.e. while mining).
 * @param fallSound  The sound played when a block gets fallen upon.
 */
public record SoundType(float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound,
                        SoundEvent hitSound, SoundEvent fallSound) {

	public static final SoundType WOOD = new SoundType(1F, 1F, SoundEvents.BLOCK_WOOD_BREAK, SoundEvents.BLOCK_WOOD_STEP, SoundEvents.BLOCK_WOOD_PLACE, SoundEvents.BLOCK_WOOD_HIT, SoundEvents.BLOCK_WOOD_FALL);
	public static final SoundType GROUND = new SoundType(1F, 1F, SoundEvents.BLOCK_GRAVEL_BREAK, SoundEvents.BLOCK_GRAVEL_STEP, SoundEvents.BLOCK_GRAVEL_PLACE, SoundEvents.BLOCK_GRAVEL_HIT, SoundEvents.BLOCK_GRAVEL_FALL);
	public static final SoundType PLANT = new SoundType(1F, 1F, SoundEvents.BLOCK_GRASS_BREAK, SoundEvents.BLOCK_GRASS_STEP, SoundEvents.BLOCK_GRASS_PLACE, SoundEvents.BLOCK_GRASS_HIT, SoundEvents.BLOCK_GRASS_FALL);
	public static final SoundType STONE = new SoundType(1F, 1F, SoundEvents.BLOCK_STONE_BREAK, SoundEvents.BLOCK_STONE_STEP, SoundEvents.BLOCK_STONE_PLACE, SoundEvents.BLOCK_STONE_HIT, SoundEvents.BLOCK_STONE_FALL);
	public static final SoundType METAL = new SoundType(1F, 1.5F, SoundEvents.BLOCK_METAL_BREAK, SoundEvents.BLOCK_METAL_STEP, SoundEvents.BLOCK_METAL_PLACE, SoundEvents.BLOCK_METAL_HIT, SoundEvents.BLOCK_METAL_FALL);
	public static final SoundType GLASS = new SoundType(1F, 1F, SoundEvents.BLOCK_GLASS_BREAK, SoundEvents.BLOCK_GLASS_STEP, SoundEvents.BLOCK_GLASS_PLACE, SoundEvents.BLOCK_GLASS_HIT, SoundEvents.BLOCK_GLASS_FALL);
	public static final SoundType CLOTH = new SoundType(1F, 1F, SoundEvents.BLOCK_CLOTH_BREAK, SoundEvents.BLOCK_CLOTH_STEP, SoundEvents.BLOCK_CLOTH_PLACE, SoundEvents.BLOCK_CLOTH_HIT, SoundEvents.BLOCK_CLOTH_FALL);
	public static final SoundType SAND = new SoundType(1F, 1F, SoundEvents.BLOCK_SAND_BREAK, SoundEvents.BLOCK_SAND_STEP, SoundEvents.BLOCK_SAND_PLACE, SoundEvents.BLOCK_SAND_HIT, SoundEvents.BLOCK_SAND_FALL);
	public static final SoundType SNOW = new SoundType(1F, 1F, SoundEvents.BLOCK_SNOW_BREAK, SoundEvents.BLOCK_SNOW_STEP, SoundEvents.BLOCK_SNOW_PLACE, SoundEvents.BLOCK_SNOW_HIT, SoundEvents.BLOCK_SNOW_FALL);
	public static final SoundType LADDER = new SoundType(1F, 1F, SoundEvents.BLOCK_LADDER_BREAK, SoundEvents.BLOCK_LADDER_STEP, SoundEvents.BLOCK_LADDER_PLACE, SoundEvents.BLOCK_LADDER_HIT, SoundEvents.BLOCK_LADDER_FALL);
	public static final SoundType ANVIL = new SoundType(0.3F, 1F, SoundEvents.BLOCK_ANVIL_BREAK, SoundEvents.BLOCK_ANVIL_STEP, SoundEvents.BLOCK_ANVIL_PLACE, SoundEvents.BLOCK_ANVIL_HIT, SoundEvents.BLOCK_ANVIL_FALL);
	public static final SoundType SLIME = new SoundType(1F, 1F, SoundEvents.BLOCK_SLIME_BREAK, SoundEvents.BLOCK_SLIME_STEP, SoundEvents.BLOCK_SLIME_PLACE, SoundEvents.BLOCK_SLIME_HIT, SoundEvents.BLOCK_SLIME_FALL);

}
