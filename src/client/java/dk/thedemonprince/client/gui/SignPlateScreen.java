package dk.thedemonprince.client.gui;

import dk.thedemonprince.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SignPlateScreen extends Screen {
    private final Screen parent;
    private TemplateListWidget templateList;
    private ConfigManager.SignTemplate editingTemplate = null;
    private int editingIndex = -1;

    private TextFieldWidget nameField;
    private TextFieldWidget line1;
    private TextFieldWidget line2;
    private TextFieldWidget line3;
    private TextFieldWidget line4;

    public SignPlateScreen(Screen parent) {
        super(Text.literal("SignPlate"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        ConfigManager.ConfigData config = ConfigManager.getInstance().getConfig();
        Text enabledText = Text.literal("SignPlate: ").append(Text.literal(config.signPlateEnabled ? "ON" : "OFF").formatted(config.signPlateEnabled ? Formatting.GREEN : Formatting.RED));
        
        addDrawableChild(ButtonWidget.builder(enabledText, button -> {
            config.signPlateEnabled = !config.signPlateEnabled;
            ConfigManager.getInstance().save();
            refresh();
        }).dimensions(centerX - 100, 10, 200, 20).build());

        if (editingTemplate == null) {
            // List View - Dimensions: width, height, y, itemHeight
            // y=40 starts below the ON/OFF button
            // height: available space for the list widget itself
            int listY = 40;
            int listHeight = height - 120; // leaves space for buttons at the bottom
            
            templateList = new TemplateListWidget(this.client, width, listHeight, listY, 25);
            addDrawableChild(templateList);

            addDrawableChild(ButtonWidget.builder(Text.literal("Create New Template"), button -> {
                editingTemplate = new ConfigManager.SignTemplate("New Template", "", "", "", "");
                editingIndex = -1;
                refresh();
            }).dimensions(centerX - 100, height - 70, 200, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> {
                if (this.client != null) this.client.setScreen(parent);
            }).dimensions(centerX - 100, height - 45, 200, 20).build());
        } else {
            templateList = null;
            // Editor View Centered
            int startY = centerY - 90;
            nameField = new TextFieldWidget(textRenderer, centerX - 100, startY, 200, 20, Text.literal("Template Name"));
            nameField.setText(editingTemplate.name);
            addDrawableChild(nameField);

            int linesY = startY + 45;
            line1 = new TextFieldWidget(textRenderer, centerX - 100, linesY, 200, 20, Text.literal("Line 1"));
            line1.setText(editingTemplate.line1);
            addDrawableChild(line1);

            line2 = new TextFieldWidget(textRenderer, centerX - 100, linesY + 25, 200, 20, Text.literal("Line 2"));
            line2.setText(editingTemplate.line2);
            addDrawableChild(line2);

            line3 = new TextFieldWidget(textRenderer, centerX - 100, linesY + 50, 200, 20, Text.literal("Line 3"));
            line3.setText(editingTemplate.line3);
            addDrawableChild(line3);

            line4 = new TextFieldWidget(textRenderer, centerX - 100, linesY + 75, 200, 20, Text.literal("Line 4"));
            line4.setText(editingTemplate.line4);
            addDrawableChild(line4);

            addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
                editingTemplate.name = nameField.getText();
                editingTemplate.line1 = line1.getText();
                editingTemplate.line2 = line2.getText();
                editingTemplate.line3 = line3.getText();
                editingTemplate.line4 = line4.getText();
                
                if (editingIndex == -1) {
                    config.signTemplates.add(editingTemplate);
                    if (config.selectedSignTemplate == -1) config.selectedSignTemplate = config.signTemplates.size() - 1;
                } else {
                    config.signTemplates.set(editingIndex, editingTemplate);
                }
                
                ConfigManager.getInstance().save();
                editingTemplate = null;
                refresh();
            }).dimensions(centerX - 105, linesY + 105, 100, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
                editingTemplate = null;
                refresh();
            }).dimensions(centerX + 5, linesY + 105, 100, 20).build());
        }
    }

    private void refresh() {
        this.clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (editingTemplate != null) {
            int centerX = width / 2;
            int centerY = height / 2;
            int startY = centerY - 90;
            int linesY = startY + 45;
            
            context.drawTextWithShadow(textRenderer, Text.literal("Template Name:"), centerX - 100, startY - 12, 0xAAAAAA);
            context.drawTextWithShadow(textRenderer, Text.literal("Lines (1-4):"), centerX - 100, linesY - 12, 0xAAAAAA);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
    }

    class TemplateListWidget extends ElementListWidget<TemplateEntry> {
        public TemplateListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
            ConfigManager.ConfigData config = ConfigManager.getInstance().getConfig();
            for (int i = 0; i < config.signTemplates.size(); i++) {
                addEntry(new TemplateEntry(i, config.signTemplates.get(i)));
            }
        }

        @Override
        public int getRowWidth() { return 300; }
        
        @Override
        protected int getScrollbarX() { return width / 2 + 155; }
    }

    class TemplateEntry extends ElementListWidget.Entry<TemplateEntry> {
        private final int index;
        private final ConfigManager.SignTemplate template;
        private final List<ClickableWidget> buttons = new ArrayList<>();

        public TemplateEntry(int index, ConfigManager.SignTemplate template) {
            this.index = index;
            this.template = template;
            
            buttons.add(ButtonWidget.builder(Text.literal("Select"), button -> {
                ConfigManager.getInstance().getConfig().selectedSignTemplate = index;
                ConfigManager.getInstance().save();
                refresh();
            }).dimensions(0, 0, 50, 20).build());

            buttons.add(ButtonWidget.builder(Text.literal("Edit"), button -> {
                editingTemplate = template;
                editingIndex = index;
                refresh();
            }).dimensions(0, 0, 40, 20).build());

            buttons.add(ButtonWidget.builder(Text.literal("X").formatted(Formatting.RED), button -> {
                ConfigManager.ConfigData config = ConfigManager.getInstance().getConfig();
                config.signTemplates.remove(index);
                if (config.selectedSignTemplate >= config.signTemplates.size()) {
                    config.selectedSignTemplate = config.signTemplates.size() - 1;
                }
                ConfigManager.getInstance().save();
                refresh();
            }).dimensions(0, 0, 20, 20).build());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            ConfigManager.ConfigData config = ConfigManager.getInstance().getConfig();
            boolean isSelected = config.selectedSignTemplate == this.index;
            
            context.drawTextWithShadow(textRenderer, (isSelected ? "● " : "○ ") + template.name, x + 5, y + 5, isSelected ? 0x55FF55 : 0xAAAAAA);

            int btnX = x + entryWidth - 120;
            for (int i = 0; i < buttons.size(); i++) {
                ClickableWidget btn = buttons.get(i);
                btn.setX(btnX + (i == 0 ? 0 : (i == 1 ? 55 : 100)));
                btn.setY(y);
                if (i == 0) btn.active = !isSelected;
                btn.render(context, mouseX, mouseY, tickDelta);
            }
        }

        @Override
        public List<? extends net.minecraft.client.gui.Element> children() {
            return this.buttons;
        }

        @Override
        public List<? extends net.minecraft.client.gui.Selectable> selectableChildren() {
            return this.buttons;
        }
    }
}
