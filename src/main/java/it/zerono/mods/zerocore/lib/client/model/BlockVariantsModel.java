/*
 *
 * BlockVariantsModel.java
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

package it.zerono.mods.zerocore.lib.client.model;

import com.google.common.collect.ImmutableList;
import io.github.fabricators_of_create.porting_lib.model.IModelData;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.zerono.mods.zerocore.lib.client.model.data.GenericProperties;
import it.zerono.mods.zerocore.lib.client.render.ModRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BlockVariantsModel
        extends AbstractDynamicBakedModel {

    public BlockVariantsModel(final int blocksCount, final boolean ambientOcclusion, final boolean guid3D, final boolean builtInRenderer) {

        super(ambientOcclusion, guid3D, builtInRenderer);
        this._entries = new Int2ObjectArrayMap<>(blocksCount);
    }

    @SuppressWarnings("unused")
    public void addBlock(int blockId, boolean hasGeneralQuads, /*int particlesModelIndex,*/ BakedModel... variants) {
        this._entries.put(blockId, new BlockEntry(/*particlesModelIndex, */hasGeneralQuads, variants));
    }

    @SuppressWarnings("unused")
    public void addBlock(int blockId, boolean hasGeneralQuads, /*int particlesModelIndex,*/ List<BakedModel> variants) {
        this._entries.put(blockId, new BlockEntry(/*particlesModelIndex, */hasGeneralQuads, variants));
    }

    //region IDynamicBakedModel


    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state, @Nullable Direction side, Random rand) {
        return null;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        if(blockView instanceof RenderAttachedBlockView renderAttachedBlockView) {
            IModelData data = (IModelData) renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
            if (data.hasProperty(GenericProperties.ID) && data.hasProperty(GenericProperties.VARIANT_INDEX) && this.containsBlock(data)) {
                this.getBlock(data).emitQuads(GenericProperties.getVariantIndex(data), blockView, state, pos, randomSupplier, context);
            }
        }
        ((FabricBakedModel)ModRenderHelper.getMissingModel()).emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.fallbackConsumer().accept(this);
    }
// TODO: PORT
//    @Override
//    public TextureAtlasSprite getParticleIcon(final IModelData data) {
//
//        if (data.hasProperty(GenericProperties.ID) && data.hasProperty(GenericProperties.VARIANT_INDEX) && this.containsBlock(data)) {
//            return this.getBlock(data).getParticleTexture(GenericProperties.getVariantIndex(data), data);
//        }
//
//        return ModRenderHelper.getMissingModel().getParticleIcon(EmptyModelData.INSTANCE);
//    }

    //endregion
    //region internals

    private boolean containsBlock(final IModelData data) {
        return data.hasProperty(GenericProperties.ID) && this._entries.containsKey(GenericProperties.getId(data));
    }

    private BlockEntry getBlock(final IModelData data) {
        return this._entries.get(GenericProperties.getId(data));
    }

    private static class BlockEntry {

        BlockEntry(/*final int particlesModelIndex,*/ final boolean hasGeneralQuads, final BakedModel... variants) {

            this._variants = ImmutableList.copyOf(variants);
            //this._particlesModelIndex = particlesModelIndex;
            this._noGeneralQuads = !hasGeneralQuads;
        }

        BlockEntry(/*final int particlesModelIndex,*/ final boolean hasGeneralQuads, final List<BakedModel> variants) {

            this._variants = ImmutableList.copyOf(variants);
            //this._particlesModelIndex = particlesModelIndex;
            this._noGeneralQuads = !hasGeneralQuads;
        }

        void emitQuads(final int variantIndex, BlockAndTintGetter blockAndTintGetter, @Nullable BlockState state, BlockPos pos,
                       Supplier<Random> rand, RenderContext context) {
            if (!this._noGeneralQuads) {
                ((FabricBakedModel)this._variants.get(variantIndex)).emitBlockQuads(blockAndTintGetter, state, pos, rand, context);
            }
        }

        TextureAtlasSprite getParticleTexture(final int variantIndex, IModelData data) {
            return this._variants.get(variantIndex).getParticleIcon(/*data*/);
        }

        //region internals

        private final List<BakedModel> _variants;
        //private final int _particlesModelIndex;
        private final boolean _noGeneralQuads;

        //endregion
    }

    private final Int2ObjectMap<BlockEntry> _entries;

    //endregion
}
