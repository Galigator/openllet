package openllet.core.boxes.abox;

import openllet.aterm.ATerm;
import openllet.core.KnowledgeBase;

public interface ABoxForBinding
{
	Individual getIndividual(ATerm aTerm);

	Literal getLiteral(ATerm target);

	KnowledgeBase getKB();
}
