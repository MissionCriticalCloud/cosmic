package com.cloud.consoleproxy.vnc.packet.client;

import com.cloud.consoleproxy.vnc.RfbConstants;
import com.cloud.consoleproxy.vnc.VncScreenDescription;

import java.io.DataOutputStream;
import java.io.IOException;

public class SetPixelFormatPacket implements ClientPacket {

    private final int bitsPerPixel, depth, bigEndianFlag, trueColourFlag, redMax, greenMax, blueMax, redShift, greenShift, blueShift;

    private final VncScreenDescription screen;

    public SetPixelFormatPacket(final VncScreenDescription screen, final int bitsPerPixel, final int depth, final int bigEndianFlag, final int trueColorFlag, final int redMax,
                                final int greenMax, final int blueMax,
                                final int redShift, final int greenShift, final int blueShift) {
        this.screen = screen;
        this.bitsPerPixel = bitsPerPixel;
        this.depth = depth;
        this.bigEndianFlag = bigEndianFlag;
        this.trueColourFlag = trueColorFlag;
        this.redMax = redMax;
        this.greenMax = greenMax;
        this.blueMax = blueMax;
        this.redShift = redShift;
        this.greenShift = greenShift;
        this.blueShift = blueShift;
    }

    @Override
    public void write(final DataOutputStream os) throws IOException {
        os.writeByte(RfbConstants.CLIENT_SET_PIXEL_FORMAT);

        // Padding
        os.writeByte(0);
        os.writeByte(0);
        os.writeByte(0);

        // Send pixel format
        os.writeByte(bitsPerPixel);
        os.writeByte(depth);
        os.writeByte(bigEndianFlag);
        os.writeByte(trueColourFlag);
        os.writeShort(redMax);
        os.writeShort(greenMax);
        os.writeShort(blueMax);
        os.writeByte(redShift);
        os.writeByte(greenShift);
        os.writeByte(blueShift);

        // Padding
        os.writeByte(0);
        os.writeByte(0);
        os.writeByte(0);

        screen.setPixelFormat(bitsPerPixel, depth, bigEndianFlag, trueColourFlag, redMax, greenMax, blueMax, redShift, greenShift, blueShift);
    }
}
