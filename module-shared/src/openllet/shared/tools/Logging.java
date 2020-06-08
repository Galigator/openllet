package openllet.shared.tools;

import java.util.logging.Logger;

public interface Logging
{
	Logger getLogger();

	default org.slf4j.Logger logger()
	{
		return Log.toSlf4j(getLogger());
	}
}
