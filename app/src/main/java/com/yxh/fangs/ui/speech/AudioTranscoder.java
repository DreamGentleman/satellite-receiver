package com.yxh.fangs.ui.speech;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class AudioTranscoder {

    static final int TARGET_SAMPLE_RATE = 16000;

    private static final int DECODE_TIMEOUT_US = 10000;

    private final Context context;

    AudioTranscoder(Context context) {
        this.context = context.getApplicationContext();
    }

    File transcodeTo16kMonoPcm(Uri uri) {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec decoder = null;
        PcmWriter pcmWriter = null;
        try {
            extractor.setDataSource(context, uri, null);
            int audioTrack = selectAudioTrack(extractor);
            if (audioTrack < 0) {
                return null;
            }

            extractor.selectTrack(audioTrack);
            MediaFormat inputFormat = extractor.getTrackFormat(audioTrack);
            String mime = inputFormat.getString(MediaFormat.KEY_MIME);
            if (mime == null) {
                return null;
            }

            File outputFile = new File(context.getCacheDir(), "iat_import_16k.pcm");
            pcmWriter = new PcmWriter(outputFile);
            PcmResampler resampler = new PcmResampler(
                    inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                    inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
                    pcmWriter
            );

            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(inputFormat, null, null, 0);
            decoder.start();

            decodeToPcm(extractor, decoder, resampler);
            pcmWriter.close();
            return pcmWriter.getByteCount() > 0 ? outputFile : null;
        } catch (IOException e) {
            return null;
        } finally {
            extractor.release();
            if (decoder != null) {
                try {
                    decoder.stop();
                } catch (Throwable ignored) {
                }
                try {
                    decoder.release();
                } catch (Throwable ignored) {
                }
            }
            if (pcmWriter != null) {
                try {
                    pcmWriter.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private int selectAudioTrack(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    private void decodeToPcm(MediaExtractor extractor, MediaCodec decoder, PcmResampler resampler)
            throws IOException {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        boolean inputDone = false;
        boolean outputDone = false;

        while (!outputDone) {
            if (!inputDone) {
                int inputIndex = decoder.dequeueInputBuffer(DECODE_TIMEOUT_US);
                if (inputIndex >= 0) {
                    ByteBuffer inputBuffer = decoder.getInputBuffer(inputIndex);
                    if (inputBuffer != null) {
                        int sampleSize = extractor.readSampleData(inputBuffer, 0);
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            );
                            inputDone = true;
                        } else {
                            decoder.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    sampleSize,
                                    extractor.getSampleTime(),
                                    0
                            );
                            extractor.advance();
                        }
                    }
                }
            }

            int outputIndex = decoder.dequeueOutputBuffer(bufferInfo, DECODE_TIMEOUT_US);
            if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat outputFormat = decoder.getOutputFormat();
                resampler.updateSourceFormat(
                        outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                        outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                );
            } else if (outputIndex >= 0) {
                ByteBuffer outputBuffer = decoder.getOutputBuffer(outputIndex);
                if (outputBuffer != null && bufferInfo.size > 0) {
                    outputBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    resampler.write(outputBuffer);
                }
                decoder.releaseOutputBuffer(outputIndex, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputDone = true;
                }
            }
        }
    }

    private static final class PcmResampler {
        private int sourceSampleRate;
        private int sourceChannelCount;
        private int accumulator;
        private final PcmWriter pcmWriter;

        private PcmResampler(int sourceSampleRate, int sourceChannelCount, PcmWriter pcmWriter) {
            this.sourceSampleRate = sourceSampleRate;
            this.sourceChannelCount = Math.max(1, sourceChannelCount);
            this.pcmWriter = pcmWriter;
        }

        private void updateSourceFormat(int sourceSampleRate, int sourceChannelCount) {
            this.sourceSampleRate = sourceSampleRate;
            this.sourceChannelCount = Math.max(1, sourceChannelCount);
            this.accumulator = 0;
        }

        private void write(ByteBuffer buffer) throws IOException {
            int frameSize = sourceChannelCount * 2;
            while (buffer.remaining() >= frameSize) {
                int mixed = 0;
                for (int i = 0; i < sourceChannelCount; i++) {
                    mixed += buffer.getShort();
                }
                short monoSample = (short) (mixed / sourceChannelCount);
                accumulator += TARGET_SAMPLE_RATE;
                while (accumulator >= sourceSampleRate) {
                    pcmWriter.writeSample(monoSample);
                    accumulator -= sourceSampleRate;
                }
            }
        }
    }

    private static final class PcmWriter {
        private final FileOutputStream outputStream;
        private int byteCount;
        private boolean closed;

        private PcmWriter(File file) throws IOException {
            outputStream = new FileOutputStream(file);
        }

        private void writeSample(short sample) throws IOException {
            outputStream.write(sample & 0xFF);
            outputStream.write((sample >> 8) & 0xFF);
            byteCount += 2;
        }

        private int getByteCount() {
            return byteCount;
        }

        private void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            outputStream.flush();
            outputStream.close();
        }
    }
}
