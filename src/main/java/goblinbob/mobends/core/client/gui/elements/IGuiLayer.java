package goblinbob.mobends.core.client.gui.elements;

public interface IGuiLayer
{
	void cleanUp();
	void draw();
	void update(int mouseX, int mouseY);
	default void handleResize(int width, int height) {}
	default boolean handleKeyTyped(char typedChar, int keyCode) { return false; }
	default boolean handleMouseInput() { return false; }
	default boolean handleMouseClicked(int mouseX, int mouseY, int button) { return false; }
	default boolean handleMouseReleased(int mouseX, int mouseY, int button) { return false; }
}
