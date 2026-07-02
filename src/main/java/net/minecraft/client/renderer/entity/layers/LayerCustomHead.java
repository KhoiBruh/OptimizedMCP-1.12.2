package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.Facing;


public class LayerCustomHead implements LayerRenderer<EntityLivingBase> {

	private final ModelRenderer modelRenderer;

	public LayerCustomHead(ModelRenderer p_i46120_1_) {
		modelRenderer = p_i46120_1_;
	}

	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

		if (!itemstack.isEmpty()) {
			Item item = itemstack.getItem();
			Minecraft minecraft = Minecraft.getMinecraft();
			GLS.pushMatrix();

			if (entitylivingbaseIn.isSneaking()) {
				GLS.translate(0F, 0.2F, 0F);
			}

			boolean flag = entitylivingbaseIn instanceof EntityVillager || entitylivingbaseIn instanceof EntityZombieVillager;

			if (entitylivingbaseIn.isChild() && !(entitylivingbaseIn instanceof EntityVillager)) {
				float f = 2F;
				float f1 = 1.4F;
				GLS.translate(0F, 0.5F * scale, 0F);
				GLS.scale(0.7F, 0.7F, 0.7F);
				GLS.translate(0F, 16F * scale, 0F);
			}

			modelRenderer.postRender(0.0625F);
			GLS.color(1F, 1F, 1F, 1F);

			if (item == Items.SKULL) {
				float f2 = 1.1875F;
				GLS.scale(1.1875F, -1.1875F, -1.1875F);

				if (flag) {
					GLS.translate(0F, 0.0625F, 0F);
				}

				GameProfile gameprofile = null;

				if (itemstack.hasTagCompound()) {
					NBTTagCompound nbttagcompound = itemstack.getTagCompound();

					if (nbttagcompound.hasKey("SkullOwner", 10)) {
						gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
					} else if (nbttagcompound.hasKey("SkullOwner", 8)) {
						String s = nbttagcompound.getString("SkullOwner");

						if (s != null && !s.isBlank()) {
							gameprofile = TileEntitySkull.updateGameprofile(new GameProfile(null, s));
							nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
						}
					}
				}

				TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0F, -0.5F, Facing.UP, 180F, itemstack.getMetadata(), gameprofile, -1, limbSwing);
			} else if (!(item instanceof ItemArmor) || ((ItemArmor) item).getEquipmentSlot() != EntityEquipmentSlot.HEAD) {
				float f3 = 0.625F;
				GLS.translate(0F, -0.25F, 0F);
				GLS.rotate(180F, 0F, 1F, 0F);
				GLS.scale(0.625F, -0.625F, -0.625F);

				if (flag) {
					GLS.translate(0F, 0.1875F, 0F);
				}

				minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.HEAD);
			}

			GLS.popMatrix();
		}
	}

	public boolean shouldCombineTextures() {
		return false;
	}

}
