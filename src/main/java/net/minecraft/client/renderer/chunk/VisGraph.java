package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Queues;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntegerCache;
import net.minecraft.util.math.BlockPos;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Queue;
import java.util.Set;

public class VisGraph {

	private static final int DX = (int) Math.pow(16.0D, 0.0D);
	private static final int DZ = (int) Math.pow(16.0D, 1.0D);
	private static final int DY = (int) Math.pow(16.0D, 2.0D);
	private static final int[] INDEX_OF_EDGES = new int[1352];

	static {
		int i = 0;
		int j = 15;
		int k = 0;

		for (int l = 0; l < 16; ++l) {
			for (int i1 = 0; i1 < 16; ++i1) {
				for (int j1 = 0; j1 < 16; ++j1) {
					if (l == 0 || l == 15 || i1 == 0 || i1 == 15 || j1 == 0 || j1 == 15) {
						INDEX_OF_EDGES[k++] = getIndex(l, i1, j1);
					}
				}
			}
		}
	}

	private final BitSet bitSet = new BitSet(4096);
	private int empty = 4096;

	private static int getIndex(BlockPos pos) {

		return getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

	private static int getIndex(int x, int y, int z) {

		return x | y << 8 | z << 4;
	}

	public void setOpaqueCube(BlockPos pos) {

		bitSet.set(getIndex(pos), true);
		--empty;
	}

	public SetVisibility computeVisibility() {

		SetVisibility setvisibility = new SetVisibility();

		if (4096 - empty < 256) {
			setvisibility.setAllVisible(true);
		} else if (empty == 0) {
			setvisibility.setAllVisible(false);
		} else {
			for (int i : INDEX_OF_EDGES) {
				if (!bitSet.get(i)) {
					setvisibility.setManyVisible(floodFill(i));
				}
			}
		}

		return setvisibility;
	}

	public Set<EnumFacing> getVisibleFacings(BlockPos pos) {

		return floodFill(getIndex(pos));
	}

	private Set<EnumFacing> floodFill(int pos) {

		Set<EnumFacing> set = EnumSet.noneOf(EnumFacing.class);
		Queue<Integer> queue = Queues.newArrayDeque();
		queue.add(IntegerCache.getInteger(pos));
		bitSet.set(pos, true);

		while (!queue.isEmpty()) {
			int i = queue.poll();
			addEdges(i, set);

			for (EnumFacing enumfacing : EnumFacing.values()) {
				int j = getNeighborIndexAtFace(i, enumfacing);

				if (j >= 0 && !bitSet.get(j)) {
					bitSet.set(j, true);
					queue.add(IntegerCache.getInteger(j));
				}
			}
		}

		return set;
	}

	private void addEdges(int pos, Set<EnumFacing> p_178610_2_) {

		int i = pos & 15;

		if (i == 0) {
			p_178610_2_.add(EnumFacing.WEST);
		} else if (i == 15) {
			p_178610_2_.add(EnumFacing.EAST);
		}

		int j = pos >> 8 & 15;

		if (j == 0) {
			p_178610_2_.add(EnumFacing.DOWN);
		} else if (j == 15) {
			p_178610_2_.add(EnumFacing.UP);
		}

		int k = pos >> 4 & 15;

		if (k == 0) {
			p_178610_2_.add(EnumFacing.NORTH);
		} else if (k == 15) {
			p_178610_2_.add(EnumFacing.SOUTH);
		}
	}

	private int getNeighborIndexAtFace(int pos, EnumFacing facing) {

		return switch (facing) {
			case DOWN -> {
				if ((pos >> 8 & 15) == 0) {
					yield -1;
				}

				yield pos - DY;
			}
			case UP -> {
				if ((pos >> 8 & 15) == 15) {
					yield -1;
				}

				yield pos + DY;
			}
			case NORTH -> {
				if ((pos >> 4 & 15) == 0) {
					yield -1;
				}

				yield pos - DZ;
			}
			case SOUTH -> {
				if ((pos >> 4 & 15) == 15) {
					yield -1;
				}

				yield pos + DZ;
			}
			case WEST -> {
				if ((pos & 15) == 0) {
					yield -1;
				}

				yield pos - DX;
			}
			case EAST -> {
				if ((pos & 15) == 15) {
					yield -1;
				}

				yield pos + DX;
			}
			default -> -1;
		};
	}
}
