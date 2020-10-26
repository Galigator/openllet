package openllet;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class OpenlletExceptionFormatter
{

	private boolean _verbose = false;

	public OpenlletExceptionFormatter()
	{
	}

	/**
	 * Format a user-friendly exception
	 *
	 * @param e
	 * @return user-friendly format... if exception can be friendly.
	 */
	public String formatException(final Throwable e)
	{
		Throwable cause = e;
		while (cause.getCause() != null)
			cause = cause.getCause();

		if (!_verbose)
		{
			if (cause instanceof FileNotFoundException)
				return format((FileNotFoundException) cause);
			if (cause instanceof OpenlletCmdException)
				return format((OpenlletCmdException) cause);
			return formatGeneric(cause);
		}

		final StringWriter writer = new StringWriter();
		try (PrintWriter pw = new PrintWriter(writer))
		{
			cause.printStackTrace(pw);
		}
		return writer.toString();

	}

	private static String format(final FileNotFoundException e)
	{
		return "ERROR: Cannot open " + e.getMessage();
	}

	private static String format(final OpenlletCmdException e)
	{
		return "ERROR: " + e.getMessage();
	}

	/**
	 * Return a generic exception message.
	 *
	 * @param e
	 */
	private static String formatGeneric(final Throwable e)
	{
		String msg = e.getMessage();
		if (msg != null)
		{
			final int index = msg.indexOf('\n', 0);
			if (index != -1)
				msg = msg.substring(0, index);
		}

		return msg + "\nUse -v for detail.";
	}

	public void setVerbose(final boolean verbose)
	{
		_verbose = verbose;
	}

}
