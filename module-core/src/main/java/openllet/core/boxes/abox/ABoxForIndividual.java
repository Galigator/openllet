package openllet.core.boxes.abox;

import java.util.List;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;

public interface ABoxForIndividual extends ABoxStatus
{
	List<ATermAppl> getNodeNames();

	Node getNode(ATerm aTerm);

	Individual getIndividual(ATerm aTerm);

	default ABoxForRule getABoxForRule()
	{
		return (ABoxForRule) this;
	}
}
