package edu.umass.ciir.galagotools.spelling;

import edu.umass.ciir.galagotools.utils.StrUtil;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.tokenize.Tokenizer;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.OutputClass;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.execution.Verified;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author jfoley
 */
@Verified
@InputClass(className = "org.lemurproject.galago.core.parse.Document")
@OutputClass(className = "org.lemurproject.galago.core.parse.Document")
public class CharacterTrigramTokenizer extends Tokenizer {
  public CharacterTrigramTokenizer(TupleFlowParameters parameters) {
    super(parameters);
  }

  public CharacterTrigramTokenizer() {
    super(new FakeParameters(Parameters.instance()));
  }

  @Override
  public void tokenize(Document input) {
    // kill spaces
    String text = StrUtil.compactSpaces(input.text.toLowerCase());
    input.terms = new ArrayList<>(text.length());
    for (int i = 0; i < text.length()-3; i++) {
      String trigram = text.substring(i, i + 3);
      if(trigram.contains(" ")) {
        continue;
      }
      input.terms.add(trigram);
    }
    input.tags = Collections.emptyList();
    input.termCharBegin = Collections.emptyList();
    input.termCharEnd = Collections.emptyList();
  }
}
