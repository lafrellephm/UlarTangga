import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundUtility {

    public static void playSound(String filename) {
        new Thread(() -> {
            try {
                // Menggunakan Resource Loading (Classpath)
                // Path dimulai dengan "/" yang berarti root dari folder src (atau folder output classes)
                String path = "/sound/" + filename;

                // Mengambil URL file dari classpath
                URL soundURL = SoundUtility.class.getResource(path);

                if (soundURL == null) {
                    System.err.println("Sound Error: File tidak ditemukan di path: " + path);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

            } catch (UnsupportedAudioFileException e) {
                System.err.println("Sound Error: Format audio tidak didukung (" + filename + ")");
            } catch (IOException e) {
                System.err.println("Sound Error: Gagal membaca file (" + filename + ")");
            } catch (LineUnavailableException e) {
                System.err.println("Sound Error: Audio line tidak tersedia.");
            } catch (Exception e) {
                System.err.println("Sound Error: " + e.getMessage());
            }
        }).start();
    }
}