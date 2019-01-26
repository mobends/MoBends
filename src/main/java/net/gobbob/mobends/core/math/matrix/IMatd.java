package net.gobbob.mobends.core.math.matrix;

public interface IMatd
{

	double[] getFields();
	double get(int c, int r);
	int getCols();
	int getRows();
	
	void set(int c, int r, double value);
	void setFields(double... values);
	void scale(double scalar);
	
}
