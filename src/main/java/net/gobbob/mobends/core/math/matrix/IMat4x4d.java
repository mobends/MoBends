package net.gobbob.mobends.core.math.matrix;

public interface IMat4x4d extends IMatd
{

	default int getCols() { return 4; };
	default int getRows() { return 4; };
	void copyFrom(IMat4x4d src);
	
}
