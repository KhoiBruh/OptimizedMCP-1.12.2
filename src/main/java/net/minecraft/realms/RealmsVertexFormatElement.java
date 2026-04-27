package net.minecraft.realms;

import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class RealmsVertexFormatElement {

	private final VertexFormatElement v;

	public RealmsVertexFormatElement(VertexFormatElement vIn) {

		v = vIn;
	}

	public VertexFormatElement getVertexFormatElement() {

		return v;
	}

	public boolean isPosition() {

		return v.isPositionElement();
	}

	public int getIndex() {

		return v.getIndex();
	}

	public int getByteSize() {

		return v.getSize();
	}

	public int getCount() {

		return v.getElementCount();
	}

	public int hashCode() {

		return v.hashCode();
	}

	public boolean equals(Object p_equals_1_) {

		return v.equals(p_equals_1_);
	}

	public String toString() {

		return v.toString();
	}

}
