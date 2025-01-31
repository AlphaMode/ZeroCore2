/*
 *
 * AbstractScreen.java
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

package it.zerono.mods.zerocore.base.client.screen;

import it.zerono.mods.zerocore.lib.block.AbstractModBlockEntity;
import it.zerono.mods.zerocore.lib.client.gui.*;
import it.zerono.mods.zerocore.lib.client.gui.control.AbstractButtonControl;
import it.zerono.mods.zerocore.lib.client.gui.control.Label;
import it.zerono.mods.zerocore.lib.client.gui.control.Panel;
import it.zerono.mods.zerocore.lib.client.gui.control.SlotsGroup;
import it.zerono.mods.zerocore.lib.client.gui.layout.FixedLayoutEngine;
import it.zerono.mods.zerocore.lib.client.gui.layout.ILayoutEngine;
import it.zerono.mods.zerocore.lib.client.gui.sprite.ISprite;
import it.zerono.mods.zerocore.lib.client.gui.sprite.ISpriteTextureMap;
import it.zerono.mods.zerocore.lib.client.gui.sprite.Sprite;
import it.zerono.mods.zerocore.lib.client.gui.sprite.SpriteTextureMap;
import it.zerono.mods.zerocore.lib.data.gfx.Colour;
import it.zerono.mods.zerocore.lib.item.inventory.PlayerInventoryUsage;
import it.zerono.mods.zerocore.lib.item.inventory.container.ModTileContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractScreen<T extends AbstractModBlockEntity & INamedContainerProvider, C extends ModTileContainer<T>>
        extends ModTileContainerScreen<T, C> {

    public static final int DEFAULT_GUI_WIDTH = 224;
    public static final int DEFAULT_GUI_HEIGHT = 166;

    public static final Style STYLE_TOOLTIP_TITLE = Style.EMPTY
            .withColor(TextFormatting.YELLOW)
            .withBold(true);

    public static final Style STYLE_TOOLTIP_VALUE = Style.EMPTY
            .withColor(TextFormatting.DARK_AQUA)
            .withBold(true);

    public static final Style STYLE_TOOLTIP_INFO = Style.EMPTY
            .withColor(TextFormatting.DARK_PURPLE)
            .withItalic(true);

    protected AbstractScreen(final C container, final PlayerInventory inventory, final PlayerInventoryUsage inventoryUsage,
                             final ITextComponent title, final NonNullSupplier<SpriteTextureMap> mainTextureSupplier) {
        this(container, inventory, inventoryUsage, title, DEFAULT_GUI_WIDTH, DEFAULT_GUI_HEIGHT, mainTextureSupplier.get());
    }

    protected AbstractScreen(final C container, final PlayerInventory inventory, final PlayerInventoryUsage inventoryUsage,
                             final ITextComponent title, final int guiWidth, final int guiHeight,
                             final NonNullSupplier<SpriteTextureMap> mainTextureSupplier) {
        this(container, inventory, inventoryUsage, title, guiWidth, guiHeight, mainTextureSupplier.get());
    }

    protected AbstractScreen(final C container, final PlayerInventory inventory, final PlayerInventoryUsage inventoryUsage,
                             final ITextComponent title, final int guiWidth, final int guiHeight,
                             final SpriteTextureMap mainTexture) {

        super(container, inventory, title, guiWidth, guiHeight);
        this._mainTextMap = mainTexture;

        switch (inventoryUsage) {

            default:
            case None:
                this._invMainSprite = this._invHotBarSprite = this._invSingleSprite = Sprite.EMPTY;
                break;

            case HotBar:
                this._invHotBarSprite = this.createInventoryHotBarSprite();
                this._invMainSprite = Sprite.EMPTY;
                this._invSingleSprite = this.createInventorySingleSprite();
                break;

            case MainInventory:
                this._invHotBarSprite = Sprite.EMPTY;
                this._invMainSprite = this.createInventoryMainSprite();
                this._invSingleSprite = this.createInventorySingleSprite();
                break;

            case Both:
                this._invHotBarSprite = this.createInventoryHotBarSprite();
                this._invMainSprite = this.createInventoryMainSprite();
                this._invSingleSprite = this.createInventorySingleSprite();
                break;
        }

        this._contentPanel = new Panel(this, "content");
        this._helpButton = null;
    }

    protected void addControl(final IControl control) {
        this._contentPanel.addControl(control);
    }

    protected void setContentPanelBackground(final ISprite sprite) {
        this._contentPanel.setBackground(sprite);
    }

    protected void setContentPanelBackground(final Colour colour) {
        this._contentPanel.setBackground(colour);
    }

    protected void setContentLayoutEngine(final ILayoutEngine engine) {
        this._contentPanel.setLayoutEngine(engine);
    }

    protected void addPatchouliHelpButton(final ResourceLocation bookId, final ResourceLocation entryId, final int pageNum) {
        this._helpButton = this.createPatchouliHelpButton(bookId, entryId, pageNum);
    }

    protected void setButtonSpritesAndOverlayForState(final AbstractButtonControl button,
                                                      final ButtonState standardState,
                                                      final NonNullSupplier<ISprite> standardSprite) {
        this.setButtonSpritesAndOverlayForState(button, standardState,standardSprite.get());
    }

    protected void setButtonSpritesAndOverlayForState(final AbstractButtonControl button,
                                                      final ButtonState standardState,
                                                      final ISprite standardSprite) {

        button.setIconForState(standardSprite, standardState);

        ISprite withOverlay;

        withOverlay = standardSprite.copyWith(BaseIcons.Button16x16HightlightOverlay.get());
        button.setIconForState(withOverlay, standardState.getHighlighted());

        withOverlay = standardSprite.copyWith(BaseIcons.Button16x16DisabledOverlay.get());
        button.setIconForState(withOverlay, standardState.getDisabled());
    }

    @Nullable
    protected IControl getTitleBarWidget() {
        return null;
    }

    //region slot groups

    protected SlotsGroup createSingleSlotGroupControl(final String controlName, final String inventorySlotsGroupName) {
        return this.createMonoSlotGroupControl(controlName, inventorySlotsGroupName, this._invSingleSprite, 1);
    }

    protected SlotsGroup createPlayerHotBarSlotsGroupControl() {
        return this.createPlayerHotBarSlotsGroupControl(this._invHotBarSprite, 1);
    }

    protected SlotsGroup createPlayerInventorySlotsGroupControl() {
        return this.createPlayerInventorySlotsGroupControl(this._invMainSprite, 1);
    }

    //endregion
    //region ModTileContainerScreen

    /**
     * Called when this screen is being created for the first time.
     * Override to handle this event
     */
    @Override
    protected void onScreenCreate() {

        super.onScreenCreate();

        final int guiWidth = this.getGuiWidth();
        final int guiHeight = this.getGuiHeight();

        final Panel mainPanel = new Panel(this, "mainPanel");
        final Panel titlePanel = new Panel(this, "titlePanel");
        final int contentHeight = guiHeight - TITLE_PANEL_HEIGHT;

        // - main panel

        mainPanel.setDesiredDimension(DesiredDimension.Height, guiHeight);
        mainPanel.setDesiredDimension(DesiredDimension.Width, guiWidth);
        mainPanel.setLayoutEngineHint(FixedLayoutEngine.hint(0, 0, guiWidth, guiHeight));
        mainPanel.setBackground(this._mainTextMap.sprite().ofSize(guiWidth, guiHeight).build());
        mainPanel.setLayoutEngine(new FixedLayoutEngine().setZeroMargins());

        // - title panel

        titlePanel.setDesiredDimension(DesiredDimension.Height, TITLE_PANEL_HEIGHT);
        titlePanel.setDesiredDimension(DesiredDimension.Width, guiWidth);
        titlePanel.setLayoutEngineHint(FixedLayoutEngine.hint(0, 0, guiWidth, TITLE_PANEL_HEIGHT));
        titlePanel.setLayoutEngine(new FixedLayoutEngine().setZeroMargins());

        final IControl titleWidget = this.getTitleBarWidget();
        int titlePosX = 7, titleWidth = guiWidth - 14;

        if (null != titleWidget) {

            titleWidget.setLayoutEngineHint(FixedLayoutEngine.hint(7, 7, 10, 10));
            titlePanel.addControl(titleWidget);
            titlePosX += 10;
            titleWidth -= 10;
        }

        if (null != this._helpButton) {

            this._helpButton.setLayoutEngineHint(FixedLayoutEngine.hint(guiWidth - 18, 6, 12, 12));
            titlePanel.addControl(this._helpButton);
            titleWidth -= 12;
        }

        final Label title = new Label(this, "title", this.getTitle());

        title.setPadding(2);
        title.setColor(Colour.BLACK);
        title.setAutoSize(false);
        title.setLayoutEngineHint(FixedLayoutEngine.hint(titlePosX, 7, titleWidth, 12));
        titlePanel.addControl(title);

        // - content panel

        // MC calls the init() method (witch rise onScreenCreated()) also when the main windows is resized: clear
        // the controls in the content panel to avoid duplications
        this._contentPanel.removeControls();

        this._contentPanel.setLayoutEngine(new FixedLayoutEngine());
        this._contentPanel.setDesiredDimension(DesiredDimension.Height, contentHeight);
        this._contentPanel.setDesiredDimension(DesiredDimension.Width, guiWidth);
        this._contentPanel.setLayoutEngineHint(FixedLayoutEngine.hint(0, TITLE_PANEL_HEIGHT, guiWidth, contentHeight));

        // create main window

        mainPanel.addControl(titlePanel, this._contentPanel);
        this.createWindow(mainPanel, true);
    }

    //endregion
    //region internals

    private ISprite createInventoryHotBarSprite() {
        return this._mainTextMap.sprite().from(0, 202).ofSize(162, 18).build();
    }

    private ISprite createInventoryMainSprite() {
        return this._mainTextMap.sprite().from(0, 202).ofSize(162, 54).build();
    }

    private ISprite createInventorySingleSprite() {
        return this._mainTextMap.sprite().from(0, 202).ofSize(18, 18).build();
    }

    private static final int TITLE_PANEL_HEIGHT = 21;

    private final ISpriteTextureMap _mainTextMap;
    private final ISprite _invMainSprite;
    private final ISprite _invHotBarSprite;
    private final ISprite _invSingleSprite;

    private final IControlContainer _contentPanel;
    private IControl _helpButton;

    //endregion
}
