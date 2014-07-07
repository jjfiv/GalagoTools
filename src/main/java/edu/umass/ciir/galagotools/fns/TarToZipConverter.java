package edu.umass.ciir.galagotools.fns;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.StreamUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author jfoley.
 */
public class TarToZipConverter extends AppFunction {

  @Override
  public String getName() {
    return "tar-to-zip";
  }

  @Override
  public String getHelpString() {
    return AppFnRunner.helpDescriptions(this,
        Parameters.parseArray(
            "input", "the tar file",
            "output", "the zip file to write"));
  }

  @Override
  public void run(Parameters p, PrintStream output) throws Exception {
    TarArchiveInputStream tais = new TarArchiveInputStream(new FileInputStream(p.getString("input")));
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(p.getString("output")));

    while(true) {
      TarArchiveEntry tarEntry = tais.getNextTarEntry();
      if(tarEntry == null) break;
      if(!tarEntry.isFile()) continue;
      if(!tais.canReadEntryData(tarEntry)) continue;

      ZipEntry forData = new ZipEntry(tarEntry.getFile().getPath());
      forData.setSize(tarEntry.getSize());
      zos.putNextEntry(forData);
      StreamUtil.copyStream(tais, zos);
      zos.closeEntry();
    }
    tais.close();
    zos.close();
  }
}