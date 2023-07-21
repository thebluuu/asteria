package net.caffeinemc.phosphor.gui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.caffeinemc.phosphor.common.Phosphor;
import net.caffeinemc.phosphor.module.Module;

public class CategoryTab implements Renderable {
    public Module.Category category;
    private boolean firstFrame;
    private float posX, posY;

    public CategoryTab(Module.Category category, float posX, float posY) {
        this.category = category;
        this.posX = posX;
        this.posY = posY;
        this.firstFrame = true;
    }

    @Override
    public String getName() {
        return category.name;
    }

    @Override
    public void render() {
        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        ImGui.getStyle().setFramePadding(4, 6);
        ImGui.getStyle().setButtonTextAlign(0, 0.5f);
        ImGui.begin(getName(), imGuiWindowFlags);


        if (firstFrame) {
            ImGui.setWindowPos(posX, posY);
            firstFrame = false;
        }

        for (Module module : Phosphor.moduleManager().getModulesByCategory(category)) {
            ImGui.pushID(module.getName());

            if (module.isEnabled()) {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
                ImGui.pushStyleColor(ImGuiCol.Button, 0.65f, 0.24f, 0.33f, 0.50f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.65f, 0.24f, 0.33f, 0.65f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.65f, 0.24f, 0.33f, 0.8f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
                ImGui.pushStyleColor(ImGuiCol.Button, 0.07f, 0.07f, 0.11f, 0.f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.09f, 0.09f, 0.15f, 0.65f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.1f, 0.1f, 0.16f, 0.8f);
            }

            boolean isToggled = ImGui.button(module.getName(), 180f, 30f);

            ImGui.popStyleColor(4);

            if (isToggled) {
                module.toggle();
            }

            if (ImGui.isItemHovered()) {
                ImGui.setTooltip(module.getDescription());

                if (ImGui.isMouseClicked(1)) {
                    module.toggleShowOptions();
                }
            }

            if (module.showOptions()) {
                ImGui.indent(10f);
                ImGui.pushFont(ImguiLoader.getDosisFont());
                ImGui.getStyle().setFramePadding(4, 4);
                ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
                module.renderSettings();
                ImGui.getStyle().setButtonTextAlign(0f, 0f);
                ImGui.getStyle().setFramePadding(4, 6);
                ImGui.popFont();
                ImGui.unindent(10f);
            }

            ImGui.popID();
        }

        ImGui.end();
    }

    @Override
    public Theme getTheme() {
        return RadiumMenu.getInstance().getTheme();
    }
}