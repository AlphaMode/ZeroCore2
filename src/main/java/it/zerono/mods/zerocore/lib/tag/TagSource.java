/*
 *
 * TagSource.java
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

package it.zerono.mods.zerocore.lib.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import io.github.fabricators_of_create.porting_lib.util.NonNullFunction;
import io.github.fabricators_of_create.porting_lib.util.NonNullSupplier;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess"})
public class TagSource<T> {

    public TagSource(final NonNullSupplier<Registry<T>> registrySupplier) {
        this._registry = registrySupplier;
    }

    public TagKey<T> createKey(final ResourceLocation id) {
        return TagKey.create(this._registry.get().key(), id);
    }

    public TagKey<T> createKey(final String id) {
        return this.createKey(new ResourceLocation(id));
    }

    public TagKey<T> createKey(final String id, NonNullFunction<String, ResourceLocation> factory) {
        return this.createKey(factory.apply(id));
    }

    public boolean isTagged(final T object, final TagKey<T> tagKey) {

        final Registry<T> registry = this._registry.get();

        return registry.getResourceKey(object)
                .map(registry::getHolderOrThrow)
                .map(holder -> holder.is(tagKey))
                .orElse(false);
    }

    public boolean exist(final TagKey<T> tagKey) {
        return this._registry.get().getTag(tagKey).isPresent();
    }

    public boolean existWithContent(final TagKey<T> tagKey) {
        return this.getObjects(tagKey).size() > 0;
    }

    public List<T> getObjects(final TagKey<T> tagKey) {

        final List<T> list = new LinkedList<>();

        this._registry.get().getTagOrEmpty(tagKey).forEach(holder -> list.add(holder.value()));

        return list.isEmpty() ? ObjectLists.emptyList() : ObjectLists.unmodifiable(new ObjectArrayList<>(list));
    }

    public Optional<T> getFirstObject(final TagKey<T> tagKey) {

        final List<T> objects = this.getObjects(tagKey);

        return objects.isEmpty() ? Optional.empty() : Optional.ofNullable(objects.get(0));
    }

    public List<TagKey<T>> getTags(final T object) {

        final Registry<T> registry = this._registry.get();
        final List<TagKey<T>> list = registry.getResourceKey(object)
                .map(registry::getHolderOrThrow)
                .map(Holder::tags)
                .map(s -> s.collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return list.isEmpty() ? ObjectLists.emptyList() : ObjectLists.unmodifiable(new ObjectArrayList<>(list));
    }

    //region internals

    private final NonNullSupplier<Registry<T>> _registry;

    //endregion
}
