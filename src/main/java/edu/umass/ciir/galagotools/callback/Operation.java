package edu.umass.ciir.galagotools.callback;

import java.io.IOException;

/**
* @author jfoley.
*/
public interface Operation<T> {
  public void process(T obj) throws IOException;
}
