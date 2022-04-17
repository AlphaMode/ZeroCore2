package it.zerono.mods.zerocore.internal.network;

import net.fabricmc.api.EnvType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.thread.BlockableEventLoop;
import org.jetbrains.annotations.Nullable;

public class NetworkContext {
    private final BlockableEventLoop executor;
    private @Nullable ServerPlayer serverPlayer;
    private final Side direction;
    private boolean handled = false;

    public NetworkContext(BlockableEventLoop executor, Side direction, @Nullable ServerPlayer serverPlayer) {
        this.executor = executor;
        this.direction = direction;
    }

    public void enqueueWork(Runnable runnable) {
        executor.execute(runnable);
    }

    public Side getDirection() {
        return direction;
    }

    public void setPacketHandled(boolean bool) {
        handled = bool;
    }

    public ServerPlayer getSender() {
        return serverPlayer;
    }

    public enum Side {
        TO_CLIENT(EnvType.SERVER),
        TO_SERVER(EnvType.CLIENT);

        private final EnvType side;

        Side(EnvType side) {
            this.side = side;
        }

        public EnvType getOriginationSide() {
            return side;
        }
    }
}
