/*
 * Created on Mar 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package openllet.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.shared.tools.Log;

/**
 * @author ronwalf Automatic (from ant) version information for Pellet
 */
public class VersionInfo
{
	public final static Logger	_logger				= Log.getLogger(VersionInfo.class);
	private Properties			versionProperties	= null;

	private static String		UNKNOWN				= "(unknown)";

	public VersionInfo()
	{
		versionProperties = new Properties();

		try (final InputStream vstream = VersionInfo.class.getResourceAsStream("src/main/resources/openllet/version.properties.in"))
		{
			if (vstream != null) try
			{
				versionProperties.load(vstream);
			}
			catch (final IOException e)
			{
				_logger.log(Level.SEVERE, "Could not load version properties", e);
			}
			finally
			{
				try
				{
					vstream.close();
				}
				catch (final IOException e)
				{
					_logger.log(Level.SEVERE, "Could not close version properties", e);
				}
			}
		}
		catch (final IOException exception)
		{
			exception.printStackTrace();
		}
	}

	public static final VersionInfo getInstance()
	{
		return new VersionInfo();
	}

	public String getVersionString()
	{
		return versionProperties.getProperty("org.mindswap.pellet.version", "(unreleased)");
	}

	public String getReleaseDate()
	{
		return versionProperties.getProperty("org.mindswap.pellet.releaseDate", UNKNOWN);
	}

	@Override
	public String toString()
	{
		return "Version: " + getVersionString() + " Released: " + getReleaseDate();
	}
}
