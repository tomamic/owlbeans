package it.unipr.aotlab.owl.io;

import it.unipr.aotlab.owl.core.OwlOntology;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Writes an intermediate ontology to a given output.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public interface OwlWriter {

	public void init(Properties conf) throws IOException;
	public void init(Properties conf, OutputStream output) throws IOException;
	public void write(OwlOntology ontology) throws IOException;

}
