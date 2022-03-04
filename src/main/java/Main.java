import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

public class Main {

    private static final boolean debug = true;

    private static final String tmpPathPrefix = "/tmp";
    private static final String path;

    private static final String filename = "teorver.pdf";

    private static final String convName = "conv_"+filename+".png";
    private static final String decodeName = "decoded_"+filename;

    static {
        path = System.getProperty("user.dir").replace('\\','/')+tmpPathPrefix;
    }

    private static final File fileIn = new File(path+'/'+filename);
    private static final File fileOut = new File(path+'/'+decodeName);
    private static final File imageOut = new File(path+'/'+convName);

    public static void main(String[] args) throws IOException {
        run();
    }

    public static void run() throws IOException {
        byte[] bytes = Files.readAllBytes(fileIn.toPath());
        BufferedImage img = bytesToImage(bytes);

        ImageIO.write(img,"png", imageOut);

        BufferedImage imgRead = ImageIO.read(imageOut);
        byte[] bytesRead = imageToBytes(imgRead);
        Files.write(fileOut.toPath(),bytesRead);
    }

    public static BufferedImage bytesToImage(byte[] bytes) {
        int width; //width=height
        int totalBytes;
        int bytesInPixel = 3; //rgb
        int pixels;

        totalBytes = bytes.length;
        pixels = (int)Math.ceil((double)totalBytes/bytesInPixel);
        width = (int)Math.ceil(Math.sqrt(pixels))+1;

        BufferedImage img = new BufferedImage(width,width,BufferedImage.TYPE_INT_RGB);

        int n = 0;
        int i = 0;
        int j = 0;
        int k = 0;
        int lastByte = -1;
        byte fillEmptyByte = 0;
        Color fillEmpty = new Color(0,0,0);

        byte[] rgb = new byte[3];
        while (n<totalBytes){
            lastByte = bytes[n] & 0xff;
            rgb[k] = bytes[n];
            if (k==2){
                Color color = new Color(rgb[0] & 0xff,rgb[1] & 0xff,rgb[2] & 0xff);
                img.setRGB(i,j,color.getRGB());

                k = 0;
                j++;
                if (j>=width){
                    j = 0;
                    i++;
                }
            } else {
                k++;
            }
            n++;
        }

        if (lastByte==0) {
            fillEmpty = new Color(1,1,1);
            fillEmptyByte = 1;
        }

        log("Empty byte: "+fillEmptyByte);

        long empty = 0L;

        if (k==1){
            rgb[1]=fillEmptyByte;
            rgb[2]=fillEmptyByte;
            empty+=2;
        } else if (k==2){
            rgb[2]=fillEmptyByte;
            empty++;
        }

        if (k!=0) {
            Color color = new Color(rgb[0] & 0xff,rgb[1] & 0xff,rgb[2] & 0xff);
            img.setRGB(i, j, color.getRGB());
        }

        j++;
        if (j>=width){
            j = 0;
            i++;
        }


        while(i<width){
            while(j<width){
                img.setRGB(i,j,fillEmpty.getRGB());
                empty+=3;
                j++;
            }
            j=0;
            i++;
        }
        log("Real empty bytes count: "+empty);
        log("Real informative bytes count: "+(bytes.length-empty));
        log("Calculated informative bytes count: "+(bytes.length- getBytesToSkip(img)));
        return img;
    }

    private static int getBytesToSkip(BufferedImage bufferedImage){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        byte fillEmpty = (byte) new Color(bufferedImage.getRGB(width-1,height-1)).getBlue();
        int skip = 0;
        for (int i = width-1; i>=0; i--){
            for (int j = height-1; j>=0;j--){
                Color c = new Color(bufferedImage.getRGB(i,j));
                if (c.getBlue()!=fillEmpty) return skip-3;
                skip++;
                if (c.getGreen()!=fillEmpty) return skip-3;
                skip++;
                if (c.getRed()!=fillEmpty) return skip-3;
                skip++;
            }
        }

        return skip-3;
    }

    public static byte[] imageToBytes(BufferedImage bufferedImage){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        byte fillEmpty = (byte) new Color(bufferedImage.getRGB(width-1,height-1)).getBlue();
        log(fillEmpty);
        int skip = getBytesToSkip(bufferedImage);
        log("Bytes to skip: "+skip);
        int total = width*height*3-skip;
        log("Informative bytes in total: "+total);
        byte[] bytes = new byte[total];
        int n = 0;
        for (int i = 0; i<width; i++){
            for (int j = 0; j<height;j++){
                Color c = new Color(bufferedImage.getRGB(i,j));

                bytes[n] = (byte) c.getRed();
                n++;
                if (n>=total) break;

                bytes[n] = (byte) c.getGreen();
                n++;
                if (n>=total) break;

                bytes[n] = (byte) c.getBlue();
                n++;
                if (n>=total) break;
            }
            if (n>=total) break;
        }

        return bytes;
    }

    private static void log(Object o){
        if (!debug) return;
        System.out.println(o);
    }

}
