/*
 *
 * CuboidPartVariantsModel.java
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

package it.zerono.mods.zerocore.lib.client.model.multiblock;

import io.github.fabricators_of_create.porting_lib.model.IModelData;
import it.zerono.mods.zerocore.lib.client.model.BlockVariantsModel;
import it.zerono.mods.zerocore.lib.client.model.data.multiblock.PartProperties;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CuboidPartVariantsModel extends BlockVariantsModel {

    public CuboidPartVariantsModel(final BakedModel template, final int blocksCount, final boolean ambientOcclusion) {

        super(blocksCount, ambientOcclusion, true, false);
        this._template = template;
    }

    //region IDynamicBakedModel

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction renderSide,
                                    Random rand, IModelData data) {

        if (null != renderSide && data.hasProperty(PartProperties.OUTWARD_FACING) && PartProperties.getOutwardFacing(data).except(renderSide) /*.isSet(renderSide)*/) {
            return this._template.getQuads(state, renderSide, rand, data);
        }

        return super.getQuads(state, renderSide, rand, data);
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        context.pushTransform(quad -> {
            return true;
        });
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }

    //endregion
    //region internals

    private final BakedModel _template;

    //endregion
}
