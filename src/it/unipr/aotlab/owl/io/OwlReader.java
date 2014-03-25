package it.unipr.aotlab.owl.io;

import it.unipr.aotlab.owl.core.OwlOntology;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Builds an intermediate ontology by reading a source.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public interface OwlReader {

	public void init(Properties conf) throws IOException;
	public void init(Properties conf, InputStream output) throws IOException;
	public OwlOntology read() throws IOException;

}
