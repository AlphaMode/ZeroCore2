/*
 *
 * ZeroCore.java
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

package it.zerono.mods.zerocore;

import com.mojang.brigadier.CommandDispatcher;
import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import it.zerono.mods.zerocore.internal.Lib;
import it.zerono.mods.zerocore.internal.command.ZeroCoreCommand;
import it.zerono.mods.zerocore.internal.gamecontent.Content;
import it.zerono.mods.zerocore.internal.network.Network;
import it.zerono.mods.zerocore.internal.proxy.ClientProxy;
import it.zerono.mods.zerocore.internal.proxy.IProxy;
import it.zerono.mods.zerocore.internal.proxy.ServerProxy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public final class ZeroCore implements ModInitializer {

    public static final String MOD_ID = "zerocore";

    public static ZeroCore getInstance() {
        return s_instance;
    }

    public static IProxy getProxy() {
        return s_proxy;
    }

    public static ResourceLocation newID(final String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {

        s_instance = this;
        s_proxy = EnvExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
        Network.initialize();
        CommandRegistrationCallback.EVENT.register(this::onRegisterCommands);
        Lib.initialize();
        Content.initialize();
    }

    //endregion
    //region internals

    private void onRegisterCommands(final CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        ZeroCoreCommand.register(dispatcher);
    }

    private static ZeroCore s_instance;
    private static IProxy s_proxy;

    //endregion
}
