/*
 *
 * GlassBlock.java
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

package it.zerono.mods.zerocore.base.multiblock.part;

import it.zerono.mods.zerocore.lib.block.multiblock.IMultiblockPartType;
import it.zerono.mods.zerocore.lib.block.property.BlockFacingsProperty;
import it.zerono.mods.zerocore.lib.multiblock.IMultiblockController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class GlassBlock <Controller extends IMultiblockController<Controller>,
                         PartType extends Enum<PartType> & IMultiblockPartType>
        extends GenericDeviceBlock<Controller, PartType> {

    public GlassBlock(final MultiblockPartProperties<PartType> properties) {
        super(properties);
    }

    //region Block

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return this == adjacentBlockState.getBlock();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0F;
    }

    //endregion
    //region ModBlock

    @Override
    protected void buildBlockState(final StateDefinition.Builder<Block, BlockState> builder) {

        super.buildBlockState(builder);
        builder.add(BlockFacingsProperty.FACINGS);
    }

    @Override
    protected BlockState buildDefaultState(final BlockState state) {
        return super.buildDefaultState(state).setValue(BlockFacingsProperty.FACINGS, BlockFacingsProperty.None);
    }

    //endregion
}
