package edu.umass.ciir.galagotools.tupleflow;

import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.tupleflow.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley.
 */
public abstract class KeyValueReducer<K,V> implements Reducer<KeyValuePair> {
  public final DiskMapWrapper.Codec<K> keyCodec;
  public final DiskMapWrapper.Codec<V> valCodec;
  public KeyValueReducer(DiskMapWrapper.Codec<K> keyCodec, DiskMapWrapper.Codec<V> valCodec) {
    this.keyCodec = keyCodec;
    this.valCodec = valCodec;
  }

  @Override
  public ArrayList<KeyValuePair> reduce(List<KeyValuePair> input) throws IOException {
    ArrayList<KeyValuePair> results = new ArrayList<KeyValuePair>();
    if(input.isEmpty())
      return results;

    K lastKey = keyCodec.fromBytes(input.get(0).key);
    ArrayList<V> values = new ArrayList<V>();

    for(KeyValuePair kv : input) {
      K curKey = keyCodec.fromBytes(kv.key);
      if(curKey.equals(lastKey)) {
        values.add(valCodec.fromBytes(kv.value));
      } else {
        results.add(reduce(lastKey, values));
        lastKey = curKey;
        values = new ArrayList<V>();
      }
    }
    results.add(reduce(lastKey, values));

    return results;
  }

  public abstract KeyValuePair reduce(K key, ArrayList<V> values);
}
