package javajs.api.js;


/**
 * methods in j2s JavaScript accessed in Jmol 
 */
public interface J2SObjectInterface {

  void repaint(JSAppletObject html5Applet, boolean asNewThread);

  void setCanvasImage(Object canvas, int width, int height);

  Object getHiddenCanvas(JSAppletObject html5Applet, String string, int w, int h);

  Object loadFileAsynchronously(Object fileLoadThread, JSAppletObject html5Applet, String fileName, Object appData);

  void resizeApplet(Object html5Applet, int[] dims);

  boolean isBinaryUrl(String filename);


  Object doAjax(Object url, String postOut, Object bytesOrStringOut, boolean isBinary);

  void applyFunc(Object func, Object data);

  Object newGrayScaleImage(Object context, Object image, int width,
                           int height, int[] grayBuffer);


  void showInfo(JSAppletObject html5Applet, boolean show);

  void clearConsole(JSAppletObject html5Applet);


}
