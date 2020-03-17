package goblinbob.mobends.core.client.gui;

public interface IGuiDraggable
{

    /**
     * Moves the draggable to the specified position relative
     * to the screen origin.
     * @param x
     * @param y
     */
    void dragTo(int x, int y);

    void setDragged(boolean dragged);

    boolean isDragged();

}
