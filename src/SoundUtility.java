import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundUtility {

    /**
     * Memutar file suara (.wav) dari folder resources/sound.
     * Dijalankan di thread baru agar tidak mengganggu UI.
     */
    public static void playSound(String filename) {
        new Thread(() -> {
            try {
                // Path relatif terhadap root source (src)
                String path = "/sound/" + filename;
                URL soundURL = SoundUtility.class.getResource(path);

                if (soundURL == null) {
                    System.err.println("Sound Error: File tidak ditemukan di path: " + path);
                    return;
                }

                // Setup Audio Stream
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