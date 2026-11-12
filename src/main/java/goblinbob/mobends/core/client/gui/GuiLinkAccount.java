package goblinbob.mobends.core.client.gui;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.gui.elements.GuiCompactTextField;
import goblinbob.mobends.core.connection.MinecraftLink;
import goblinbob.mobends.core.connection.MoBendsAccount;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.util.ApiErrorException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/**
 * Links the player's Minecraft account to their Mo' Bends account (see {@link MinecraftLink}): the player enters
 * the code from the dashboard, confirms the Mo' Bends account it belongs to, and the game proves it owns the
 * Minecraft account. The confirmation keeps an attacker from getting a player to link their account to the
 * attacker's code.
 * <p>
 * It is opened from the Mo' Bends menu, which is only reachable in a world, so the game is never connecting to a
 * server meanwhile: a join on Mojang's session server then could fail that connection.
 */
public class GuiLinkAccount extends GuiScreen
{

    private static final int COMPONENT_BUTTON_BACK = 0;
    private static final int COMPONENT_BUTTON_ACTION = 1;
    private static final int COMPONENT_BUTTON_DASHBOARD = 2;
    private static final int CONTENT_WIDTH = 260;

    private enum State
    {
        /** The player types the code in. */
        ENTER_CODE,
        /** Waiting on {@link MinecraftLink#start}. */
        STARTING,
        /** The player confirms the account to link to. */
        CONFIRM,
        /** Waiting on {@link MinecraftLink#joinAndConfirm}. */
        LINKING,
        LINKED,
        FAILED
    }

    private final GuiScreen parent;
    private final GuiCompactTextField codeField;
    private GuiButton backButton;
    private GuiButton actionButton;
    private GuiButton dashboardButton;

    private State state = State.ENTER_CODE;
    private String code = "";
    private MinecraftLink.Start start;
    private String message = "";
    /** Whether the code no longer works (it expired, or a newer one replaced it), so the player needs a new one. */
    private boolean codeSpent;

    public GuiLinkAccount(GuiScreen parent)
    {
        this.parent = parent;
        this.codeField = new GuiCompactTextField(Minecraft.getMinecraft().fontRendererObj, 120, 20);
        this.codeField.setMaxStringLength(32);
        this.codeField.setPlaceholderText("ABCD-EFGH");
        this.codeField.setFocused(true);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        int centerX = width / 2;

        codeField.setPosition(centerX - codeField.width / 2, height / 2 - 10);

        buttonList.clear();
        // What the step is about, under the code or the message; leaving and the dashboard at the bottom left.
        actionButton = new GuiButton(COMPONENT_BUTTON_ACTION, centerX - 50, height / 2 + 18, 100, 20, "");
        backButton = new GuiButton(COMPONENT_BUTTON_BACK, 10, height - 30, 60, 20, "");
        final String dashboard = I18n.format("mobends.gui.link.dashboard");
        // The dashboard, where the code comes from.
        dashboardButton = new GuiButton(COMPONENT_BUTTON_DASHBOARD, 75, height - 30,
                fontRendererObj.getStringWidth(dashboard) + 20, 20, dashboard);
        buttonList.add(actionButton);
        buttonList.add(backButton);
        buttonList.add(dashboardButton);
        updateButtons();
    }

    private void setState(State state, String message)
    {
        this.state = state;
        this.message = message;
        updateButtons();
    }

    private void updateButtons()
    {
        if (backButton == null)
        {
            return;
        }

        backButton.displayString = I18n.format(state == State.CONFIRM ? "gui.cancel" : "mobends.gui.back");
        actionButton.visible = state != State.STARTING && state != State.LINKING;

        switch (state)
        {
            case CONFIRM:
                actionButton.displayString = I18n.format("mobends.gui.link.confirm");
                break;
            case LINKED:
                actionButton.displayString = I18n.format("gui.done");
                break;
            case FAILED:
                actionButton.displayString = I18n.format(codeSpent ? "mobends.gui.link.new_code" : "mobends.gui.link.retry");
                break;
            default:
                actionButton.displayString = I18n.format("mobends.gui.link.continue");
        }
        actionButton.enabled = state != State.ENTER_CODE || hasCode();
    }

    private boolean hasCode()
    {
        return codeField.getText().matches(".*[A-Za-z0-9].*");
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == COMPONENT_BUTTON_DASHBOARD)
        {
            WebLink.ACCOUNT.open();
            return;
        }

        if (button.id == COMPONENT_BUTTON_BACK)
        {
            if (state == State.CONFIRM)
            {
                setState(State.ENTER_CODE, "");
            }
            else
            {
                mc.displayGuiScreen(parent);
            }
            return;
        }

        switch (state)
        {
            case ENTER_CODE:
                startLinking();
                break;
            case CONFIRM:
                confirmLinking();
                break;
            case LINKED:
                mc.displayGuiScreen(parent);
                break;
            case FAILED:
                // Back to the code, so the account is confirmed again with a fresh server id.
                if (codeSpent)
                {
                    codeField.setText("");
                }
                setState(State.ENTER_CODE, "");
                break;
            default:
                break;
        }
    }

    private void startLinking()
    {
        // Only once per code entered: a second /start would replace the server id the first one is confirmed with.
        if (state != State.ENTER_CODE || !hasCode())
        {
            return;
        }

        code = codeField.getText();
        setState(State.STARTING, I18n.format("mobends.gui.link.starting"));

        runInBackground(() -> {
            try
            {
                MinecraftLink.Start start = MinecraftLink.start(code);
                return () -> {
                    this.start = start;
                    setState(State.CONFIRM, I18n.format("mobends.gui.link.confirm_prompt",
                            mc.getSession().getUsername(), start.getDisplayName()));
                };
            }
            catch (IOException e)
            {
                return fail(e, false);
            }
        });
    }

    private void confirmLinking()
    {
        // Only once: of several confirmations of the same code, only one can succeed.
        if (state != State.CONFIRM)
        {
            return;
        }

        setState(State.LINKING, I18n.format("mobends.gui.link.linking"));
        MinecraftLink.Start start = this.start;

        runInBackground(() -> {
            try
            {
                MinecraftLink.Result result = MinecraftLink.joinAndConfirm(code, start);
                String displayName = result.getDisplayName() != null ? result.getDisplayName() : start.getDisplayName();
                // Signed in even if the player has left this screen meanwhile: the code is spent either way.
                mc.addScheduledTask(() -> {
                    MoBendsAccount.signIn(displayName, result.getToken());
                    // Fetches the player's accessories again, now that they follow the account.
                    SupporterContent.forgetPlayer(mc.getSession().getUsername());
                });
                return () -> setState(State.LINKED,
                        I18n.format("mobends.gui.link.linked", result.getUsername(), displayName));
            }
            catch (AuthenticationException | IOException e)
            {
                return fail(e, true);
            }
        });
    }

    /**
     * @param confirming Whether it failed confirming the join, rather than starting.
     */
    private Runnable fail(Exception e, boolean confirming)
    {
        Core.LOG.warning("Couldn't link the Minecraft account: " + e);
        boolean spent = e instanceof ApiErrorException && "unknown_code".equals(((ApiErrorException) e).getError());
        String message = spent && confirming
                // The join worked, but the code was replaced or expired before the API checked it.
                ? I18n.format("mobends.gui.link.error.unknown_code_confirming")
                : describe(e);
        return () -> {
            codeSpent = spent;
            setState(State.FAILED, message);
        };
    }

    private static String describe(Exception e)
    {
        if (e instanceof AuthenticationUnavailableException)
        {
            return I18n.format("mobends.gui.link.error.mojang_unavailable");
        }
        if (e instanceof AuthenticationException)
        {
            return I18n.format("mobends.gui.link.error.session");
        }
        if (e instanceof ApiErrorException)
        {
            String error = ((ApiErrorException) e).getError();
            if (error != null)
            {
                switch (error)
                {
                    case "unknown_code":
                    case "not_started":
                    case "invalid_request":
                    case "join_not_verified":
                    case "session_server_error":
                        return I18n.format("mobends.gui.link.error." + error);
                    case "internal_error":
                        return I18n.format("mobends.gui.link.error.api");
                    default:
                        break;
                }
            }
            return I18n.format("mobends.gui.link.error.api");
        }
        return I18n.format("mobends.gui.link.error.connection");
    }

    /**
     * Runs the task on a thread of its own, then what it returns on the main thread, if this screen is still open.
     */
    private void runInBackground(Task task)
    {
        Thread thread = new Thread(() -> {
            Runnable then;
            try
            {
                then = task.run();
            }
            catch (RuntimeException e)
            {
                then = fail(e, false);
            }
            Runnable result = then;
            mc.addScheduledTask(() -> {
                if (mc.currentScreen == this)
                {
                    result.run();
                }
            });
        }, "Mo' Bends account link");
        thread.setDaemon(true);
        thread.start();
    }

    @FunctionalInterface
    private interface Task
    {
        Runnable run();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (state == State.ENTER_CODE)
        {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)
            {
                startLinking();
                return;
            }
            if (codeField.textboxKeyTyped(typedChar, keyCode))
            {
                updateButtons();
                return;
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (state == State.ENTER_CODE)
        {
            codeField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void updateScreen()
    {
        codeField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        int centerX = width / 2;
        drawCenteredString(fontRendererObj, I18n.format("mobends.gui.link.title"), centerX, height / 2 - 70, 0xFFFFFF);

        if (state == State.ENTER_CODE)
        {
            drawLines(I18n.format("mobends.gui.link.enter_code"), height / 2 - 45, 0xAAAAAA);
            codeField.drawTextBox();
        }
        else
        {
            int color = state == State.FAILED ? 0xFF5555 : state == State.LINKED ? 0x55FF55 : 0xFFFFFF;
            int y = drawLines(message, height / 2 - 40, color);
            if (state == State.CONFIRM)
            {
                drawLines(I18n.format("mobends.gui.link.confirm_warning"), y + 8, 0xAAAAAA);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /** Draws the text centered, wrapped to the content width, and returns the y below it. */
    private int drawLines(String text, int y, int color)
    {
        List<String> lines = fontRendererObj.listFormattedStringToWidth(text, CONTENT_WIDTH);
        for (String line : lines)
        {
            drawCenteredString(fontRendererObj, line, width / 2, y, color);
            y += fontRendererObj.FONT_HEIGHT + 1;
        }
        return y;
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

}
