/*
 *
 * DebuggableHelper.java
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

package it.zerono.mods.zerocore.lib;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.IFluidHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandler;

public final class DebuggableHelper {

    public static void getDebugMessagesFor(final IDebugMessages messages, final ItemStack stack) {

        if (stack.isEmpty()) {
            messages.addUnlocalized("ItemStack: EMPTY");
        } else {
            messages.add(new TextComponent("ItemStack: [")
                    .append(stack.getHoverName())
                    .append("] ")
                    .append(Integer.toString(stack.getCount()))
                    .append(" / ")
                    .append(Integer.toString(stack.getMaxStackSize())));
        }
    }

    public static void getDebugMessagesFor(final IDebugMessages messages, final FluidStack stack) {

        if (stack.isEmpty()) {
            messages.addUnlocalized("FluidStack: EMPTY");
        } else {
            messages.add(new TextComponent("FluidStack: [")
                    .append(stack.getDisplayName())
                    .append("] ")
                    .append(Long.toString(stack.getAmount())));
        }
    }

    public static void getDebugMessagesFor(final IDebugMessages messages, final IItemHandler handler) {

        final int slots = handler.getSlots();

        messages.addUnlocalized("Slots count: %1$d", slots);

        for (int i = 0; i < slots; ++i) {
            messages.add(handler.getStackInSlot(i), DebuggableHelper::getDebugMessagesFor,
                    new TextComponent(String.format("[%d]", i)));
        }
    }

    public static void getDebugMessagesFor(final IDebugMessages messages, final IFluidHandler handler) {

        final int tanks = handler.getTanks();

        messages.addUnlocalized("Tanks count: %1$d", tanks);

        for (int i = 0; i < tanks; ++i) {
            messages.add(handler.getFluidInTank(i), DebuggableHelper::getDebugMessagesFor,
                    new TextComponent(String.format("[%d]", i)));
        }
    }
}
