/**
 *
 */
package openllet.jena.test;

import java.io.IOException;
import java.io.InputStream;
import openllet.test.query.MiscSPARQLDLTest;
import org.apache.jena.ontology.OntDocumentManager.ReadFailureHandler;
import org.apache.jena.rdf.model.Model;

/**
 * @author Pavel Klinov
 */
public class ResourceImportLoader implements ReadFailureHandler
{

	/* (non-Javadoc)
	 * @see org.apache.jena.ontology.OntDocumentManager.ReadFailureHandler#handleFailedRead(java.lang.String, org.apache.jena.rdf.model.Model, java.lang.Exception)
	 */
	@Override
	public void handleFailedRead(final String url, final Model m, final Exception e)
	{
		if (url.startsWith("resource:"))
		{
			final String resourceName = "/" + url.substring(9);

			try (InputStream resourceStream = MiscSPARQLDLTest.class.getResourceAsStream(resourceName))
			{
				m.read(resourceStream, null);
			}
			catch (final IOException exception)
			{
				exception.printStackTrace();
			}
		}
	}
}
