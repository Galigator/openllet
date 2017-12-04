package openllet.core.knowledge;

import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.rbox.RBox;
import openllet.core.boxes.tbox.TBox;
import openllet.core.datatypes.DatatypeReasoner;
import openllet.core.utils.Timers;

/**
 * @since 2.6.4
 */
public interface Boxes
{
	public ABox getABox();

	public TBox getTBox();

	public RBox getRBox();

	public Timers getTimers();

	/**
	 * @return Returns the DatatypeReasoner
	 */
	public default DatatypeReasoner getDatatypeReasoner()
	{
		return getABox().getDatatypeReasoner();
	}
}
