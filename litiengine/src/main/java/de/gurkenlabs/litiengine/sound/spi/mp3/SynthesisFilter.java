package de.gurkenlabs.litiengine.sound.spi.mp3;

/// A class for the synthesis filter bank.
/// This class performs the polyphase synthesis filter required for MP3 decoding.
/// It converts 32 subband samples into 32 PCM samples.
final class SynthesisFilter {
    private final float[] v1;
    private final float[] v2;
    private float[] actualV; // v1 or v2
    private int actualWritePos; // 0-15
    private final float[] samples; // 32 new subband samples
    private final int channel;
    private final float scaleFactor;
    private int pcmBufferIndex = 0;

    // DCT coefficients loaded from resource
    private static float[] d = null;
    private static float[][] d16 = null;

    public SynthesisFilter(int channelNumber, float factor) {
        if (d == null) {
            d = loadD();
            d16 = splitArray(d, 16);
        }

        v1 = new float[512];
        v2 = new float[512];
        samples = new float[32];
        channel = channelNumber;
        scaleFactor = factor;

        reset();
    }

    public void reset() {
        for (int p = 0; p < 512; p++) {
            v1[p] = v2[p] = 0.0f;
        }
        for (int p2 = 0; p2 < 32; p2++) {
            samples[p2] = 0.0f;
        }

        actualV = v1;
        actualWritePos = 15;
    }

    public void inputSample(float sample, int subbandnumber) {
        samples[subbandnumber] = sample;
    }

    public void inputSamples(float[] s) {
        System.arraycopy(s, 0, samples, 0, 32);
    }

    /// Compute new values via a fast cosine transform.
    private void computeNewV() {
        float newV0, newV1, newV2, newV3, newV4, newV5, newV6, newV7, newV8, newV9;
        float newV10, newV11, newV12, newV13, newV14, newV15, newV16, newV17, newV18, newV19;
        float newV20, newV21, newV22, newV23, newV24, newV25, newV26, newV27, newV28, newV29;
        float newV30, newV31;

        newV0 = newV1 = newV2 = newV3 = newV4 = newV5 = newV6 = newV7 = newV8 = newV9 =
        newV10 = newV11 = newV12 = newV13 = newV14 = newV15 = newV16 = newV17 = newV18 = newV19 =
        newV20 = newV21 = newV22 = newV23 = newV24 = newV25 = newV26 = newV27 = newV28 = newV29 =
        newV30 = newV31 = 0.0f;

        float[] s = samples;

        // Stage 1: Sum pairs
        float p0 = s[0] + s[31];
        float p1 = s[1] + s[30];
        float p2 = s[2] + s[29];
        float p3 = s[3] + s[28];
        float p4 = s[4] + s[27];
        float p5 = s[5] + s[26];
        float p6 = s[6] + s[25];
        float p7 = s[7] + s[24];
        float p8 = s[8] + s[23];
        float p9 = s[9] + s[22];
        float p10 = s[10] + s[21];
        float p11 = s[11] + s[20];
        float p12 = s[12] + s[19];
        float p13 = s[13] + s[18];
        float p14 = s[14] + s[17];
        float p15 = s[15] + s[16];

        // Stage 2: DCT-IV on even indices
        float pp0 = p0 + p15;
        float pp1 = p1 + p14;
        float pp2 = p2 + p13;
        float pp3 = p3 + p12;
        float pp4 = p4 + p11;
        float pp5 = p5 + p10;
        float pp6 = p6 + p9;
        float pp7 = p7 + p8;
        float pp8 = (p0 - p15) * cos1_32;
        float pp9 = (p1 - p14) * cos3_32;
        float pp10 = (p2 - p13) * cos5_32;
        float pp11 = (p3 - p12) * cos7_32;
        float pp12 = (p4 - p11) * cos9_32;
        float pp13 = (p5 - p10) * cos11_32;
        float pp14 = (p6 - p9) * cos13_32;
        float pp15 = (p7 - p8) * cos15_32;

        // Stage 3: More DCT-IV
        p0 = pp0 + pp7;
        p1 = pp1 + pp6;
        p2 = pp2 + pp5;
        p3 = pp3 + pp4;
        p4 = (pp0 - pp7) * cos1_16;
        p5 = (pp1 - pp6) * cos3_16;
        p6 = (pp2 - pp5) * cos5_16;
        p7 = (pp3 - pp4) * cos7_16;
        p8 = pp8 + pp15;
        p9 = pp9 + pp14;
        p10 = pp10 + pp13;
        p11 = pp11 + pp12;
        p12 = (pp8 - pp15) * cos1_16;
        p13 = (pp9 - pp14) * cos3_16;
        p14 = (pp10 - pp13) * cos5_16;
        p15 = (pp11 - pp12) * cos7_16;

        // Stage 4: DCT-II
        pp0 = p0 + p3;
        pp1 = p1 + p2;
        pp2 = (p0 - p3) * cos1_8;
        pp3 = (p1 - p2) * cos3_8;
        pp4 = p4 + p7;
        pp5 = p5 + p6;
        pp6 = (p4 - p7) * cos1_8;
        pp7 = (p5 - p6) * cos3_8;
        pp8 = p8 + p11;
        pp9 = p9 + p10;
        pp10 = (p8 - p11) * cos1_8;
        pp11 = (p9 - p10) * cos3_8;
        pp12 = p12 + p15;
        pp13 = p13 + p14;
        pp14 = (p12 - p15) * cos1_8;
        pp15 = (p13 - p14) * cos3_8;

        // Stage 5: Final DCT-II
        p0 = pp0 + pp1;
        p1 = (pp0 - pp1) * cos1_4;
        p2 = pp2 + pp3;
        p3 = (pp2 - pp3) * cos1_4;
        p4 = pp4 + pp5;
        p5 = (pp4 - pp5) * cos1_4;
        p6 = pp6 + pp7;
        p7 = (pp6 - pp7) * cos1_4;
        p8 = pp8 + pp9;
        p9 = (pp8 - pp9) * cos1_4;
        p10 = pp10 + pp11;
        p11 = (pp10 - pp11) * cos1_4;
        p12 = pp12 + pp13;
        p13 = (pp12 - pp13) * cos1_4;
        p14 = pp14 + pp15;
        p15 = (pp14 - pp15) * cos1_4;

        // Calculate new V values (first 32)
        float tmp1;
        newV19 = -(newV4 = (newV12 = p7) + p5) - p6;
        newV27 = -p6 - p7 - p4;
        newV6 = (newV10 = (newV14 = p15) + p11) + p13;
        newV17 = -(newV2 = p15 + p13 + p9) - p14;
        newV21 = (tmp1 = -p14 - p15 - p10 - p11) - p13;
        newV29 = -p14 - p15 - p12 - p8;
        newV25 = tmp1 - p12;
        newV31 = -p0;
        newV0 = p1;
        newV23 = -(newV8 = p3) - p2;

        // Calculate differences for odd indices
        p0 = (s[0] - s[31]) * cos1_64;
        p1 = (s[1] - s[30]) * cos3_64;
        p2 = (s[2] - s[29]) * cos5_64;
        p3 = (s[3] - s[28]) * cos7_64;
        p4 = (s[4] - s[27]) * cos9_64;
        p5 = (s[5] - s[26]) * cos11_64;
        p6 = (s[6] - s[25]) * cos13_64;
        p7 = (s[7] - s[24]) * cos15_64;
        p8 = (s[8] - s[23]) * cos17_64;
        p9 = (s[9] - s[22]) * cos19_64;
        p10 = (s[10] - s[21]) * cos21_64;
        p11 = (s[11] - s[20]) * cos23_64;
        p12 = (s[12] - s[19]) * cos25_64;
        p13 = (s[13] - s[18]) * cos27_64;
        p14 = (s[14] - s[17]) * cos29_64;
        p15 = (s[15] - s[16]) * cos31_64;

        // DCT-IV on odd indices
        pp0 = p0 + p15;
        pp1 = p1 + p14;
        pp2 = p2 + p13;
        pp3 = p3 + p12;
        pp4 = p4 + p11;
        pp5 = p5 + p10;
        pp6 = p6 + p9;
        pp7 = p7 + p8;
        pp8 = (p0 - p15) * cos1_32;
        pp9 = (p1 - p14) * cos3_32;
        pp10 = (p2 - p13) * cos5_32;
        pp11 = (p3 - p12) * cos7_32;
        pp12 = (p4 - p11) * cos9_32;
        pp13 = (p5 - p10) * cos11_32;
        pp14 = (p6 - p9) * cos13_32;
        pp15 = (p7 - p8) * cos15_32;

        p0 = pp0 + pp7;
        p1 = pp1 + pp6;
        p2 = pp2 + pp5;
        p3 = pp3 + pp4;
        p4 = (pp0 - pp7) * cos1_16;
        p5 = (pp1 - pp6) * cos3_16;
        p6 = (pp2 - pp5) * cos5_16;
        p7 = (pp3 - pp4) * cos7_16;
        p8 = pp8 + pp15;
        p9 = pp9 + pp14;
        p10 = pp10 + pp13;
        p11 = pp11 + pp12;
        p12 = (pp8 - pp15) * cos1_16;
        p13 = (pp9 - pp14) * cos3_16;
        p14 = (pp10 - pp13) * cos5_16;
        p15 = (pp11 - pp12) * cos7_16;

        pp0 = p0 + p3;
        pp1 = p1 + p2;
        pp2 = (p0 - p3) * cos1_8;
        pp3 = (p1 - p2) * cos3_8;
        pp4 = p4 + p7;
        pp5 = p5 + p6;
        pp6 = (p4 - p7) * cos1_8;
        pp7 = (p5 - p6) * cos3_8;
        pp8 = p8 + p11;
        pp9 = p9 + p10;
        pp10 = (p8 - p11) * cos1_8;
        pp11 = (p9 - p10) * cos3_8;
        pp12 = p12 + p15;
        pp13 = p13 + p14;
        pp14 = (p12 - p15) * cos1_8;
        pp15 = (p13 - p14) * cos3_8;

        p0 = pp0 + pp1;
        p1 = (pp0 - pp1) * cos1_4;
        p2 = pp2 + pp3;
        p3 = (pp2 - pp3) * cos1_4;
        p4 = pp4 + pp5;
        p5 = (pp4 - pp5) * cos1_4;
        p6 = pp6 + pp7;
        p7 = (pp6 - pp7) * cos1_4;
        p8 = pp8 + pp9;
        p9 = (pp8 - pp9) * cos1_4;
        p10 = pp10 + pp11;
        p11 = (pp10 - pp11) * cos1_4;
        p12 = pp12 + pp13;
        p13 = (pp12 - pp13) * cos1_4;
        p14 = pp14 + pp15;
        p15 = (pp14 - pp15) * cos1_4;

        // Calculate remaining new V values (32-48)
        float tmp2;
        newV5 = (newV11 = (newV13 = (newV15 = p15) + p7) + p11) + p5 + p13;
        newV7 = (newV9 = p15 + p11 + p3) + p13;
        newV16 = -(newV1 = (tmp1 = p13 + p15 + p9) + p1) - p14;
        newV18 = -(newV3 = tmp1 + p5 + p7) - p6 - p14;
        newV22 = (tmp1 = -p10 - p11 - p14 - p15) - p13 - p2 - p3;
        newV20 = tmp1 - p13 - p5 - p6 - p7;
        newV24 = tmp1 - p12 - p2 - p3;
        newV26 = tmp1 - p12 - (tmp2 = p4 + p6 + p7);
        newV30 = (tmp1 = -p8 - p12 - p14 - p15) - p0;
        newV28 = tmp1 - tmp2;

        // Insert V[0-15] into actual v
        float[] dest = actualV;
        int pos = actualWritePos;

        dest[0 + pos] = newV0;
        dest[16 + pos] = newV1;
        dest[32 + pos] = newV2;
        dest[48 + pos] = newV3;
        dest[64 + pos] = newV4;
        dest[80 + pos] = newV5;
        dest[96 + pos] = newV6;
        dest[112 + pos] = newV7;
        dest[128 + pos] = newV8;
        dest[144 + pos] = newV9;
        dest[160 + pos] = newV10;
        dest[176 + pos] = newV11;
        dest[192 + pos] = newV12;
        dest[208 + pos] = newV13;
        dest[224 + pos] = newV14;
        dest[240 + pos] = newV15;

        // V[16] is always 0.0
        dest[256 + pos] = 0.0f;

        // Insert V[17-31] (== -newV[15-1]) into actual v
        dest[272 + pos] = -newV15;
        dest[288 + pos] = -newV14;
        dest[304 + pos] = -newV13;
        dest[320 + pos] = -newV12;
        dest[336 + pos] = -newV11;
        dest[352 + pos] = -newV10;
        dest[368 + pos] = -newV9;
        dest[384 + pos] = -newV8;
        dest[400 + pos] = -newV7;
        dest[416 + pos] = -newV6;
        dest[432 + pos] = -newV5;
        dest[448 + pos] = -newV4;
        dest[464 + pos] = -newV3;
        dest[480 + pos] = -newV2;
        dest[496 + pos] = -newV1;

        // Insert V[32] (== -newV[0]) into other v
        dest = (actualV == v1) ? v2 : v1;

        dest[0 + pos] = -newV0;
        // Insert V[33-48] (== newV[16-31]) into other v
        dest[16 + pos] = newV16;
        dest[32 + pos] = newV17;
        dest[48 + pos] = newV18;
        dest[64 + pos] = newV19;
        dest[80 + pos] = newV20;
        dest[96 + pos] = newV21;
        dest[112 + pos] = newV22;
        dest[128 + pos] = newV23;
        dest[144 + pos] = newV24;
        dest[160 + pos] = newV25;
        dest[176 + pos] = newV26;
        dest[192 + pos] = newV27;
        dest[208 + pos] = newV28;
        dest[224 + pos] = newV29;
        dest[240 + pos] = newV30;
        dest[256 + pos] = newV31;

        // Insert V[49-63] (== newV[30-16]) into other v
        dest[272 + pos] = newV30;
        dest[288 + pos] = newV29;
        dest[304 + pos] = newV28;
        dest[320 + pos] = newV27;
        dest[336 + pos] = newV26;
        dest[352 + pos] = newV25;
        dest[368 + pos] = newV24;
        dest[384 + pos] = newV23;
        dest[400 + pos] = newV22;
        dest[416 + pos] = newV21;
        dest[432 + pos] = newV20;
        dest[448 + pos] = newV19;
        dest[464 + pos] = newV18;
        dest[480 + pos] = newV17;
        dest[496 + pos] = newV16;
    }

    private float[] _tmpOut = new float[32];

    private void computePcmSamples() {
        float[] vp = actualV;
        float[] tmpOut = _tmpOut;
        for (int i = 0; i < 32; i++) {
            float[] dp = d16[i];
            float pcmSample = 0;
            int row = i * 16;
            for (int k = 0; k < 16; k++) {
                pcmSample += vp[row + ((actualWritePos - k) & 15)] * dp[k];
            }
            tmpOut[i] = pcmSample * scaleFactor;
        }
    }

    public void calculate_pcm_samples(float[] output) {
        computeNewV();
        computePcmSamples();

        System.arraycopy(_tmpOut, 0, output, 0, 32);

        actualWritePos = (actualWritePos + 1) & 0xf;
        actualV = (actualV == v1) ? v2 : v1;

        for (int p = 0; p < 32; p++) {
            samples[p] = 0.0f;
        }
    }

    public void calculatePcmSamples(int channel, int[] pcmBuffer) {
        computeNewV();
        computePcmSamples();

        for (int i = 0; i < 32; i++) {
            int sample = Math.round(_tmpOut[i]);
            sample = Math.max(-32768, Math.min(32767, sample));
            pcmBuffer[pcmBufferIndex++] = sample;
        }

        actualWritePos = (actualWritePos + 1) & 0xf;
        actualV = (actualV == v1) ? v2 : v1;

        for (int p = 0; p < 32; p++) {
            samples[p] = 0.0f;
        }
    }

    public int getPcmBufferIndex() {
        return pcmBufferIndex;
    }

    public void resetPcmBufferIndex() {
        pcmBufferIndex = 0;
    }

    private static float[] loadD() {
        // ISO/IEC 11172-3 synthesis window, grouped as 32 rows of 16 coefficients.
        return new float[] {
          0.0f, -4.42505E-4f, 0.003250122f, -0.007003784f, 0.031082153f, -0.07862854f, 0.10031128f, -0.57203674f,
          1.144989f, 0.57203674f, 0.10031128f, 0.07862854f, 0.031082153f, 0.007003784f, 0.003250122f, 4.42505E-4f,
          -1.5259E-5f, -4.73022E-4f, 0.003326416f, -0.007919312f, 0.030517578f, -0.08418274f, 0.090927124f, -0.6002197f,
          1.1442871f, 0.54382324f, 0.1088562f, 0.07305908f, 0.03147888f, 0.006118774f, 0.003173828f, 3.96729E-4f,
          -1.5259E-5f, -5.34058E-4f, 0.003387451f, -0.008865356f, 0.029785156f, -0.08970642f, 0.08068848f, -0.6282959f,
          1.1422119f, 0.51560974f, 0.11657715f, 0.06752014f, 0.03173828f, 0.0052948f, 0.003082275f, 3.66211E-4f,
          -1.5259E-5f, -5.79834E-4f, 0.003433228f, -0.009841919f, 0.028884888f, -0.09516907f, 0.06959534f, -0.6562195f,
          1.1387634f, 0.48747253f, 0.12347412f, 0.06199646f, 0.031845093f, 0.004486084f, 0.002990723f, 3.20435E-4f,
          -1.5259E-5f, -6.2561E-4f, 0.003463745f, -0.010848999f, 0.027801514f, -0.10054016f, 0.057617188f, -0.6839142f,
          1.1339264f, 0.45947266f, 0.12957764f, 0.056533813f, 0.031814575f, 0.003723145f, 0.00289917f, 2.89917E-4f,
          -1.5259E-5f, -6.86646E-4f, 0.003479004f, -0.011886597f, 0.026535034f, -0.1058197f, 0.044784546f, -0.71131897f,
          1.1277466f, 0.43165588f, 0.1348877f, 0.051132202f, 0.031661987f, 0.003005981f, 0.002792358f, 2.59399E-4f,
          -1.5259E-5f, -7.47681E-4f, 0.003479004f, -0.012939453f, 0.02508545f, -0.110946655f, 0.031082153f, -0.7383728f,
          1.120224f, 0.40408325f, 0.13945007f, 0.045837402f, 0.03138733f, 0.002334595f, 0.002685547f, 2.44141E-4f,
          -3.0518E-5f, -8.08716E-4f, 0.003463745f, -0.014022827f, 0.023422241f, -0.11592102f, 0.01651001f, -0.7650299f,
          1.1113739f, 0.37680054f, 0.14326477f, 0.040634155f, 0.03100586f, 0.001693726f, 0.002578735f, 2.13623E-4f,
          -3.0518E-5f, -8.8501E-4f, 0.003417969f, -0.01512146f, 0.021575928f, -0.12069702f, 0.001068115f, -0.791214f,
          1.1012115f, 0.34986877f, 0.1463623f, 0.03555298f, 0.030532837f, 0.001098633f, 0.002456665f, 1.98364E-4f,
          -3.0518E-5f, -9.61304E-4f, 0.003372192f, -0.016235352f, 0.01953125f, -0.1252594f, -0.015228271f, -0.816864f,
          1.0897827f, 0.32331848f, 0.1487732f, 0.03060913f, 0.029937744f, 5.49316E-4f, 0.002349854f, 1.67847E-4f,
          -3.0518E-5f, -0.001037598f, 0.00328064f, -0.017349243f, 0.01725769f, -0.12956238f, -0.03237915f, -0.84194946f,
          1.0771179f, 0.2972107f, 0.15049744f, 0.025817871f, 0.029281616f, 3.0518E-5f, 0.002243042f, 1.52588E-4f,
          -4.5776E-5f, -0.001113892f, 0.003173828f, -0.018463135f, 0.014801025f, -0.1335907f, -0.050354004f, -0.8663635f,
          1.0632172f, 0.2715912f, 0.15159607f, 0.0211792f, 0.028533936f, -4.42505E-4f, 0.002120972f, 1.37329E-4f,
          -4.5776E-5f, -0.001205444f, 0.003051758f, -0.019577026f, 0.012115479f, -0.13729858f, -0.06916809f, -0.89009094f,
          1.0481567f, 0.24650574f, 0.15206909f, 0.016708374f, 0.02772522f, -8.69751E-4f, 0.00201416f, 1.2207E-4f,
          -6.1035E-5f, -0.001296997f, 0.002883911f, -0.020690918f, 0.009231567f, -0.14067078f, -0.088775635f, -0.9130554f,
          1.0319366f, 0.22198486f, 0.15196228f, 0.012420654f, 0.02684021f, -0.001266479f, 0.001907349f, 1.06812E-4f,
          -6.1035E-5f, -0.00138855f, 0.002700806f, -0.02178955f, 0.006134033f, -0.14367676f, -0.10916138f, -0.9351959f,
          1.0146179f, 0.19805908f, 0.15130615f, 0.00831604f, 0.025909424f, -0.001617432f, 0.001785278f, 1.06812E-4f,
          -7.6294E-5f, -0.001480103f, 0.002487183f, -0.022857666f, 0.002822876f, -0.1462555f, -0.13031006f, -0.95648193f,
          0.99624634f, 0.17478943f, 0.15011597f, 0.004394531f, 0.024932861f, -0.001937866f, 0.001693726f, 9.1553E-5f,
          -7.6294E-5f, -0.001586914f, 0.002227783f, -0.023910522f, -6.86646E-4f, -0.14842224f, -0.15220642f, -0.9768524f,
          0.9768524f, 0.15220642f, 0.14842224f, 6.86646E-4f, 0.023910522f, -0.002227783f, 0.001586914f, 7.6294E-5f,
          -9.1553E-5f, -0.001693726f, 0.001937866f, -0.024932861f, -0.004394531f, -0.15011597f, -0.17478943f, -0.99624634f,
          0.95648193f, 0.13031006f, 0.1462555f, -0.002822876f, 0.022857666f, -0.002487183f, 0.001480103f, 7.6294E-5f,
          -1.06812E-4f, -0.001785278f, 0.001617432f, -0.025909424f, -0.00831604f, -0.15130615f, -0.19805908f, -1.0146179f,
          0.9351959f, 0.10916138f, 0.14367676f, -0.006134033f, 0.02178955f, -0.002700806f, 0.00138855f, 6.1035E-5f,
          -1.06812E-4f, -0.001907349f, 0.001266479f, -0.02684021f, -0.012420654f, -0.15196228f, -0.22198486f, -1.0319366f,
          0.9130554f, 0.088775635f, 0.14067078f, -0.009231567f, 0.020690918f, -0.002883911f, 0.001296997f, 6.1035E-5f,
          -1.2207E-4f, -0.00201416f, 8.69751E-4f, -0.02772522f, -0.016708374f, -0.15206909f, -0.24650574f, -1.0481567f,
          0.89009094f, 0.06916809f, 0.13729858f, -0.012115479f, 0.019577026f, -0.003051758f, 0.001205444f, 4.5776E-5f,
          -1.37329E-4f, -0.002120972f, 4.42505E-4f, -0.028533936f, -0.0211792f, -0.15159607f, -0.2715912f, -1.0632172f,
          0.8663635f, 0.050354004f, 0.1335907f, -0.014801025f, 0.018463135f, -0.003173828f, 0.001113892f, 4.5776E-5f,
          -1.52588E-4f, -0.002243042f, -3.0518E-5f, -0.029281616f, -0.025817871f, -0.15049744f, -0.2972107f, -1.0771179f,
          0.84194946f, 0.03237915f, 0.12956238f, -0.01725769f, 0.017349243f, -0.00328064f, 0.001037598f, 3.0518E-5f,
          -1.67847E-4f, -0.002349854f, -5.49316E-4f, -0.029937744f, -0.03060913f, -0.1487732f, -0.32331848f, -1.0897827f,
          0.816864f, 0.015228271f, 0.1252594f, -0.01953125f, 0.016235352f, -0.003372192f, 9.61304E-4f, 3.0518E-5f,
          -1.98364E-4f, -0.002456665f, -0.001098633f, -0.030532837f, -0.03555298f, -0.1463623f, -0.34986877f, -1.1012115f,
          0.791214f, -0.001068115f, 0.12069702f, -0.021575928f, 0.01512146f, -0.003417969f, 8.8501E-4f, 3.0518E-5f,
          -2.13623E-4f, -0.002578735f, -0.001693726f, -0.03100586f, -0.040634155f, -0.14326477f, -0.37680054f, -1.1113739f,
          0.7650299f, -0.01651001f, 0.11592102f, -0.023422241f, 0.014022827f, -0.003463745f, 8.08716E-4f, 3.0518E-5f,
          -2.44141E-4f, -0.002685547f, -0.002334595f, -0.03138733f, -0.045837402f, -0.13945007f, -0.40408325f, -1.120224f,
          0.7383728f, -0.031082153f, 0.110946655f, -0.02508545f, 0.012939453f, -0.003479004f, 7.47681E-4f, 1.5259E-5f,
          -2.59399E-4f, -0.002792358f, -0.003005981f, -0.031661987f, -0.051132202f, -0.1348877f, -0.43165588f, -1.1277466f,
          0.71131897f, -0.044784546f, 0.1058197f, -0.026535034f, 0.011886597f, -0.003479004f, 6.86646E-4f, 1.5259E-5f,
          -2.89917E-4f, -0.00289917f, -0.003723145f, -0.031814575f, -0.056533813f, -0.12957764f, -0.45947266f, -1.1339264f,
          0.6839142f, -0.057617188f, 0.10054016f, -0.027801514f, 0.010848999f, -0.003463745f, 6.2561E-4f, 1.5259E-5f,
          -3.20435E-4f, -0.002990723f, -0.004486084f, -0.031845093f, -0.06199646f, -0.12347412f, -0.48747253f, -1.1387634f,
          0.6562195f, -0.06959534f, 0.09516907f, -0.028884888f, 0.009841919f, -0.003433228f, 5.79834E-4f, 1.5259E-5f,
          -3.66211E-4f, -0.003082275f, -0.0052948f, -0.03173828f, -0.06752014f, -0.11657715f, -0.51560974f, -1.1422119f,
          0.6282959f, -0.08068848f, 0.08970642f, -0.029785156f, 0.008865356f, -0.003387451f, 5.34058E-4f, 1.5259E-5f,
          -3.96729E-4f, -0.003173828f, -0.006118774f, -0.03147888f, -0.07305908f, -0.1088562f, -0.54382324f, -1.1442871f,
          0.6002197f, -0.090927124f, 0.08418274f, -0.030517578f, 0.007919312f, -0.003326416f, 4.73022E-4f, 1.5259E-5f
        };
    }

    private static float[][] splitArray(float[] array, int blockSize) {
        int size = array.length / blockSize;
        float[][] result = new float[size][blockSize];
        for (int i = 0; i < size; i++) {
            System.arraycopy(array, i * blockSize, result[i], 0, blockSize);
        }
        return result;
    }

    // DCT coefficients
    private static final float cos1_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI / 64.0)));
    private static final float cos3_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 3.0 / 64.0)));
    private static final float cos5_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 5.0 / 64.0)));
    private static final float cos7_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 7.0 / 64.0)));
    private static final float cos9_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 9.0 / 64.0)));
    private static final float cos11_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 11.0 / 64.0)));
    private static final float cos13_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 13.0 / 64.0)));
    private static final float cos15_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 15.0 / 64.0)));
    private static final float cos17_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 17.0 / 64.0)));
    private static final float cos19_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 19.0 / 64.0)));
    private static final float cos21_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 21.0 / 64.0)));
    private static final float cos23_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 23.0 / 64.0)));
    private static final float cos25_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 25.0 / 64.0)));
    private static final float cos27_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 27.0 / 64.0)));
    private static final float cos29_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 29.0 / 64.0)));
    private static final float cos31_64 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 31.0 / 64.0)));
    private static final float cos1_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI / 32.0)));
    private static final float cos3_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 3.0 / 32.0)));
    private static final float cos5_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 5.0 / 32.0)));
    private static final float cos7_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 7.0 / 32.0)));
    private static final float cos9_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 9.0 / 32.0)));
    private static final float cos11_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 11.0 / 32.0)));
    private static final float cos13_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 13.0 / 32.0)));
    private static final float cos15_32 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 15.0 / 32.0)));
    private static final float cos1_16 = (float) (1.0 / (2.0 * Math.cos(Math.PI / 16.0)));
    private static final float cos3_16 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 3.0 / 16.0)));
    private static final float cos5_16 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 5.0 / 16.0)));
    private static final float cos7_16 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 7.0 / 16.0)));
    private static final float cos1_8 = (float) (1.0 / (2.0 * Math.cos(Math.PI / 8.0)));
    private static final float cos3_8 = (float) (1.0 / (2.0 * Math.cos(Math.PI * 3.0 / 8.0)));
    private static final float cos1_4 = (float) (1.0 / (2.0 * Math.cos(Math.PI / 4.0)));
}
