package goblinbob.mobends.core.kumo.state;

public interface ILayerState
{

    void start();
    void update(float deltaTime);
    boolean isAnimationFinished();

}
