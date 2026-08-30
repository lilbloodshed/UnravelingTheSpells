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
    public void render(ShrivingForgeTile blockentity, float tick, PoseStack poseStack, MultiBufferSource bufferSource, int i, int i1) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack scroll = blockentity.getRenderScroll();
        ItemStack stone = blockentity.getRenderStone();
        ItemStack result = blockentity.getRenderResult();

        float time = (blockentity.getLevel().getGameTime() + tick) * 0.5f;
        float angle = time % 360;

        final float center = 1.3f;
        final float amplitude = 0.05f;
        final float period = 80.0f;
        float angleForSin = (float) (2 * Math.PI * time / period);
        float y_ampl1 = center + (float) Math.sin(angleForSin) * amplitude;
        float y_ampl2 = center - (float) Math.sin(angleForSin) * amplitude;

        poseStack.pushPose();
        poseStack.translate(0.5f, y_ampl1, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(0.3f, 0f, 0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(25f));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        itemRenderer.renderStatic(scroll, ItemDisplayContext.FIXED, getLightLevel(blockentity.getLevel(), blockentity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, blockentity.getLevel(), 1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5f, y_ampl2, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.3f, 0f, 0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-25f));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        itemRenderer.renderStatic(stone, ItemDisplayContext.FIXED, getLightLevel(blockentity.getLevel(), blockentity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, blockentity.getLevel(), 1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5f, 1f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, getLightLevel(blockentity.getLevel(), blockentity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, blockentity.getLevel(), 1);
        poseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
