package net.minecraft.client.renderer.chunk;

import net.minecraft.util.Facing;

import java.util.BitSet;
import java.util.Set;

public class SetVisibility {

	private static final int COUNT_FACES = Facing.values().length;
	private final BitSet bitSet;

	public SetVisibility() {

		bitSet = new BitSet(COUNT_FACES * COUNT_FACES);
	}

	public void setManyVisible(Set<Facing> facing) {

		for (Facing enumfacing : facing) {
			for (Facing enumfacing1 : facing) {
				setVisible(enumfacing, enumfacing1, true);
			}
		}
	}

	public void setVisible(Facing facing, Facing facing2, boolean p_178619_3_) {

		bitSet.set(facing.ordinal() + facing2.ordinal() * COUNT_FACES, p_178619_3_);
		bitSet.set(facing2.ordinal() + facing.ordinal() * COUNT_FACES, p_178619_3_);
	}

	public void setAllVisible(boolean visible) {

		bitSet.set(0, bitSet.size(), visible);
	}

	public boolean isVisible(Facing facing, Facing facing2) {

		return bitSet.get(facing.ordinal() + facing2.ordinal() * COUNT_FACES);
	}

	public String toString() {

		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append(' ');

		for (Facing enumfacing : Facing.values()) {
			stringbuilder.append(' ').append(enumfacing.toString().toUpperCase().charAt(0));
		}

		stringbuilder.append('\n');

		for (Facing enumfacing2 : Facing.values()) {
			stringbuilder.append(enumfacing2.toString().toUpperCase().charAt(0));

			for (Facing enumfacing1 : Facing.values()) {
				if (enumfacing2 == enumfacing1) {
					stringbuilder.append("  ");
				} else {
					boolean flag = isVisible(enumfacing2, enumfacing1);
					stringbuilder.append(' ').append(flag ? 'Y' : 'n');
				}
			}

			stringbuilder.append('\n');
		}

		return stringbuilder.toString();
	}

}
