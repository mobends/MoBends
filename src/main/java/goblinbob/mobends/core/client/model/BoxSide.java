package goblinbob.mobends.core.client.model;

public enum BoxSide
{
	LEFT(0),
	RIGHT(1),
	TOP(2),
	BOTTOM(3),
	FRONT(4),
	BACK(5);
	
	int faceIndex;
	
	BoxSide(int index)
	{
		this.faceIndex = index;
	}
}
