package net.gobbob.mobends.core.math.matrix;

public interface IMat3x3d extends IMatd
{
	
	default int getCols() { return 3; };
	default int getRows() { return 3; };
	void copyFrom(IMat3x3d src);
	
}
