import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundUtility {

    // Ganti "sounds/" sesuai lokasi folder audio Anda. 
    // Jika file ada di root project, hapus "sounds/".
    private static final String SOUND_PATH = "sound/";

    public static void playSound(String filename) {
        new Thread(() -> {
            try {
                File soundFile = new File(SOUND_PATH + filename);
                if (!soundFile.exists()) {
                    System.err.println("File audio tidak ditemukan: " + filename);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Gagal memutar audio: " + e.getMessage());
            }
        }).start();
    }
}