package openllet.core.boxes.abox;

public interface ABoxStatus
{
	/**
	 * @return true if Abox is closed.
	 */
	boolean isClosed();

	/**
	 * @return Returns the isComplete.
	 */
	boolean isComplete();

	/**
	 * @return the changed
	 */
	boolean isChanged();
}
