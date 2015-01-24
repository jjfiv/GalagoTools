package edu.umass.ciir.galagotools.fns;

import edu.umass.ciir.galagotools.galago.GalagoUtil;
import org.lemurproject.galago.core.btree.simple.DiskMapBuilder;
import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.tools.AppFunction;

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
    return makeHelpStr(
        "field", "metadata field name to create diskmap from",
        "indexPath", "galago index to read from and put it inside"
    );
  }

  @Override
  public void run(Parameters argp, PrintStream out) throws Exception {
    String field = argp.getString("field");
    File indexPath = new File(argp.getString("indexPath"));
    if(!indexPath.isDirectory()) throw new IllegalArgumentException("Not a directory!");
    File output = new File(indexPath, "metadata."+field);

    DiskIndex index = new DiskIndex(indexPath.getAbsolutePath());
    DiskMapBuilder diskMapBuilder = new DiskMapBuilder(output.getAbsolutePath());

    for(Document doc : GalagoUtil.documentIterable(index, Document.DocumentComponents.JustMetadata)) {
      if(doc != null) {
        String value = doc.metadata.get(field);
        if(value != null) {
          diskMapBuilder.put(ByteUtil.fromString(doc.name), ByteUtil.fromString(value));
        }
      }

    }

    diskMapBuilder.close();
    DiskMapWrapper<String,String> data = new DiskMapWrapper<>(output.getAbsolutePath(), new DiskMapWrapper.StringCodec(), new DiskMapWrapper.StringCodec());

    out.println("Wrote "+data.size()+" metadata field="+field+" to "+output.getAbsolutePath());
    data.close();
  }
}
