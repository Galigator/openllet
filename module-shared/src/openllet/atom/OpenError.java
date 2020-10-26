package openllet.atom;

/**
 * exceptions are thrown when agent related errors occur.
 */
public class OpenError extends RuntimeException
{
	private static final long serialVersionUID = 3127335636382782538L;

	/**
	 * Instantiates a new exception.
	 */
	public OpenError()
	{
		super();
	}

	/**
	 * Instantiates a new exception using the given message.
	 *
	 * @param message Details about the exception.
	 */
	public OpenError(final String message)
	{
		super(message);
	}

	/**
	 * Instantiates a new exception using the given cause.
	 *
	 * @param cause Cause of the exception.
	 */
	public OpenError(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Instantiates a new exception using the given message and cause.
	 *
	 * @param message Details about the exception.
	 * @param cause Cause of the exception.
	 */
	public OpenError(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
