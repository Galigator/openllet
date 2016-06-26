package openllet.modularity.test;

import openllet.modularity.GraphBasedModuleExtractor;
import openllet.modularity.ModuleExtractor;

/**
 * @author Evren Sirin
 */
public class GraphBasedRandomizedIncrementalClassifierTest extends RandomizedIncrementalClassifierTest
{
	public GraphBasedRandomizedIncrementalClassifierTest()
	{
		super("test/data/modularity/");
	}

	@Override
	public ModuleExtractor createModuleExtractor()
	{
		return new GraphBasedModuleExtractor();
	}
}
