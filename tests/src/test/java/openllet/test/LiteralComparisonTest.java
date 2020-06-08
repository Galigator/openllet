package openllet.test;

import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxImpl;
import openllet.core.boxes.abox.Literal;
import openllet.core.utils.TermFactory;
import org.junit.Assert;
import org.junit.Test;

public class LiteralComparisonTest
{

	@Test
	public void numericLiteralComparison()
	{
		final KnowledgeBase kb = new KnowledgeBaseImpl();
		final ABox abox = new ABoxImpl(kb);
		final Literal byteLiteral = abox.addLiteral(TermFactory.literal((byte) 0));
		final Literal shortLiteral = abox.addLiteral(TermFactory.literal((short) 200));
		Assert.assertTrue("numeric literals should be different", byteLiteral.isDifferent(shortLiteral));
	}

}
