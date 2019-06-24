package net.gobbob.mobends.core.client.gui.elements;

public abstract class GuiPanel extends GuiElement
{

    private boolean shown;
    private int width;
    private int height;
    private Direction direction;

    public GuiPanel(GuiElement element, int x, int y, int width, int height, Direction direction)
    {
        super(element, x, y);
        this.shown = true;
        this.width = width;
        this.height = height;
        this.direction = direction;
    }

    @Override
    public void drawChildren()
    {
        float t = this.shown ? 0F : 1F;
//        float xOffset = 0F;
//        float yOffset = 0F;
//
//        if (this.direction == Direction.UP)
//            yOffset = t * height;
//        else if (this.direction == Direction.RIGHT)
//            xOffset = -t * width;
//        else if (this.direction == Direction.DOWN)
//            yOffset = -t * height;
//        else
//            xOffset = t * width;

        if (t != 1F)
            super.drawChildren();
    }

    public void setShown(boolean shown)
    {
        this.shown = shown;
    }

    public boolean isShown()
    {
        return shown;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public enum Direction
    {
        UP, RIGHT, DOWN, LEFT
    }

}
