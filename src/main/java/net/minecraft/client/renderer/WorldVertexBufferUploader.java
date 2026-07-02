package net.minecraft.client.renderer;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;


import java.nio.ByteBuffer;
import java.util.List;

public class WorldVertexBufferUploader {

	public void draw(BufferBuilder bufferBuilderIn) {
		if (bufferBuilderIn.getVertexCount() > 0) {
			VertexFormat vertexformat = bufferBuilderIn.getVertexFormat();
			int i = vertexformat.getNextOffset();
			ByteBuffer bytebuffer = bufferBuilderIn.getByteBuffer();
			List<VertexFormatElement> list = vertexformat.getElements();

			for (int j = 0; j < list.size(); ++j) {
				VertexFormatElement vertexformatelement = list.get(j);
				VertexFormatElement.Usage vertexformatelement$enumusage = vertexformatelement.getUsage();
				int k = vertexformatelement.getType().getGlConstant();
				int l = vertexformatelement.getIndex();
				bytebuffer.position(vertexformat.getOffset(j));

				switch (vertexformatelement$enumusage) {
					case POSITION:
						GLS.vertexPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
						GLS.enableClientState(32884);
						break;

					case UV:
						GLS.clientActiveTexture(OpenGlHelper.defaultTexUnit + l);
						GLS.texCoordPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
						GLS.enableClientState(32888);
						GLS.clientActiveTexture(OpenGlHelper.defaultTexUnit);
						break;

					case COLOR:
						GLS.colorPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
						GLS.enableClientState(32886);
						break;

					case NORMAL:
						GLS.normalPointer(k, i, bytebuffer);
						GLS.enableClientState(32885);
				}
			}

			GLS.drawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount());
			int i1 = 0;

			for (int j1 = list.size(); i1 < j1; ++i1) {
				VertexFormatElement vertexformatelement1 = list.get(i1);
				VertexFormatElement.Usage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
				int k1 = vertexformatelement1.getIndex();

				switch (vertexformatelement$enumusage1) {
					case POSITION:
						GLS.disableClientState(32884);
						break;

					case UV:
						GLS.clientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
						GLS.disableClientState(32888);
						GLS.clientActiveTexture(OpenGlHelper.defaultTexUnit);
						break;

					case COLOR:
						GLS.disableClientState(32886);
						GLS.resetColor();
						break;

					case NORMAL:
						GLS.disableClientState(32885);
				}
			}
		}

		bufferBuilderIn.reset();
	}

}
