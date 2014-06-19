package edu.umass.ciir.galagotools.fns;

import org.lemurproject.galago.core.btree.simple.DiskMapBuilder;
import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.DocumentReader;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.io.File;
import java.io.PrintStream;

/**
 * @author jfoley.
 */
public class AddMetadataPart extends AppFunction {
  @Override
  public String getName() {
    return "add-metadata-part";
  }

  @Override
  public String getHelpString() {
    return "add-metadata-part\n\n"+
      "\t--field=fieldName\n" +
      "\t--indexPath=INDEX\n";
  }

  @Override
  public void run(Parameters argp, PrintStream out) throws Exception {
    String field = argp.getString("field");
    File indexPath = new File(argp.getString("indexPath"));
    if(!indexPath.isDirectory()) throw new IllegalArgumentException("Not a directory!");
    File output = new File(indexPath, "metadata."+field);

    DiskIndex index = new DiskIndex(indexPath.getAbsolutePath());
    CorpusReader reader = (CorpusReader) index.getIndexPart("corpus");
    DocumentReader.DocumentIterator iter = reader.getIterator();

    DiskMapBuilder diskMapBuilder = new DiskMapBuilder(output.getAbsolutePath());

    while(!iter.isDone()) {
      Document doc = iter.getDocument(Document.DocumentComponents.JustMetadata);

      if(doc != null) {
        String value = doc.metadata.get(field);
        if(value != null) {
          diskMapBuilder.put(Utility.fromString(doc.name), Utility.fromString(value));
        }
      }

      iter.nextKey();
    }

    diskMapBuilder.close();
    DiskMapWrapper<String,String> data = new DiskMapWrapper<String,String>(output.getAbsolutePath(), new DiskMapWrapper.StringCodec(), new DiskMapWrapper.StringCodec());

    out.println("Wrote "+data.size()+" metadata field="+field+" to "+output.getAbsolutePath());
    data.close();
  }
}
