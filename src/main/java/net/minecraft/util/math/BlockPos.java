package net.minecraft.util.math;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.concurrent.Immutable;
import java.util.Iterator;
import java.util.List;

@Immutable
public class BlockPos extends Vec3i {

	/**
	 * An immutable block pos with zero as all coordinates.
	 */
	public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
	private static final int NUM_Z_BITS = NUM_X_BITS;
	private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
	private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
	private static final int Y_SHIFT = NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
	private static final long X_MASK = (1L << NUM_X_BITS) - 1L;

	public BlockPos(int x, int y, int z) {

		super(x, y, z);
	}

	public BlockPos(double x, double y, double z) {

		super(x, y, z);
	}

	public BlockPos(Entity source) {

		this(source.posX, source.posY, source.posZ);
	}

	public BlockPos(Vec3d vec) {

		this(vec.x(), vec.y(), vec.z());
	}

	public BlockPos(Vec3i source) {

		this(source.getX(), source.getY(), source.getZ());
	}

	/**
	 * Create a BlockPos from a serialized long value (created by toLong)
	 */
	public static BlockPos fromLong(long serialized) {

		int i = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
		int j = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
		int k = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
		return new BlockPos(i, j, k);
	}

	public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to) {

		return getAllInBox(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
	}

	public static Iterable<BlockPos> getAllInBox(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {

		return new Iterable<BlockPos>() {
			public Iterator<BlockPos> iterator() {

				return new AbstractIterator<BlockPos>() {
					private boolean first = true;
					private int lastPosX;
					private int lastPosY;
					private int lastPosZ;

					protected BlockPos computeNext() {

						if (first) {
							first = false;
							lastPosX = x1;
							lastPosY = y1;
							lastPosZ = z1;
							return new BlockPos(x1, y1, z1);
						} else if (lastPosX == x2 && lastPosY == y2 && lastPosZ == z2) {
							return endOfData();
						} else {
							if (lastPosX < x2) {
								++lastPosX;
							} else if (lastPosY < y2) {
								lastPosX = x1;
								++lastPosY;
							} else if (lastPosZ < z2) {
								lastPosX = x1;
								lastPosY = y1;
								++lastPosZ;
							}

							return new BlockPos(lastPosX, lastPosY, lastPosZ);
						}
					}
				};
			}
		};
	}

	public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(BlockPos from, BlockPos to) {

		return getAllInBoxMutable(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
	}

	public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {

		return new Iterable<BlockPos.MutableBlockPos>() {
			public Iterator<BlockPos.MutableBlockPos> iterator() {

				return new AbstractIterator<BlockPos.MutableBlockPos>() {
					private BlockPos.MutableBlockPos pos;

					protected BlockPos.MutableBlockPos computeNext() {

						if (pos == null) {
							pos = new BlockPos.MutableBlockPos(x1, y1, z1);
							return pos;
						} else if (pos.x == x2 && pos.y == y2 && pos.z == z2) {
							return endOfData();
						} else {
							if (pos.x < x2) {
								++pos.x;
							} else if (pos.y < y2) {
								pos.x = x1;
								++pos.y;
							} else if (pos.z < z2) {
								pos.x = x1;
								pos.y = y1;
								++pos.z;
							}

							return pos;
						}
					}
				};
			}
		};
	}

	/**
	 * Add the given coordinates to the coordinates of this BlockPos
	 */
	public BlockPos add(double x, double y, double z) {

		return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos((double) getX() + x, (double) getY() + y, (double) getZ() + z);
	}

	/**
	 * Add the given coordinates to the coordinates of this BlockPos
	 */
	public BlockPos add(int x, int y, int z) {

		return x == 0 && y == 0 && z == 0 ? this : new BlockPos(getX() + x, getY() + y, getZ() + z);
	}

	/**
	 * Add the given Vector to this BlockPos
	 */
	public BlockPos add(Vec3i vec) {

		return add(vec.getX(), vec.getY(), vec.getZ());
	}

	/**
	 * Subtract the given Vector from this BlockPos
	 */
	public BlockPos subtract(Vec3i vec) {

		return add(-vec.getX(), -vec.getY(), -vec.getZ());
	}

	/**
	 * Offset this BlockPos 1 block up
	 */
	public BlockPos up() {

		return up(1);
	}

	/**
	 * Offset this BlockPos n blocks up
	 */
	public BlockPos up(int n) {

		return offset(EnumFacing.UP, n);
	}

	/**
	 * Offset this BlockPos 1 block down
	 */
	public BlockPos down() {

		return down(1);
	}

	/**
	 * Offset this BlockPos n blocks down
	 */
	public BlockPos down(int n) {

		return offset(EnumFacing.DOWN, n);
	}

	/**
	 * Offset this BlockPos 1 block in northern direction
	 */
	public BlockPos north() {

		return north(1);
	}

	/**
	 * Offset this BlockPos n blocks in northern direction
	 */
	public BlockPos north(int n) {

		return offset(EnumFacing.NORTH, n);
	}

	/**
	 * Offset this BlockPos 1 block in southern direction
	 */
	public BlockPos south() {

		return south(1);
	}

	/**
	 * Offset this BlockPos n blocks in southern direction
	 */
	public BlockPos south(int n) {

		return offset(EnumFacing.SOUTH, n);
	}

	/**
	 * Offset this BlockPos 1 block in western direction
	 */
	public BlockPos west() {

		return west(1);
	}

	/**
	 * Offset this BlockPos n blocks in western direction
	 */
	public BlockPos west(int n) {

		return offset(EnumFacing.WEST, n);
	}

	/**
	 * Offset this BlockPos 1 block in eastern direction
	 */
	public BlockPos east() {

		return east(1);
	}

	/**
	 * Offset this BlockPos n blocks in eastern direction
	 */
	public BlockPos east(int n) {

		return offset(EnumFacing.EAST, n);
	}

	/**
	 * Offset this BlockPos 1 block in the given direction
	 */
	public BlockPos offset(EnumFacing facing) {

		return offset(facing, 1);
	}

	/**
	 * Offsets this BlockPos n blocks in the given direction
	 */
	public BlockPos offset(EnumFacing facing, int n) {

		return n == 0 ? this : new BlockPos(getX() + facing.getFrontOffsetX() * n, getY() + facing.getFrontOffsetY() * n, getZ() + facing.getFrontOffsetZ() * n);
	}

	public BlockPos rotate(Rotation rotationIn) {

		switch (rotationIn) {
			case NONE:
			default:
				return this;

			case CLOCKWISE_90:
				return new BlockPos(-getZ(), getY(), getX());

			case CLOCKWISE_180:
				return new BlockPos(-getX(), getY(), -getZ());

			case COUNTERCLOCKWISE_90:
				return new BlockPos(getZ(), getY(), -getX());
		}
	}

	/**
	 * Calculate the cross product of this and the given Vector
	 */
	public BlockPos crossProduct(Vec3i vec) {

		return new BlockPos(getY() * vec.getZ() - getZ() * vec.getY(), getZ() * vec.getX() - getX() * vec.getZ(), getX() * vec.getY() - getY() * vec.getX());
	}

	/**
	 * Serialize this BlockPos into a long value
	 */
	public long toLong() {

		return ((long) getX() & X_MASK) << X_SHIFT | ((long) getY() & Y_MASK) << Y_SHIFT | ((long) getZ() & Z_MASK);
	}

	/**
	 * Returns a version of this BlockPos that is guaranteed to be immutable.
	 *
	 * <p>When storing a BlockPos given to you for an extended period of time, make sure you
	 * use this in case the value is changed internally.</p>
	 */
	public BlockPos toImmutable() {

		return this;
	}

	public static class MutableBlockPos extends BlockPos {

		protected int x;
		protected int y;
		protected int z;

		public MutableBlockPos() {

			this(0, 0, 0);
		}

		public MutableBlockPos(BlockPos pos) {

			this(pos.getX(), pos.getY(), pos.getZ());
		}

		public MutableBlockPos(int x_, int y_, int z_) {

			super(0, 0, 0);
			x = x_;
			y = y_;
			z = z_;
		}

		public BlockPos add(double x, double y, double z) {

			return super.add(x, y, z).toImmutable();
		}

		public BlockPos add(int x, int y, int z) {

			return super.add(x, y, z).toImmutable();
		}

		public BlockPos offset(EnumFacing facing, int n) {

			return super.offset(facing, n).toImmutable();
		}

		public BlockPos rotate(Rotation rotationIn) {

			return super.rotate(rotationIn).toImmutable();
		}

		public int getX() {

			return x;
		}

		public int getY() {

			return y;
		}

		public void setY(int yIn) {

			y = yIn;
		}

		public int getZ() {

			return z;
		}

		public BlockPos.MutableBlockPos setPos(int xIn, int yIn, int zIn) {

			x = xIn;
			y = yIn;
			z = zIn;
			return this;
		}

		public BlockPos.MutableBlockPos setPos(Entity entityIn) {

			return setPos(entityIn.posX, entityIn.posY, entityIn.posZ);
		}

		public BlockPos.MutableBlockPos setPos(double xIn, double yIn, double zIn) {

			return setPos(MathHelper.floor(xIn), MathHelper.floor(yIn), MathHelper.floor(zIn));
		}

		public BlockPos.MutableBlockPos setPos(Vec3i vec) {

			return setPos(vec.getX(), vec.getY(), vec.getZ());
		}

		public BlockPos.MutableBlockPos move(EnumFacing facing) {

			return move(facing, 1);
		}

		public BlockPos.MutableBlockPos move(EnumFacing facing, int n) {

			return setPos(x + facing.getFrontOffsetX() * n, y + facing.getFrontOffsetY() * n, z + facing.getFrontOffsetZ() * n);
		}

		public BlockPos toImmutable() {

			return new BlockPos(this);
		}

	}

	public static final class PooledMutableBlockPos extends BlockPos.MutableBlockPos {

		private static final List<BlockPos.PooledMutableBlockPos> POOL = Lists.newArrayList();
		private boolean released;

		private PooledMutableBlockPos(int xIn, int yIn, int zIn) {

			super(xIn, yIn, zIn);
		}

		public static BlockPos.PooledMutableBlockPos retain() {

			return retain(0, 0, 0);
		}

		public static BlockPos.PooledMutableBlockPos retain(double xIn, double yIn, double zIn) {

			return retain(MathHelper.floor(xIn), MathHelper.floor(yIn), MathHelper.floor(zIn));
		}

		public static BlockPos.PooledMutableBlockPos retain(Vec3i vec) {

			return retain(vec.getX(), vec.getY(), vec.getZ());
		}

		public static BlockPos.PooledMutableBlockPos retain(int xIn, int yIn, int zIn) {

			synchronized (POOL) {
				if (!POOL.isEmpty()) {
					BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = POOL.remove(POOL.size() - 1);

					if (blockpos$pooledmutableblockpos != null && blockpos$pooledmutableblockpos.released) {
						blockpos$pooledmutableblockpos.released = false;
						blockpos$pooledmutableblockpos.setPos(xIn, yIn, zIn);
						return blockpos$pooledmutableblockpos;
					}
				}
			}

			return new BlockPos.PooledMutableBlockPos(xIn, yIn, zIn);
		}

		public void release() {

			synchronized (POOL) {
				if (POOL.size() < 100) {
					POOL.add(this);
				}

				released = true;
			}
		}

		public BlockPos.PooledMutableBlockPos setPos(int xIn, int yIn, int zIn) {

			if (released) {
				BlockPos.LOGGER.error("PooledMutableBlockPosition modified after it was released.", new Throwable());
				released = false;
			}

			return (BlockPos.PooledMutableBlockPos) super.setPos(xIn, yIn, zIn);
		}

		public BlockPos.PooledMutableBlockPos setPos(Entity entityIn) {

			return (BlockPos.PooledMutableBlockPos) super.setPos(entityIn);
		}

		public BlockPos.PooledMutableBlockPos setPos(double xIn, double yIn, double zIn) {

			return (BlockPos.PooledMutableBlockPos) super.setPos(xIn, yIn, zIn);
		}

		public BlockPos.PooledMutableBlockPos setPos(Vec3i vec) {

			return (BlockPos.PooledMutableBlockPos) super.setPos(vec);
		}

		public BlockPos.PooledMutableBlockPos move(EnumFacing facing) {

			return (BlockPos.PooledMutableBlockPos) super.move(facing);
		}

		public BlockPos.PooledMutableBlockPos move(EnumFacing facing, int n) {

			return (BlockPos.PooledMutableBlockPos) super.move(facing, n);
		}

	}

}
