package mymusicplayer;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import java.io.InputStream;



public class MP3AudioSource implements AudioProcessor {
    private final Bitstream bitstream;
    private final Decoder decoder;
    private final int bufferSize;
    private final float[] buffer;

    public MP3AudioSource(InputStream inputStream, int bufferSize) {
        this.bitstream = new Bitstream(inputStream);
        this.decoder = new Decoder();
        this.bufferSize = bufferSize;
        this.buffer = new float[bufferSize];
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        try {
            // Read the next frame from the Bitstream
            Header header = bitstream.readFrame();
            if (header == null) {
                return false; // End of the stream
            }

            // Decode the frame using the Decoder
            SampleBuffer sampleBuffer = (SampleBuffer) decoder.decodeFrame(header, bitstream);

            // Copy decoded samples to the buffer
            int samplesRead = Math.min(sampleBuffer.getBufferLength(), bufferSize);
            for (int i = 0; i < samplesRead; i++) {
                buffer[i] = sampleBuffer.getBuffer()[i] / 32768.0f; // Normalize to [-1, 1]
            }

            // Close the current frame to prepare for the next one
            bitstream.closeFrame();

            // Set the buffer in the AudioEvent
            audioEvent.setFloatBuffer(buffer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void processingFinished() {
        try {
            bitstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
