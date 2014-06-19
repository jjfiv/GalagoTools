package edu.umass.ciir.galagotools.err;

/**
 * @author jfoley
 */
public class NotHandledNow extends RuntimeException {
  public NotHandledNow(String where, String method) {
    this("'"+method+"' not handled now for '"+where+"'");
  }
  public NotHandledNow(String msg) {
    super(msg);
  }
}

