package goblinbob.mobends.core.animation.keyframe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Describes which bones of an armature should be affected, and which should not.
 * 
 * @author Iwo Plaza
 *
 */
public class ArmatureMask
{
	private Mode mode;
	private List<String> includeList;
	private List<String> excludeList;
	
	public ArmatureMask(Mode mode)
	{
		this.mode = mode;
		this.includeList = new ArrayList<String>();
		this.excludeList = new ArrayList<String>();
	}
	
	public void include(String bone)
	{
		this.includeList.add(bone);
	}
	
	public void includeAll(Collection<String> bones)
	{
		this.includeList.addAll(bones);
	}
	
	public void exclude(String bone)
	{
		this.excludeList.add(bone);
	}
	
	public void excludeAll(Collection<String> bones)
	{
		this.excludeList.addAll(bones);
	}
	
	public boolean doesAllow(String bone)
	{
		switch (this.mode)
		{
			case INCLUDE_ONLY:
				return this.includeList.contains(bone);
			case EXCLUDE_ONLY:
				return !this.excludeList.contains(bone);
			default:
				return true;
		}
	}
	
	public static enum Mode
	{
		INCLUDE_ONLY, EXCLUDE_ONLY
	}
}
