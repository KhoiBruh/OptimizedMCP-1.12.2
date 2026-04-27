package net.minecraft.client.renderer.block.model;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class ModelResourceLocation extends ResourceLocation {

	private final String variant;

	protected ModelResourceLocation(int unused, String... resourceName) {

		super(0, resourceName[0], resourceName[1]);
		variant = StringUtils.isEmpty(resourceName[2]) ? "normal" : resourceName[2].toLowerCase(Locale.ROOT);
	}

	public ModelResourceLocation(String pathIn) {

		this(0, parsePathString(pathIn));
	}

	public ModelResourceLocation(ResourceLocation location, String variantIn) {

		this(location.toString(), variantIn);
	}

	public ModelResourceLocation(String location, String variantIn) {

		this(0, parsePathString(location + '#' + (variantIn == null ? "normal" : variantIn)));
	}

	protected static String[] parsePathString(String pathIn) {

		String[] astring = new String[]{null, pathIn, null};
		int i = pathIn.indexOf(35);
		String s = pathIn;

		if (i >= 0) {
			astring[2] = pathIn.substring(i + 1);

			if (i > 1) {
				s = pathIn.substring(0, i);
			}
		}

		System.arraycopy(ResourceLocation.splitObjectName(s), 0, astring, 0, 2);
		return astring;
	}

	public String getVariant() {

		return variant;
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (p_equals_1_ instanceof ModelResourceLocation modelresourcelocation && super.equals(p_equals_1_)) {
			return variant.equals(modelresourcelocation.variant);
		} else {
			return false;
		}
	}

	public int hashCode() {

		return 31 * super.hashCode() + variant.hashCode();
	}

	public String toString() {

		return super.toString() + '#' + variant;
	}

}
