package goblinbob.mobends.core.client.gui;

public class GuiDragger<T extends IGuiDraggable>
{

    private T draggedElement;

    public GuiDragger()
    {
        this.draggedElement = null;
    }

    public void setDraggedElement(T draggedElement)
    {
        if (this.draggedElement != null)
        {
            this.draggedElement.setDragged(false);
        }

        this.draggedElement = draggedElement;
        this.draggedElement.setDragged(true);
    }

    public void stopDragging()
    {
        if (draggedElement != null)
        {
            draggedElement.setDragged(false);
        }
        draggedElement = null;
    }

    public T getDraggedElement()
    {
        return draggedElement;
    }

    public void update(int mouseX, int mouseY)
    {
        if (draggedElement != null)
        {
            draggedElement.dragTo(mouseX, mouseY);
        }
    }

}
