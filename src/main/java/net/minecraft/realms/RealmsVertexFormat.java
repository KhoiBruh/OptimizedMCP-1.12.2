package net.minecraft.realms;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.util.List;

public class RealmsVertexFormat {

	private VertexFormat v;

	public RealmsVertexFormat(VertexFormat vIn) {

		v = vIn;
	}

	public RealmsVertexFormat from(VertexFormat p_from_1_) {

		v = p_from_1_;
		return this;
	}

	public VertexFormat getVertexFormat() {

		return v;
	}

	public void clear() {

		v.clear();
	}

	public int getUvOffset(int p_getUvOffset_1_) {

		return v.getUvOffsetById(p_getUvOffset_1_);
	}

	public int getElementCount() {

		return v.getElementCount();
	}

	public boolean hasColor() {

		return v.hasColor();
	}

	public boolean hasUv(int p_hasUv_1_) {

		return v.hasUvOffset(p_hasUv_1_);
	}

	public RealmsVertexFormatElement getElement(int p_getElement_1_) {

		return new RealmsVertexFormatElement(v.getElement(p_getElement_1_));
	}

	public RealmsVertexFormat addElement(RealmsVertexFormatElement p_addElement_1_) {

		return from(v.addElement(p_addElement_1_.getVertexFormatElement()));
	}

	public int getColorOffset() {

		return v.getColorOffset();
	}

	public List<RealmsVertexFormatElement> getElements() {

		List<RealmsVertexFormatElement> list = Lists.newArrayList();

		for (VertexFormatElement vertexformatelement : v.getElements()) {
			list.add(new RealmsVertexFormatElement(vertexformatelement));
		}

		return list;
	}

	public boolean hasNormal() {

		return v.hasNormal();
	}

	public int getVertexSize() {

		return v.getNextOffset();
	}

	public int getOffset(int p_getOffset_1_) {

		return v.getOffset(p_getOffset_1_);
	}

	public int getNormalOffset() {

		return v.getNormalOffset();
	}

	public int getIntegerSize() {

		return v.getIntegerSize();
	}

	public boolean equals(Object p_equals_1_) {

		return v.equals(p_equals_1_);
	}

	public int hashCode() {

		return v.hashCode();
	}

	public String toString() {

		return v.toString();
	}

}
