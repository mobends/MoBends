package net.gobbob.mobends.core.pack.state;

public interface ILayerState
{

    void start();
    void update(float deltaTime);
    boolean isAnimationFinished();

}
