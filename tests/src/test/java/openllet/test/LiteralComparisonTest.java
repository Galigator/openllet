package openllet.test;

import static openllet.core.utils.TermFactory.literal;

import org.junit.Assert;
import org.junit.Test;

import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxImpl;
import openllet.core.boxes.abox.Literal;
import openllet.core.utils.TermFactory;

public class LiteralComparisonTest
{

	@Test
	public void numericLiteralComparison()
	{
		final KnowledgeBase kb = new KnowledgeBaseImpl();
		final ABox abox = new ABoxImpl(kb);
		final Literal byteLiteral = abox.addLiteral(literal((byte) 0));
		final Literal shortLiteral = abox.addLiteral(literal((short) 200));
		Assert.assertTrue("numeric literals should be different", byteLiteral.isDifferent(shortLiteral));
	}

	@Test
	public void numericLiteralComparison2()
	{
		final KnowledgeBase kb = new KnowledgeBaseImpl();
		final ABox abox = new ABoxImpl(kb);
		final Literal a = abox.addLiteral(literal((byte) 127));
		final Literal b = abox.addLiteral(literal((short) 127));
		Assert.assertTrue("numeric literals should be the same", a.isSame(b));
	}
	
	@Test
	public void numericLiteralComparison3()
	{
		final KnowledgeBase kb = new KnowledgeBaseImpl();
		final ABox abox = new ABoxImpl(kb);
		final Literal a = abox.addLiteral(literal((byte) 128)); // Overflow on -128
		final Literal b = abox.addLiteral(literal((short) 128));
		Assert.assertTrue("numeric literals should be different", a.isDifferent(b));
	}
}
