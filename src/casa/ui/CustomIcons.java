package casa.ui;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

public class CustomIcons {
  private static BufferedImage onePixel;

  static {
    onePixel = new BufferedImage (1, 1, BufferedImage.TYPE_4BYTE_ABGR);
    onePixel.setRGB (0, 0, 0);
  }

  public static ImageIcon getCustomIcon (String name) {
    ImageIcon icon;

    URL imageURL = CustomIcons.class.getResource (name);
    if (imageURL != null) {
      icon = new ImageIcon (imageURL);
    } else {
      imageURL = CustomIcons.class.getResource ("images/customGraphics/"+name);
      if (imageURL != null) {
        icon = new ImageIcon (imageURL);
      } else {
        icon = new ImageIcon (onePixel);
      }
    }
    
    return icon;
  }
  
  public static ImageIcon getCustomIcon (String name, String description) {
    ImageIcon icon = getCustomIcon (name);

    icon.setDescription (description);
    
    return icon;
  }
  
//  public static ImageIcon FRAME_ICON = getCustomIcon ("/images/customGraphics/frameIcon.png", "CASA Frame Icon");
//  public static ImageIcon CASA_SMALL = getCustomIcon ("/images/customGraphics/casa_small.jpg", "CASA Icon");
//  public static ImageIcon REAL_INNER_ICON = getCustomIcon ("/images/customGraphics/real_innericon.png", "CASA Inner Icon");
  public static ImageIcon FRAME_ICON = getCustomIcon ("/images/customGraphics/casa.icns", "CASA Frame Icon");
  public static ImageIcon CASA_SMALL = getCustomIcon ("/images/customGraphics/casa.icns", "CASA Icon");
  public static ImageIcon REAL_INNER_ICON = getCustomIcon ("/images/customGraphics/casa.icns", "CASA Inner Icon");
}
