package openllet.core.boxes.abox;

import java.util.Map;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.datatypes.DatatypeReasoner;

public interface ABoxForRule extends ABoxStatus, ABoxForBinding
{
	Map<ATermAppl, Node> getNodes();

	Individual addIndividual(final ATermAppl x, final DependencySet ds);

	Literal addLiteral(final ATermAppl dataValue);

	Literal addLiteral(final DependencySet ds);

	DatatypeReasoner getDatatypeReasoner();

	void copyOnWrite();
}
