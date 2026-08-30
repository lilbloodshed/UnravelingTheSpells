package org.holy.unraveling_spells.block.shriving_forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class ShrivingForgeRenderer implements BlockEntityRenderer<ShrivingForgeTile> {
    public ShrivingForgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ShrivingForgeTile forge, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = forge.getLevel();
        if (level == null) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float time = (level.getGameTime() + partialTick) * 0.5F;
        float rotation = time % 360.0F;
        float oscillation = (float) Math.sin(2.0 * Math.PI * time / 80.0F) * 0.05F;
        int light = getLightLevel(level, forge.getBlockPos());

        renderFloatingItem(itemRenderer, forge.getRenderScroll(), poseStack, bufferSource, level, light,
                0.3F, 1.3F + oscillation, rotation, 25.0F);
        renderFloatingItem(itemRenderer, forge.getRenderStone(), poseStack, bufferSource, level, light,
                -0.3F, 1.3F - oscillation, rotation, -25.0F);

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        itemRenderer.renderStatic(forge.getRenderResult(), ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 1);
        poseStack.popPose();
    }

    private static void renderFloatingItem(ItemRenderer renderer, ItemStack stack, PoseStack poseStack,
                                           MultiBufferSource bufferSource, Level level, int light,
                                           float horizontalOffset, float height, float rotation, float tilt) {
        poseStack.pushPose();
        poseStack.translate(0.5F, height, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(horizontalOffset, 0.0F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(tilt));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, level, 1);
        poseStack.popPose();
    }

    private static int getLightLevel(Level level, BlockPos pos) {
        return LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos));
    }
}
