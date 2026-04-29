package net.minecraft.world.gen;

import java.util.Arrays;
import java.util.Random;

public class NoiseGeneratorPerlin extends NoiseGenerator {

	private final NoiseGeneratorSimplex[] noiseLevels;
	private final int levels;

	public NoiseGeneratorPerlin(Random seed, int levelsIn) {

		levels = levelsIn;
		noiseLevels = new NoiseGeneratorSimplex[levelsIn];

		for (int i = 0; i < levelsIn; ++i) {
			noiseLevels[i] = new NoiseGeneratorSimplex(seed);
		}
	}

	public double getValue(double p_151601_1_, double p_151601_3_) {

		double d0 = 0D;
		double d1 = 1D;

		for (int i = 0; i < levels; ++i) {
			d0 += noiseLevels[i].getValue(p_151601_1_ * d1, p_151601_3_ * d1) / d1;
			d1 /= 2D;
		}

		return d0;
	}

	public double[] getRegion(double[] p_151599_1_, double p_151599_2_, double p_151599_4_, int p_151599_6_, int p_151599_7_, double p_151599_8_, double p_151599_10_, double p_151599_12_) {

		return getRegion(p_151599_1_, p_151599_2_, p_151599_4_, p_151599_6_, p_151599_7_, p_151599_8_, p_151599_10_, p_151599_12_, 0.5D);
	}

	public double[] getRegion(double[] p_151600_1_, double p_151600_2_, double p_151600_4_, int p_151600_6_, int p_151600_7_, double p_151600_8_, double p_151600_10_, double p_151600_12_, double p_151600_14_) {

		if (p_151600_1_ != null && p_151600_1_.length >= p_151600_6_ * p_151600_7_) {
			Arrays.fill(p_151600_1_, 0D);
		} else {
			p_151600_1_ = new double[p_151600_6_ * p_151600_7_];
		}

		double d1 = 1D;
		double d0 = 1D;

		for (int j = 0; j < levels; ++j) {
			noiseLevels[j].add(p_151600_1_, p_151600_2_, p_151600_4_, p_151600_6_, p_151600_7_, p_151600_8_ * d0 * d1, p_151600_10_ * d0 * d1, 0.55D / d1);
			d0 *= p_151600_12_;
			d1 *= p_151600_14_;
		}

		return p_151600_1_;
	}

}
