/*
 *
 * AbstractModBlockEntity.java
 *
 * This file is part of Zero CORE 2 by ZeroNoRyouki, a Minecraft mod.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * DO NOT REMOVE OR EDIT THIS HEADER
 *
 */

package it.zerono.mods.zerocore.lib.block;

import it.zerono.mods.zerocore.internal.network.Network;
import it.zerono.mods.zerocore.internal.network.TileCommandMessage;
import it.zerono.mods.zerocore.lib.CodeHelper;
import it.zerono.mods.zerocore.lib.IDebugMessages;
import it.zerono.mods.zerocore.lib.IDebuggable;
import it.zerono.mods.zerocore.lib.data.nbt.ISyncableEntity;
import it.zerono.mods.zerocore.lib.data.nbt.IVersionAwareSyncableEntity;
import it.zerono.mods.zerocore.lib.data.nbt.NBTHelper;
import it.zerono.mods.zerocore.lib.event.Event;
import it.zerono.mods.zerocore.lib.event.IEvent;
import it.zerono.mods.zerocore.lib.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A base class for modded tile entities
 */
public abstract class AbstractModBlockEntity
        extends TileEntity
        implements IBlockStateUpdater, ISyncableEntity, IDebuggable {

    @Deprecated
    public final IEvent<Runnable> DataUpdate;

    public AbstractModBlockEntity(final TileEntityType<?> type) {

        super(type);
        this._commandDispatcher = (source, name, parameters) -> {};

        this.DataUpdate = new Event<>();
    }

    public Block getBlockType() {
        return this.getBlockState().getBlock();
    }

    //region Logical sides and deferred execution helpers

    public void callOnLogicalSide(final Runnable serverCode, final Runnable clientCode) {

        if (null != this.level) {
            CodeHelper.callOnLogicalSide(this.level, serverCode, clientCode);
        }
    }

    public <T> T callOnLogicalSide(final Supplier<T> serverCode, final Supplier<T> clientCode, final Supplier<T> invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue.get();
        }

        return CodeHelper.callOnLogicalSide(this.level, serverCode, clientCode);
    }

    public boolean callOnLogicalSide(final BooleanSupplier serverCode, final BooleanSupplier clientCode) {
        return null != this.level && CodeHelper.callOnLogicalSide(this.level, serverCode, clientCode);
    }

    public int callOnLogicalSide(final IntSupplier serverCode, final IntSupplier clientCode, final IntSupplier invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue.getAsInt();
        }

        return CodeHelper.callOnLogicalSide(this.level, serverCode, clientCode);
    }

    public long callOnLogicalSide(final LongSupplier serverCode, final LongSupplier clientCode, final LongSupplier invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue.getAsLong();
        }

        return CodeHelper.callOnLogicalSide(this.level, serverCode, clientCode);
    }

    public double callOnLogicalSide(final DoubleSupplier serverCode, final DoubleSupplier clientCode, final DoubleSupplier invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue.getAsDouble();
        }

        return CodeHelper.callOnLogicalSide(this.level, serverCode, clientCode);
    }

    public void callOnLogicalServer(final Runnable code) {

        if (null != this.level) {
            CodeHelper.callOnLogicalServer(this.level, code);
        }
    }

    public void callOnLogicalServer(final Consumer<World> code) {

        if (null != this.level && CodeHelper.calledByLogicalServer(this.level)) {
            code.accept(this.level);
        }
    }

    public <T> T callOnLogicalServer(final Supplier<T> code, final Supplier<T> invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue.get();
        }

        return CodeHelper.callOnLogicalServer(this.level, code, invalidSideReturnValue);
    }

    public boolean callOnLogicalServer(final BooleanSupplier code) {
        return null != this.level && CodeHelper.callOnLogicalServer(this.level, code);
    }

    public int callOnLogicalServer(final IntSupplier code, final int invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue;
        }

        return CodeHelper.callOnLogicalServer(this.level, code, invalidSideReturnValue);
    }

    public long callOnLogicalServer(final LongSupplier code, final long invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue;
        }

        return CodeHelper.callOnLogicalServer(this.level, code, invalidSideReturnValue);
    }

    public double callOnLogicalServer(final DoubleSupplier code, final double invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue;
        }

        return CodeHelper.callOnLogicalServer(this.level, code, invalidSideReturnValue);
    }

    public void callOnLogicalClient(final Runnable code) {

        if (null != this.level) {
            CodeHelper.callOnLogicalClient(this.level, code);
        }
    }

    public void callOnLogicalClient(final Consumer<World> code) {

        if (null != this.level && CodeHelper.calledByLogicalClient(this.level)) {
            code.accept(this.level);
        }
    }

    public <T> T callOnLogicalClient(final Supplier<T> code, final Supplier<T> invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue.get();
        }

        return CodeHelper.callOnLogicalClient(this.level, code, invalidSideReturnValue);
    }

    public boolean callOnLogicalClient(final BooleanSupplier code) {
        return null != this.level && CodeHelper.callOnLogicalClient(this.level, code);
    }

    public int callOnLogicalClient(final IntSupplier code, final int invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue;
        }

        return CodeHelper.callOnLogicalClient(this.level, code, invalidSideReturnValue);
    }

    public long callOnLogicalClient(final LongSupplier code, final long invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue;
        }

        return CodeHelper.callOnLogicalClient(this.level, code, invalidSideReturnValue);
    }

    public double callOnLogicalClient(final DoubleSupplier code, final double invalidSideReturnValue) {

        if (null == this.level) {
            return invalidSideReturnValue;
        }

        return CodeHelper.callOnLogicalClient(this.level, code, invalidSideReturnValue);
    }

    //endregion
    //region GUI management

    /**
     * Check if the tile entity has a GUI or not
     * Override in derived classes to return true if your tile entity got a GUI
     */
    public boolean canOpenGui(final World world, final BlockPos position, final BlockState state) {
        return false;
    }

    /**
     * Send a message to the player client to open this TileEntity GUI
     *
     * @param player The player to open the GUI for
     * @return true if the message was sent, false otherwise
     */
    public boolean openGui(final ServerPlayerEntity player) {
        return this.openGuiOnClient(player, buffer -> {});
    }

    /**
     * Send a message to the player client to open this TileEntity GUI
     *
     * @param player          The player to open the GUI for
     * @param extraDataWriter Consumer for extra data to be sent to the client
     * @return true if the message was sent, false otherwise
     */
    public boolean openGui(final ServerPlayerEntity player, final Consumer<PacketBuffer> extraDataWriter) {
        return this.openGuiOnClient(player, extraDataWriter);
    }

    /**
     * Retrive the client-side AbstractModBlockEntity associated with the GUI being opened from the server
     *
     * @param networkData   The data received from the server and generated from the {@code openGui} methods
     * @param <T>           The type of the AbstractModBlockEntity being retrived
     * @return              The client side AbstractModBlockEntity
     * @throws              NullPointerException if nothing can be retrieved
     */
    public static <T extends AbstractModBlockEntity> T getGuiClientBlockEntity(final PacketBuffer networkData) {
        return WorldHelper.<T>getClientTile(networkData.readBlockPos()).orElseThrow(NullPointerException::new);
    }

    //endregion
    //region TileEntity synchronization

    @Override
    public void load(final BlockState state, final CompoundNBT data) {

        super.load(state, data);
        this.syncEntityDataFrom(data, SyncReason.FullSync);
    }

    @Override
    public CompoundNBT save(final CompoundNBT data) {
        return this.syncEntityDataTo(super.save(data), SyncReason.FullSync);
    }

    /**
     * Called when the chunk's TE update tag, gotten from {@link #getUpdateTag()}, is received on the client.
     * <p>
     * Used to handle this tag in a special way. By default this simply calls readFromNBT(NBTTagCompound).
     *
     * @param data The {@link CompoundNBT} sent from {@link #getUpdateTag()}
     */
    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT data) {

        super.load(state, data);
        this.syncEntityDataFrom(data, SyncReason.NetworkUpdate);
    }

    /**
     * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
     * many blocks change at once. This compound comes back to you clientside in handleUpdateTag
     */
    @Override
    public CompoundNBT getUpdateTag() {
        return this.syncEntityDataTo(super.getUpdateTag(), SyncReason.NetworkUpdate);
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net    The NetworkManager the packet originated from
     * @param packet The data packet
     */
    @Override
    public final void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {

//        this.syncDataFrom(packet.getNbtCompound(), ISyncableEntity.SyncReason.NetworkUpdate);
//        this.onDataUpdate();

        this.syncEntityDataFrom(packet.getTag(), SyncReason.NetworkUpdate);
    }

    /**
     * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
     * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
     */
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
//
//        final CompoundNBT data = new CompoundNBT();
//
//        this.syncDataTo(data, ISyncableEntity.SyncReason.NetworkUpdate);
//        return new SUpdateTileEntityPacket(this.getPos(), 0, data);

        return new SUpdateTileEntityPacket(this.getBlockPos(), 0,
                this.syncEntityDataTo(new CompoundNBT(), SyncReason.NetworkUpdate));
    }

    protected void onDataUpdate() {
        this.DataUpdate.raise(Runnable::run);
    }

    private void syncEntityDataFrom(CompoundNBT data, SyncReason syncReason) {

        CompoundNBT dataToSync = data;

        if (syncReason.isFullSync() && data.contains("zcvase_version") && data.contains("zcvase_payload")) {

            final CompoundNBT payload = dataToSync = data.getCompound("zcvase_payload");

            if (this instanceof IVersionAwareSyncableEntity) {

                final IVersionAwareSyncableEntity vase = (IVersionAwareSyncableEntity)this;
                final int dataVersion = data.getInt("zcvase_version");

                if (vase.syncGetDataCurrentVersion() > dataVersion) {
                    dataToSync = vase.syncGetVersionConverter(dataVersion).apply(payload);
                }
            }
        }

        this.syncDataFrom(dataToSync, syncReason);
        this.onDataUpdate();
    }

    private CompoundNBT syncEntityDataTo(CompoundNBT data, SyncReason syncReason) {

        if (syncReason.isFullSync()) {

            data.putInt("zcvase_version", this.syncGetEntityDataCurrentVersion());
            data.put("zcvase_payload", this.syncDataTo(new CompoundNBT(), syncReason));
            return data;

        } else {

            return this.syncDataTo(data, syncReason);
        }
    }

    private int syncGetEntityDataCurrentVersion() {
        return this instanceof IVersionAwareSyncableEntity ? ((IVersionAwareSyncableEntity) this).syncGetDataCurrentVersion() : -1;
    }

    //endregion
    //region Tile commands

    /**
     * Send a command to the corresponding Tile Entity on the server side
     *
     * @param name the command name
     */
    public void sendCommandToServer(final String name) {
        this.sendCommandToServer(name, NBTHelper.EMPTY_COMPOUND);
    }

    /**
     * Send a command to the corresponding Tile Entity on the server side
     *
     * @param name the command name
     * @param parameters the parameters for the command
     */
    public void sendCommandToServer(final String name, final CompoundNBT parameters) {
        Network.HANDLER.sendToServer(TileCommandMessage.create(this, name, parameters));
    }

    /**
     * Send a command to the corresponding Tile Entity on the client side
     *
     * @param name the command name
     */
    public void sendCommandToPlayer(final ServerPlayerEntity player, final String name) {
        this.sendCommandToPlayer(player, name, NBTHelper.EMPTY_COMPOUND);
    }

    /**
     * Send a command to the corresponding Tile Entity on the client side
     *
     * @param name the command name
     * @param parameters the parameters for the command
     */
    public void sendCommandToPlayer(final ServerPlayerEntity player, final String name, final CompoundNBT parameters) {
        Network.HANDLER.sendToPlayer(TileCommandMessage.create(this, name, parameters), player);
    }

    /**
     * Handle a command coming from the corresponding Tile Entity on the other side
     *
     * @param source the source side
     * @param name the command name
     * @param parameters the parameters for the command, if any
     */
    public void handleCommand(final LogicalSide source, final String name, final CompoundNBT parameters) {
        this._commandDispatcher.dispatch(source, name, parameters);
    }

    protected void setCommandDispatcher(final ITileCommandDispatcher dispatcher) {
        this._commandDispatcher = Objects.requireNonNull(dispatcher);
    }

    //endregion
    //region Chunk and block updates

    /*
    @Override
    public void onChunkUnload() {
        if (!tileEntityInvalid)
            this.invalidate();
    }*/

    public void markChunkDirty() {

        final World world = this.getLevel();

        if (null != world) {
            world.blockEntityChanged(this.getBlockPos(), this);
        }
    }

    public void callNeighborBlockChange() {

        final World world = this.getLevel();

        if (null != world) {
            WorldHelper.notifyNeighborsOfStateChange(world, this.getBlockPos(), this.getBlockState().getBlock());
        }
    }

    @Deprecated // not implemented
    public void callNeighborTileChange() {
        //this.WORLD.func_147453_f(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
    }

    public void notifyBlockUpdate() {

        final World world = this.getLevel();

        if (null != world) {
            WorldHelper.notifyBlockUpdate(world, this.getBlockPos(), null, null);
        }
    }

    public void notifyBlockUpdate(BlockState oldState, BlockState newState) {

        final World world = this.getLevel();

        if (null != world) {
            WorldHelper.notifyBlockUpdate(world, this.getBlockPos(), oldState, newState);
        }
    }

    public static <T extends AbstractModBlockEntity> void notifyBlockUpdate(final Collection<T> entities) {
        entities.forEach(AbstractModBlockEntity::notifyBlockUpdate);
    }

    public static <T extends AbstractModBlockEntity> void notifyBlockUpdate(final Stream<T> entities) {
        entities.forEach(AbstractModBlockEntity::notifyBlockUpdate);
    }

    public void notifyTileEntityUpdate() {

        final World world = this.getLevel();

        if (null != world) {

            this.setChanged();
            WorldHelper.notifyBlockUpdate(world, this.getBlockPos(), null, null);
        }
    }

    public void markForRenderUpdate() {
        WorldHelper.markBlockRangeForRenderUpdate(this.getBlockPos(), this.getBlockPos());
    }

    public void requestClientRenderUpdate() {

        if (null != this.level) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockType(), EVENT_CLIENT_RENDER_UPDATE, 0);
        }
    }

    /**
     * See {@link Block#eventReceived} for more information. This must return true serverside before it is called
     * clientside.
     *
     * @param id
     * @param type
     */
    @Override
    public boolean triggerEvent(int id, int type) {

        if (null != this.level) {

            switch (id) {

                case EVENT_CLIENT_RENDER_UPDATE:
                    this.callOnLogicalClient(this::markForRenderUpdate);
                    return true;
            }
        }

        return super.triggerEvent(id, type);
    }

    //endregion
    //region IBlockStateUpdater

    @Override
    public void updateBlockState(BlockState currentState, IWorld world, BlockPos position,
                                 @Nullable TileEntity tileEntity, int updateFlags) {

        final Block block = currentState.getBlock();

        if (block instanceof IBlockStateUpdater) {
            ((IBlockStateUpdater) block).updateBlockState(currentState, world, position, tileEntity, updateFlags);
        } else {
            world.setBlock(position, this.buildUpdatedState(currentState, world, position, tileEntity), updateFlags);
        }
    }

    @Nonnull
    @Override
    public BlockState buildUpdatedState(BlockState currentState, IBlockReader reader, BlockPos position,
                                        @Nullable TileEntity tileEntity) {
        return currentState;
    }

    //endregion
    //region IDebuggable

    @Override
    public void getDebugMessages(final LogicalSide side, final IDebugMessages messages) {
        messages.addUnlocalized("Tile Entity class: %1$s", this.getClass().getSimpleName());
    }

    //endregion
    //region internals

    /**
     * Send a message to the player client to open this TileEntity GUI
     *
     * @param player          The target player
     * @param extraDataWriter Optional consumer for optional data
     * @return true if the message was sent, false otherwise
     */
    private boolean openGuiOnClient(final ServerPlayerEntity player, final Consumer<PacketBuffer> extraDataWriter) {

//        if (this instanceof INamedContainerProvider && !(player instanceof FakePlayer) &&
//                CodeHelper.calledByLogicalServer(player.getEntityWorld())) {
//
//            final Consumer<PacketBuffer> positionWriter = buffer -> buffer.writeBlockPos(this.getPos());
//
//            NetworkHooks.openGui(player, (INamedContainerProvider) this, positionWriter.andThen(extraDataWriter));
//            return true;
//        }
//
//        return false;

        return this.callOnLogicalServer(() -> {

            if (this instanceof INamedContainerProvider && !(player instanceof FakePlayer)) {

                final Consumer<PacketBuffer> positionWriter = buffer -> buffer.writeBlockPos(this.getBlockPos());

                NetworkHooks.openGui(player, (INamedContainerProvider) this, positionWriter.andThen(extraDataWriter));
                return true;

            } else {

                return false;
            }
        });
    }

    private static final int EVENT_CLIENT_RENDER_UPDATE = 1;

    private ITileCommandDispatcher _commandDispatcher;

    //endregion
}
