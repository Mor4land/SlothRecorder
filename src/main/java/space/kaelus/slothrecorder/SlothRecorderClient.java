package space.kaelus.slothrecorder;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import space.kaelus.slothrecorder.logic.PlayerTracker;
import space.kaelus.slothrecorder.render.OverlayRenderer;
import space.kaelus.slothrecorder.util.CSVExporter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlothRecorderClient implements ClientModInitializer {
    private final Map<UUID, PlayerTracker> trackers = new HashMap<>();
    private CSVExporter exporter;
    private boolean isRecording = false;
    private boolean isCheatLabel = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("slothrecord")
                .then(ClientCommandManager.literal("start")
                    .then(ClientCommandManager.argument("label", StringArgumentType.word())
                        .suggests((context, builder) -> builder.suggest("legit").suggest("cheat").buildFuture())
                        .executes(context -> {
                            String label = StringArgumentType.getString(context, "label");
                            startRecording(label, null);
                            context.getSource().sendFeedback(Text.literal("Recording started with label: " + label));
                            return 1;
                        })
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                            .executes(context -> {
                                String label = StringArgumentType.getString(context, "label");
                                String name = StringArgumentType.getString(context, "name");
                                startRecording(label, name);
                                context.getSource().sendFeedback(Text.literal("Recording started: " + name + " (" + label + ")"));
                                return 1;
                            })
                        )
                    )
                )
                .then(ClientCommandManager.literal("stop")
                    .executes(context -> {
                        stopRecording();
                        context.getSource().sendFeedback(Text.literal("Recording stopped."));
                        return 1;
                    })
                )
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                UUID uuid = player.getUuid();
                PlayerTracker tracker = trackers.computeIfAbsent(uuid, 
                    id -> new PlayerTracker(id, player.getName().getString(), (float) player.getYaw(), (float) player.getPitch()));
                
                tracker.update((float) player.getYaw(), (float) player.getPitch());

                if (isRecording && exporter != null) {
                    exporter.writeRow(tracker.toCsvRow(isCheatLabel));
                }
            }
            
            if (isRecording && exporter != null) {
                exporter.flush();
            }
        });

        OverlayRenderer.register(this);
    }

    private void startRecording(String label, String customName) {
        this.isCheatLabel = label.equalsIgnoreCase("cheat");
        this.exporter = new CSVExporter(label, customName);
        this.isRecording = true;
    }

    private void stopRecording() {
        this.isRecording = false;
        if (exporter != null) {
            exporter.close();
            exporter = null;
        }
    }

    public Map<UUID, PlayerTracker> getTrackers() {
        return trackers;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isCheatLabel() {
        return isCheatLabel;
    }
}
