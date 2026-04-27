package net.minecraft.client.renderer.culling;

import net.minecraft.util.math.AxisAlignedBB;

public class Frustum implements ICamera
{
    private final ClippingHelper clippingHelper;
    private double x;
    private double y;
    private double z;

    public Frustum()
    {
        this(ClippingHelperImpl.getInstance());
    }

    public Frustum(ClippingHelper clippingHelperIn)
    {
        clippingHelper = clippingHelperIn;
    }

    public void setPosition(double xIn, double yIn, double zIn)
    {
        x = xIn;
        y = yIn;
        z = zIn;
    }

    /**
     * Calls the clipping helper. Returns true if the box is inside all 6 clipping planes, otherwise returns false.
     */
    public boolean isBoxInFrustum(double p_78548_1_, double p_78548_3_, double p_78548_5_, double p_78548_7_, double p_78548_9_, double p_78548_11_)
    {
        return clippingHelper.isBoxInFrustum(p_78548_1_ - x, p_78548_3_ - y, p_78548_5_ - z, p_78548_7_ - x, p_78548_9_ - y, p_78548_11_ - z);
    }

    /**
     * Returns true if the bounding box is inside all 6 clipping planes, otherwise returns false.
     */
    public boolean isBoundingBoxInFrustum(AxisAlignedBB p_78546_1_)
    {
        return isBoxInFrustum(p_78546_1_.minX, p_78546_1_.minY, p_78546_1_.minZ, p_78546_1_.maxX, p_78546_1_.maxY, p_78546_1_.maxZ);
    }
}
