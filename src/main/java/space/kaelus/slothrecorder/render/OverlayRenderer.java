package space.kaelus.slothrecorder.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import space.kaelus.slothrecorder.SlothRecorderClient;
import space.kaelus.slothrecorder.logic.PlayerTracker;

public class OverlayRenderer {
    public static void register(SlothRecorderClient mod) {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider consumers = context.consumers();
            TextRenderer textRenderer = client.textRenderer;

            for (PlayerEntity player : client.world.getPlayers()) {
                if (player == client.player) continue;

                PlayerTracker tracker = mod.getTrackers().get(player.getUuid());
                if (tracker == null) continue;

                matrices.push();
                
                Vec3d pos = player.getLerpedPos(context.tickCounter().getTickDelta(true));
                Vec3d cameraPos = context.camera().getPos();
                
                matrices.translate(pos.x - cameraPos.x, pos.y - cameraPos.y + player.getHeight() + 0.5, pos.z - cameraPos.z);
                
                // Billboard effect
                matrices.multiply(context.camera().getRotation());
                matrices.scale(-0.025f, -0.025f, 0.025f);

                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                
                String info = String.format("ΔY: %.2f ΔP: %.2f SensX: %s", 
                    tracker.getLastDeltaYaw(), tracker.getLastDeltaPitch(),
                    tracker.getModeX() > 0 ? "OK" : "...");
                
                float x = (float) (-textRenderer.getWidth(info) / 2);
                
                textRenderer.draw(info, x, 0, 0xFFFFFFFF, false, matrix4f, consumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                
                if (mod.isRecording()) {
                    String rec = mod.isCheatLabel() ? "RECORDING: CHEAT" : "RECORDING: LEGIT";
                    int color = mod.isCheatLabel() ? 0xFFFF0000 : 0xFF00FF00;
                    float rx = (float) (-textRenderer.getWidth(rec) / 2);
                    textRenderer.draw(rec, rx, 10, color, false, matrix4f, consumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                }

                matrices.pop();
            }
        });
    }
}
