package net.minecraft.client.resources.data;

import net.minecraft.util.text.ITextComponent;

public record PackMetadataSection(ITextComponent packDescription, int packFormat) implements IMetadataSection {

}
