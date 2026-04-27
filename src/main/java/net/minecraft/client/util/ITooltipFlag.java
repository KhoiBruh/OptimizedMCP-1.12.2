package net.minecraft.client.util;

public interface ITooltipFlag
{
    boolean isAdvanced();

    public static enum TooltipFlags implements ITooltipFlag
    {
        NORMAL(false),
        ADVANCED(true);

        final boolean isAdvanced;

        private TooltipFlags(boolean advanced)
        {
            isAdvanced = advanced;
        }

        public boolean isAdvanced()
        {
            return isAdvanced;
        }
    }
}
