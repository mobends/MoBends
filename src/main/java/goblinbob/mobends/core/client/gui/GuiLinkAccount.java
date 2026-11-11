package goblinbob.mobends.core.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import goblinbob.mobends.core.connection.MinecraftLink;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.1 {
/*import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Links the player's Minecraft account to their Mo' Bends account (see {@link MinecraftLink}): the
 * player enters the code from the dashboard, confirms the Mo' Bends account it belongs to, and the
 * game proves it owns the Minecraft account. The confirmation keeps an attacker from getting a
 * player to link their account to the attacker's code.
 * <p>
 * It is opened from the Mo' Bends menu, which is only reachable in a world or from the mod list, so
 * the game is never connecting to a server meanwhile: a join on Mojang's session server then could
 * fail that connection.
 */
public class GuiLinkAccount extends Screen {
    private static final int CONTENT_WIDTH = 260;
    private static final int CODE_FIELD_WIDTH = 120;
    private static final int CODE_FIELD_HEIGHT = 20;

    /** Where linking is. */
    private enum State {
        /** The player types the code in. */
        ENTER_CODE,
        /** Waiting on {@link MinecraftLink#start}. */
        STARTING,
        /** The player confirms the account to link to. */
        CONFIRM,
        /** Waiting on {@link MinecraftLink#confirm}. */
        LINKING,
        LINKED,
        FAILED
    }

    private final Screen parent;
    private EditBox codeField;
    private Button backButton;
    private Button actionButton;

    private State state = State.ENTER_CODE;
    /** What's typed in the code field, kept while the screen is laid out anew. */
    private String code = "";
    private MinecraftLink.Start start;
    private Component message = CommonComponents.EMPTY;
    /** Whether the code no longer works (it expired, or a newer one replaced it), so the player needs a new one. */
    private boolean codeSpent;

    public GuiLinkAccount(Screen parent) {
        super(Component.translatable("mobends.gui.link.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        codeField = new EditBox(this.font, centerX - CODE_FIELD_WIDTH / 2, height / 2 - 10, CODE_FIELD_WIDTH, CODE_FIELD_HEIGHT,
                Component.translatable("mobends.gui.link.title"));
        codeField.setMaxLength(32);
        codeField.setHint(Component.literal("ABCD-EFGH"));
        codeField.setValue(code);
        codeField.setResponder(text -> {
            code = text;
            updateButtons();
        });
        addRenderableWidget(codeField);

        // What the step is about, under the code or the message; leaving and the dashboard at the bottom left.
        actionButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button -> onAction())
                .bounds(centerX - 50, height / 2 + 18, 100, 20).build());
        backButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button -> onBack())
                .bounds(10, height - 30, 60, 20).build());
        // The dashboard, where the code comes from.
        Component dashboard = Component.translatable("mobends.gui.link.dashboard");
        addRenderableWidget(Button.builder(dashboard, button -> WebLink.ACCOUNT.open())
                .bounds(75, height - 30, this.font.width(dashboard) + 20, 20).build());
        updateButtons();
        //? if <1.20.2 {
        /*setInitialFocus();
        *///?}
    }

    //? if >=1.20.2 {
    @Override
    protected void setInitialFocus() {
        if (state == State.ENTER_CODE) {
            setInitialFocus(codeField);
        } else {
            super.setInitialFocus();
        }
    }
    //?} else {
    /*/^* 1.20.1's screens focus nothing by themselves; called at the end of init. ^/
    private void setInitialFocus() {
        if (state == State.ENTER_CODE) {
            setInitialFocus(codeField);
        }
    }
    *///?}

    /** Moves linking on to {@code state}, showing {@code message} (but for the code's step). */
    private void setState(State state, Component message) {
        this.state = state;
        this.message = message;
        updateButtons();
        if (state == State.ENTER_CODE && codeField != null) {
            setFocused(codeField);
        }
    }

    private void updateButtons() {
        if (backButton == null) {
            return;
        }

        boolean enteringCode = state == State.ENTER_CODE;
        codeField.visible = enteringCode;
        codeField.active = enteringCode;

        backButton.setMessage(state == State.CONFIRM ? CommonComponents.GUI_CANCEL : Component.translatable("mobends.gui.back"));
        actionButton.visible = state != State.STARTING && state != State.LINKING;
        actionButton.setMessage(switch (state) {
            case CONFIRM -> Component.translatable("mobends.gui.link.confirm");
            case LINKED -> CommonComponents.GUI_DONE;
            case FAILED -> Component.translatable(codeSpent ? "mobends.gui.link.new_code" : "mobends.gui.link.retry");
            default -> Component.translatable("mobends.gui.link.continue");
        });
        actionButton.active = !enteringCode || hasCode();
    }

    private boolean hasCode() {
        return code.matches(".*[A-Za-z0-9].*");
    }

    private void onBack() {
        if (state == State.CONFIRM) {
            setState(State.ENTER_CODE, CommonComponents.EMPTY);
        } else {
            onClose();
        }
    }

    private void onAction() {
        switch (state) {
            case ENTER_CODE -> startLinking();
            case CONFIRM -> confirmLinking();
            case LINKED -> onClose();
            case FAILED -> {
                // Back to the code, so the account is confirmed again with a fresh server id.
                if (codeSpent) {
                    codeField.setValue("");
                }
                setState(State.ENTER_CODE, CommonComponents.EMPTY);
            }
            default -> {
            }
        }
    }

    private void startLinking() {
        // Only once per code entered: a second start would replace the server id the first one is confirmed with.
        if (state != State.ENTER_CODE || !hasCode()) {
            return;
        }

        setState(State.STARTING, Component.translatable("mobends.gui.link.starting"));
        MinecraftLink.start(code, start -> {
            if (isOpen()) {
                this.start = start;
                setState(State.CONFIRM, Component.translatable("mobends.gui.link.confirm_prompt",
                        this.minecraft.getUser().getName(), start.displayName()));
            }
        }, this::fail);
    }

    private void confirmLinking() {
        // Only once: of several confirmations of the same code, only one can succeed.
        if (state != State.CONFIRM) {
            return;
        }

        setState(State.LINKING, Component.translatable("mobends.gui.link.linking"));
        // Signed in even if the player has left this screen meanwhile: the code is spent either way.
        MinecraftLink.confirm(start, result -> {
            if (isOpen()) {
                setState(State.LINKED, Component.translatable("mobends.gui.link.linked", result.username(), result.displayName()));
            }
        }, this::fail);
    }

    private void fail(MinecraftLink.Failure failure) {
        if (isOpen()) {
            codeSpent = failure.codeSpent();
            setState(State.FAILED, Component.translatable(failure.translationKey()));
        }
    }

    /** Whether this screen is still the one shown: what linking answers is only shown on it. */
    private boolean isOpen() {
        return Screens.current() == this;
    }

    @Override
    //? if >=26.1 {
    /*public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean clicked = super.mouseClicked(event, doubleClick);
    *///?} else {
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = super.mouseClicked(mouseX, mouseY, button);
    //?}
        // The code is typed in whatever was clicked, as 1.12's field kept the focus: a button
        // (Try again) would otherwise take it.
        if (state == State.ENTER_CODE && getFocused() != codeField) {
            setFocused(codeField);
        }
        return clicked;
    }

    //? if >=26.1 {
    /*@Override
    public boolean keyPressed(KeyEvent event) {
        return enterCode(event.key()) || super.keyPressed(event);
    }
    *///?} else {
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return enterCode(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?}

    /** Enter starts linking, while the code is typed in. */
    private boolean enterCode(int keyCode) {
        if (state == State.ENTER_CODE && (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER)) {
            startLinking();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        Screens.open(parent);
    }

    @Override
    //? if >=26.1 {
    /*public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    *///?} else {
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //? if <1.20.2 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTicks);
    //?}

        int centerX = width / 2;
        graphics.drawCenteredString(this.font, this.title, centerX, height / 2 - 70, 0xFFFFFFFF);

        if (state == State.ENTER_CODE) {
            drawLines(graphics, Component.translatable("mobends.gui.link.enter_code"), height / 2 - 45, 0xFFAAAAAA);
        } else {
            int color = state == State.FAILED ? 0xFFFF5555 : state == State.LINKED ? 0xFF55FF55 : 0xFFFFFFFF;
            int y = drawLines(graphics, message, height / 2 - 40, color);
            if (state == State.CONFIRM) {
                drawLines(graphics, Component.translatable("mobends.gui.link.confirm_warning"), y + 8, 0xFFAAAAAA);
            }
        }
    }

    /** Draws the text centered, wrapped to the content width, and returns the y below it. */
    private int drawLines(GuiGraphics graphics, Component text, int y, int color) {
        for (FormattedCharSequence line : this.font.split(text, CONTENT_WIDTH)) {
            graphics.drawCenteredString(this.font, line, width / 2, y, color);
            y += this.font.lineHeight + 1;
        }
        return y;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
