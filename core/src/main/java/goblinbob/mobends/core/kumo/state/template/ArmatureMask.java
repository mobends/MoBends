package goblinbob.mobends.core.kumo.state.template;

import java.util.List;

/**
 * The bones a layer may write: {@code {"mode": "INCLUDE_ONLY", "includedParts": ["mouth"]}} or
 * {@code {"mode": "EXCLUDE_ONLY", "excludedParts": [...]}}.
 */
public class ArmatureMask
{

	public Mode mode;
	public List<String> includedParts;
	public List<String> excludedParts;

	public void validate() throws MalformedKumoTemplateException
	{
		if (mode == null)
		{
			throw new MalformedKumoTemplateException("A layer's mask needs a \"mode\": INCLUDE_ONLY or EXCLUDE_ONLY.");
		}
		if ((mode == Mode.INCLUDE_ONLY ? includedParts : excludedParts) == null)
		{
			throw new MalformedKumoTemplateException(String.format("A layer's %s mask needs its \"%s\".", mode, mode == Mode.INCLUDE_ONLY ? "includedParts" : "excludedParts"));
		}
	}

	public boolean doesAllow(String bone)
	{
		return mode == Mode.INCLUDE_ONLY ? includedParts.contains(bone) : !excludedParts.contains(bone);
	}

	public enum Mode
	{
		INCLUDE_ONLY, EXCLUDE_ONLY
	}

}
