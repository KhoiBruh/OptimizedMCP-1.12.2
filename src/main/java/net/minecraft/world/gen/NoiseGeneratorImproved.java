package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorImproved extends NoiseGenerator {

	private static final double[] GRAD_X = new double[]{1D, -1D, 1D, -1D, 1D, -1D, 1D, -1D, 0D, 0D, 0D, 0D, 1D, 0D, -1D, 0D};
	private static final double[] GRAD_Y = new double[]{1D, 1D, -1D, -1D, 0D, 0D, 0D, 0D, 1D, -1D, 1D, -1D, 1D, -1D, 1D, -1D};
	private static final double[] GRAD_Z = new double[]{0D, 0D, 0D, 0D, 1D, 1D, -1D, -1D, 1D, 1D, -1D, -1D, 0D, 1D, 0D, -1D};
	private static final double[] GRAD_2X = new double[]{1D, -1D, 1D, -1D, 1D, -1D, 1D, -1D, 0D, 0D, 0D, 0D, 1D, 0D, -1D, 0D};
	private static final double[] GRAD_2Z = new double[]{0D, 0D, 0D, 0D, 1D, 1D, -1D, -1D, 1D, 1D, -1D, -1D, 0D, 1D, 0D, -1D};
	/**
	 * An int[512], where the first 256 elements are the numbers 0..255, in random shuffled order,
	 * and the second half of the array is identical to the first half, apparently for convenience in wrapping lookups.
	 * <p>
	 * Effectively a shuffled 0..255 that wraps once.
	 */
	private final int[] permutations;
	public double xCoord;
	public double yCoord;
	public double zCoord;

	public NoiseGeneratorImproved() {

		this(new Random());
	}

	public NoiseGeneratorImproved(Random p_i45469_1_) {

		permutations = new int[512];
		xCoord = p_i45469_1_.nextDouble() * 256D;
		yCoord = p_i45469_1_.nextDouble() * 256D;
		zCoord = p_i45469_1_.nextDouble() * 256D;

		for (int i = 0; i < 256; permutations[i] = i++) {
		}

		for (int l = 0; l < 256; ++l) {
			int j = p_i45469_1_.nextInt(256 - l) + l;
			int k = permutations[l];
			permutations[l] = permutations[j];
			permutations[j] = k;
			permutations[l + 256] = permutations[l];
		}
	}

	public final double lerp(double p_76311_1_, double p_76311_3_, double p_76311_5_) {

		return p_76311_3_ + p_76311_1_ * (p_76311_5_ - p_76311_3_);
	}

	public final double grad2(int p_76309_1_, double p_76309_2_, double p_76309_4_) {

		int i = p_76309_1_ & 15;
		return GRAD_2X[i] * p_76309_2_ + GRAD_2Z[i] * p_76309_4_;
	}

	public final double grad(int p_76310_1_, double p_76310_2_, double p_76310_4_, double p_76310_6_) {

		int i = p_76310_1_ & 15;
		return GRAD_X[i] * p_76310_2_ + GRAD_Y[i] * p_76310_4_ + GRAD_Z[i] * p_76310_6_;
	}

	/**
	 * noiseArray should be xSize*ySize*zSize in size
	 */
	public void populateNoiseArray(double[] noiseArray, double xOffset, double yOffset, double zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale, double noiseScale) {

		if (ySize == 1) {
			int i5;
			int j5;
			int j;
			int k5;
			double d14;
			double d15;
			int l5 = 0;
			double d16 = 1D / noiseScale;

			for (int j2 = 0; j2 < xSize; ++j2) {
				double d17 = xOffset + (double) j2 * xScale + xCoord;
				int i6 = (int) d17;

				if (d17 < (double) i6) {
					--i6;
				}

				int k2 = i6 & 255;
				d17 = d17 - (double) i6;
				double d18 = d17 * d17 * d17 * (d17 * (d17 * 6D - 15D) + 10D);

				for (int j6 = 0; j6 < zSize; ++j6) {
					double d19 = zOffset + (double) j6 * zScale + zCoord;
					int k6 = (int) d19;

					if (d19 < (double) k6) {
						--k6;
					}

					int l6 = k6 & 255;
					d19 = d19 - (double) k6;
					double d20 = d19 * d19 * d19 * (d19 * (d19 * 6D - 15D) + 10D);
					i5 = permutations[k2];
					j5 = permutations[i5] + l6;
					j = permutations[k2 + 1];
					k5 = permutations[j] + l6;
					d14 = lerp(d18, grad2(permutations[j5], d17, d19), grad(permutations[k5], d17 - 1D, 0D, d19));
					d15 = lerp(d18, grad(permutations[j5 + 1], d17, 0D, d19 - 1D), grad(permutations[k5 + 1], d17 - 1D, 0D, d19 - 1D));
					double d21 = lerp(d20, d14, d15);
					int i7 = l5++;
					noiseArray[i7] += d21 * d16;
				}
			}
		} else {
			int i = 0;
			double d0 = 1D / noiseScale;
			int k = -1;
			int l;
			int i1;
			int j1;
			int k1;
			int l1;
			int i2;
			double d1 = 0D;
			double d2 = 0D;
			double d3 = 0D;
			double d4 = 0D;

			for (int l2 = 0; l2 < xSize; ++l2) {
				double d5 = xOffset + (double) l2 * xScale + xCoord;
				int i3 = (int) d5;

				if (d5 < (double) i3) {
					--i3;
				}

				int j3 = i3 & 255;
				d5 = d5 - (double) i3;
				double d6 = d5 * d5 * d5 * (d5 * (d5 * 6D - 15D) + 10D);

				for (int k3 = 0; k3 < zSize; ++k3) {
					double d7 = zOffset + (double) k3 * zScale + zCoord;
					int l3 = (int) d7;

					if (d7 < (double) l3) {
						--l3;
					}

					int i4 = l3 & 255;
					d7 = d7 - (double) l3;
					double d8 = d7 * d7 * d7 * (d7 * (d7 * 6D - 15D) + 10D);

					for (int j4 = 0; j4 < ySize; ++j4) {
						double d9 = yOffset + (double) j4 * yScale + yCoord;
						int k4 = (int) d9;

						if (d9 < (double) k4) {
							--k4;
						}

						int l4 = k4 & 255;
						d9 = d9 - (double) k4;
						double d10 = d9 * d9 * d9 * (d9 * (d9 * 6D - 15D) + 10D);

						if (j4 == 0 || l4 != k) {
							k = l4;
							l = permutations[j3] + l4;
							i1 = permutations[l] + i4;
							j1 = permutations[l + 1] + i4;
							k1 = permutations[j3 + 1] + l4;
							l1 = permutations[k1] + i4;
							i2 = permutations[k1 + 1] + i4;
							d1 = lerp(d6, grad(permutations[i1], d5, d9, d7), grad(permutations[l1], d5 - 1D, d9, d7));
							d2 = lerp(d6, grad(permutations[j1], d5, d9 - 1D, d7), grad(permutations[i2], d5 - 1D, d9 - 1D, d7));
							d3 = lerp(d6, grad(permutations[i1 + 1], d5, d9, d7 - 1D), grad(permutations[l1 + 1], d5 - 1D, d9, d7 - 1D));
							d4 = lerp(d6, grad(permutations[j1 + 1], d5, d9 - 1D, d7 - 1D), grad(permutations[i2 + 1], d5 - 1D, d9 - 1D, d7 - 1D));
						}

						double d11 = lerp(d10, d1, d2);
						double d12 = lerp(d10, d3, d4);
						double d13 = lerp(d8, d11, d12);
						int j7 = i++;
						noiseArray[j7] += d13 * d0;
					}
				}
			}
		}
	}

}
