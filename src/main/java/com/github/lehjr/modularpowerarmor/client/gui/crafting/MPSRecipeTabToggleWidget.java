package com.github.lehjr.modularpowerarmor.client.gui.crafting;

import com.mojang.blaze3d.platform.GlStateManager;
import com.github.lehjr.mpalib.client.gui.geometry.DrawableRect;
import com.github.lehjr.mpalib.client.gui.geometry.Point2D;
import com.github.lehjr.mpalib.client.sound.Musique;
import com.github.lehjr.mpalib.math.Colour;
import com.github.lehjr.modularpowerarmor.client.sound.SoundDictionary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeTabToggleWidget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MPSRecipeTabToggleWidget extends RecipeTabToggleWidget {
    private final RecipeBookCategories category;
    private final Colour activeColor = new Colour(0.1F, 0.3F, 0.4F, 0.7F);
    private final Colour inactiveColor = Colour.DARKBLUE.withAlpha(0.8);

    DrawableRect tabRectangle;
    public MPSRecipeTabToggleWidget(RecipeBookCategories category) {
        super(category);
        this.initTextureValues(153, 2, 35, 0, MPSRecipeBookGui.RECIPE_BOOK);
        this.category = category;
        this.tabRectangle = new DrawableRect(0, 0, 0, 0, inactiveColor, Colour.LIGHTBLUE.withAlpha(0.8));
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        int xChange = this.stateTriggered ? 2 : 0;
        tabRectangle.setTargetDimensions(new Point2D(this.x - xChange, this.y), new Point2D(28+ xChange, 27));
        tabRectangle.setBackgroundColour(this.stateTriggered ? activeColor : inactiveColor);
        tabRectangle.draw();
        Minecraft minecraft = Minecraft.getInstance();

        // render the item models
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        this.renderIcon(minecraft.getItemRenderer());
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(mouseButton)) {
                boolean flag = this.clicked(mouseX, mouseY);
                if (flag) {
                    Musique.playClientSound(SoundDictionary.SOUND_EVENT_GUI_SELECT, 1);
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    /**
     * Renders the item icons for the tabs. Some tabs have 2 icons, some just one.
     *
     * @param renderer
     */
    private void renderIcon(ItemRenderer renderer) {
        List<ItemStack> icons = this.category.getIcons();
        int offset = this.stateTriggered ? -4 : -2;
        if (icons.size() == 1) {
            renderer.renderItemAndEffectIntoGUI(icons.get(0), this.x + 9 + offset, this.y + 5);
        } else if (icons.size() == 2) {
            renderer.renderItemAndEffectIntoGUI(icons.get(0), this.x + 3 + offset, this.y + 5);
            renderer.renderItemAndEffectIntoGUI(icons.get(1), this.x + 14 + offset, this.y + 5);
        }
    }
}