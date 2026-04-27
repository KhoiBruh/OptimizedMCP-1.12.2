package net.minecraft.client.resources.data;

import net.minecraft.util.text.ITextComponent;

public class PackMetadataSection implements IMetadataSection
{
    private final ITextComponent packDescription;
    private final int packFormat;

    public PackMetadataSection(ITextComponent packDescriptionIn, int packFormatIn)
    {
        packDescription = packDescriptionIn;
        packFormat = packFormatIn;
    }

    public ITextComponent getPackDescription()
    {
        return packDescription;
    }

    public int getPackFormat()
    {
        return packFormat;
    }
}
