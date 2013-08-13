
import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.ImageLoader;
import net.sourceforge.jiu.codecs.PNGCodec;
import net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering;
import net.sourceforge.jiu.color.dithering.OrderedDither;
import net.sourceforge.jiu.color.promotion.PromotionRGB24;
import net.sourceforge.jiu.color.quantization.OctreeColorQuantizer;
import net.sourceforge.jiu.color.reduction.ReduceRGB;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.geometry.Resample;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.prefs.Preferences;

/**
 * Takes a scanned text document and prepares it for example for your tax office.
 * <p/>
 * https://github.com/alexkasko/openjdk-unofficial-builds#openjdk-unofficial-installers-for-windows-linux-and-mac-os-x
 *
 * @author Rene Schmidt <rene@reneschmidt.de>
 */
public class ScannedDocumentProcessor {
  /**
   * progress monitor
   */
  public ScadopProgressMonitor pm;

  /**
   * Overwrite files
   */
  Boolean overwriteAll = false;

  /**
   * File chooser
   */
  JFileChooser fc;

  /**
   * Set prefs
   */
  public void setPrefs() {
    getPrefs().put("lastDir", fc.getCurrentDirectory().toString());
  }

  /**
   * Get last dir
   */
  public File getPrefLastDir() {
    return new File(getPrefs().get("lastDir", "./"));
  }

  /**
   * Get prefs instance
   *
   * @return Prefs instance
   */
  protected Preferences getPrefs() {
    return Preferences.userRoot().node(this.getClass().getName());
  }

  /**
   * Shut down the whole thing...
   *
   * @param returnCode Return code
   */
  public void shutdown(int returnCode) {
    setPrefs();
    pm.close();
    System.exit(returnCode);
  }

  /**
   * Make 24 bit image
   *
   * @param image Image
   * @return 24 bit image
   */
  protected PixelImage promoteImage(PixelImage image) {
    PromotionRGB24 promoteImage = new PromotionRGB24();
    promoteImage.setInputImage(image);

    try {
      promoteImage.process();
    } catch (OperationFailedException ofe) {
      ofe.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return promoteImage.getOutputImage();
  }

  /**
   * Dither image
   *
   * @param image Image
   * @return Dithered image
   */
  protected PixelImage simpleDithering(PixelImage image) {
    OrderedDither dither = new OrderedDither();
    dither.setRgbBits(2, 2, 2);
    dither.setInputImage(image);

    try {
      dither.process();
    } catch (OperationFailedException ofe) {
      ofe.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return dither.getOutputImage();
  }

  /**
   * Dither image. Good for low-quality scans..
   *
   * @param image Image
   * @return Dithered image
   */
  protected PixelImage betterDithering(PixelImage image) {
    if (image.getBitsPerPixel() < 24) { // make 24 bit image out of color-indexed or grey scale PNGs
      image = promoteImage(image);
    }

    OctreeColorQuantizer quantizer = new OctreeColorQuantizer();
    quantizer.setInputImage(image);
    quantizer.setPaletteSize(8);

    try {
      quantizer.init();
      ErrorDiffusionDithering edd = new ErrorDiffusionDithering();
      edd.setTemplateType(ErrorDiffusionDithering.TYPE_JARVIS_JUDICE_NINKE);
/*
      TYPE_BURKES
      static int	TYPE_FLOYD_STEINBERG
      static int	TYPE_JARVIS_JUDICE_NINKE
      static int	TYPE_SIERRA
      static int	TYPE_STEVENSON_ARCE
      static int	TYPE_STUCKI
          */
      edd.setQuantizer(quantizer);
      edd.setInputImage(image);
      edd.process();
      return edd.getOutputImage();
    } catch (OperationFailedException ofe) {
      ofe.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return image;
  }

  /**
   * Reduce colors of image. Good for high quality scans.
   *
   * @param image Image
   * @return Color-reduced image
   */
  protected PixelImage reduceColors(PixelImage image) {
    if (image.getBitsPerPixel() < 24) { // make 24 bit image out of color-indexed or grey scale PNGs
      image = promoteImage(image);
    }

    ReduceRGB reduce = new ReduceRGB();
    reduce.setBitsPerSample(3);
    reduce.setInputImage(image);

    try {
      reduce.process();
    } catch (OperationFailedException ofe) {
      ofe.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return reduce.getOutputImage();
  }

  /**
   * Scale image
   *
   * @param image Image
   * @return Scaled image
   */
  protected PixelImage scaleDown(PixelImage image) {
    if (image.getBitsPerPixel() < 24) { // make 24 bit image out of color-indexed or grey scale PNGs
      image = promoteImage(image);
    }

    int newWidth = 800;

    Resample resample = new Resample();

    try {
      resample.setInputImage(image);
      resample.setSize(newWidth, image.getHeight() * newWidth / image.getWidth());
      resample.setFilter(Resample.FILTER_TYPE_LANCZOS3);
      resample.process();
    } catch (WrongParameterException wpe) {
      return image;
    } catch (OperationFailedException ofe) {
      ofe.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return resample.getOutputImage();
  }

  /**
   * get new file name
   *
   * @param file   File instance
   * @param suffix Suffix string
   * @return New absolute file path
   */
  public String getNewAbsolutePath(File file, String suffix) {
    String newFilename = file.getName();

    if (newFilename.endsWith(".PNG") || newFilename.endsWith(".png")) {
      newFilename = newFilename.replaceFirst("(?i)\\.png$", "");
    }

    newFilename += "_" + suffix + ".png";

    return file.getParentFile() + java.io.File.separator + newFilename;
  }

  /**
   * Process image
   *
   * @param file Image
   */
  protected void processFile(File file) {
    try {
      PixelImage image = ImageLoader.load(file.getAbsolutePath());

      if (image == null) {
        throw new Exception("Could not load image: " + file.getName());
      }

      image = scaleDown(image);
      //image = betterDithering(image);
      image = reduceColors(image);

      PNGCodec codec = new PNGCodec();
      codec.setImage(image);
      codec.setFile(overwriteAll ? file.getAbsolutePath() : getNewAbsolutePath(file, "processed"), CodecMode.SAVE);
      codec.process();
      codec.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  /**
   * Constructor
   */
  public ScannedDocumentProcessor() {
    try {
      File[] files = showOpenFileDialog();

      pm = new ScadopProgressMonitor(new JFrame(), "Please wait...", 0, files.length);
      pm.setMillisToDecideToPopup(0);
      pm.setMillisToPopup(0);
      pm.setNote("Processing files...");

      for (File file : files) {
        if (pm.isCanceled()) {
          break;
        }
        processFile(file);
        pm.setScadopProgress(++pm.progress);
      }

      pm.setNote("Done!");

      JOptionPane.showMessageDialog(null, (pm.progress + 1) + " files processed.");
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      shutdown(0);
    } finally {
      shutdown(0);
    }
  }

  /**
   * Main function, duh
   *
   * @param args main arguments
   */
  public static void main(String[] args) {
    new ScannedDocumentProcessor();
  }

  /**
   * Open save file dialog
   *
   * @return File[]
   */
  public File[] showOpenFileDialog() {
    FileNameExtensionFilter filenameExtFilter = new FileNameExtensionFilter("PNG Files", "png");
    fc = new JFileChooser(getPrefLastDir());
    fc.addChoosableFileFilter(filenameExtFilter);
    fc.setFileFilter(filenameExtFilter);
    fc.setMultiSelectionEnabled(true);

    if (fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
      overwriteAll = JOptionPane.showConfirmDialog(
          null,
          "Overwrite files?",
          "ScannedDocumentProcessor",
          JOptionPane.YES_NO_OPTION
      ) != JOptionPane.NO_OPTION;

      return fc.getSelectedFiles();
    }

    return new File[0];
  }
}
