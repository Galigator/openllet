package openllet.query.sparqldl.jena;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.function.Consumer;

/**
 * @author Cristiano Longo
 */
public abstract class ResultSetImpl implements ResultSet {

    @Override
    public void forEachRemaining(Consumer<? super QuerySolution> consumer) {
        while(hasNext())
            consumer.accept(next());
    }

    @Override
    public void close() {
        // do nothing, override it if there are underlying resources that need to be closed
    }

}
