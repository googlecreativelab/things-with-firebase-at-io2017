package net.trippedout.android.thingswithfirebase.drivers;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Based on CPP Impl:
 * https://github.com/sparkfun/SparkFun_ADC_Block_for_Edison_CPP_Library/blob/master/SparkFunADS1015.cpp
 *
 * with lots and lots of bs byte->short->unsigned errors because JAVA.
 */

public class SparkfunADCV20 implements AutoCloseable {

    private static final String TAG = SparkfunADCV20.class.getSimpleName();

    private static final int I2C_ADDRESS = 0x48;
    private I2cDevice mDevice;

    private float _scaler = 1.0f;

    /**
     * Out of the box this bus is soldered.
     */
    private static final String DEFAULT_BUS = "I2C1";

    public static final short _6_144V = 0x00;
    public static final short _4_096V = 0x01;
    public static final short _2_048V = 0x02;
    public static final short _1_024V = 0x03;
    public static final short _0_512V = 0x04;
    public static final short _0_256V = 0x05;

    private final short CONVERSION = 0x00;
    private final short CONFIG = 0x01;

    private final int START_READ = 0x8000;
    private final int CHANNEL_MASK = 0x3000; // There are four channels, and single ended reads
                                            //  are specified by a two-bit address at bits 13:12
    private final int SINGLE_ENDED = 0x4000;   // Set for single-ended
    private final int CFG_REG_CHL_MASK = 0xf000; // Used to clear the high nibble of the cfg regbefore we start our read reques
    private final int BUSY_MASK = 0x8000; // When the highest bit in the cfg reg is set, the conversion is done.
    private final int CHANNEL_SHIFT = 12;

    private final short RANGE_SHIFT = 9;
    private final int RANGE_MASK = 0x0E00; // bits to clear for gain parameter

    public SparkfunADCV20(String bus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice device = pioService.openI2cDevice(bus, I2C_ADDRESS);

        try {
            connect(device);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    public SparkfunADCV20() throws IOException {
        this(DEFAULT_BUS);
    }

    private void connect(I2cDevice device) throws IOException {
        if (mDevice != null) {
            throw new IllegalStateException("device already connected");
        }
        mDevice = device;

        //start at a low power default, and allow the user creation to decide proper voltage
        setRange(_2_048V);
    }

    public void setRange(short range) throws IOException {
        short cfgRegVal = getConfigRegister();
        cfgRegVal &= ~RANGE_MASK;
        cfgRegVal |= (range << RANGE_SHIFT) & RANGE_MASK;
        setConfigRegister(cfgRegVal);

        switch (range) {
            case _6_144V:
                _scaler = 3.0f; // each count represents 3.0 mV
                break;
            case _4_096V:
                _scaler = 2.0f; // each count represents 2.0 mV
                break;
            case _2_048V:
                _scaler = 1.0f; // each count represents 1.0 mV
                break;
            case _1_024V:
                _scaler = 0.5f; // each count represents 0.5mV
                break;
            case _0_512V:
                _scaler = 0.25f; // each count represents 0.25mV
                break;
            case _0_256V:
                _scaler = 0.125f; // each count represents 0.125mV
                break;
            default:
                _scaler = 1.0f;  // here be dragons
                break;
        }
    }

    /**
     * Returns the current reading on a channel, scaled by the current scaler and
     * presented as a floating point number.
     *
     * @param channel the numbered channel you want data from.
     */
    public float getResult(int channel) throws IOException {
        short rawVal = getRawResult(channel);
        return (float)rawVal * _scaler/1000;
    }

    public short getRawResult(int channel) throws IOException {
        short cfgRegVal = getConfigRegister();

        cfgRegVal &= ~CHANNEL_MASK; // clear existing channel settings
        cfgRegVal |= SINGLE_ENDED;  // set the SE bit for a s-e read
        cfgRegVal |= (channel<<CHANNEL_SHIFT) & CHANNEL_MASK; // put the channel bits in
        cfgRegVal |= START_READ;    // set the start read bit

        cfgRegVal = (short) (cfgRegVal & 0xFFFF);

        setConfigRegister(cfgRegVal);

        return readADC();
    }

    short readADC() throws IOException {
        short cfgRegVal = getConfigRegister();
        cfgRegVal |= START_READ; // set the start read bit
        setConfigRegister(cfgRegVal);

        byte[] result = new byte[2];
        short fullValue = 0;
        int busyDelay = 0;

        while ((getConfigRegister() & BUSY_MASK) == 0)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(busyDelay++ > 1000) return (short)0xffff;
        }

        mDevice.readRegBuffer(CONVERSION, result, 2);

        fullValue = (short)((result[0]<<8) + result[1]);
        return (short)(fullValue>>>4);
    }

    void setConfigRegister(short configValue) throws IOException {
        byte[] data = new byte[2];
        data[0] = (byte)((configValue>>>8) & 0xff);
        data[1] = (byte)(configValue & 0xff);

        mDevice.writeRegBuffer(CONFIG, data, 2);
    }

    short getConfigRegister() throws IOException {
        byte[] buff = new byte[2];
        mDevice.readRegBuffer(CONFIG, buff, 2);

        // create short from buffer array
        short regBuff = (short)(((buff[0] & 0xFF) << 8) | (buff[1] & 0xFF ));

        // shift according to how they handle it in cpp lib - not sure why?
        regBuff = (short)((((regBuff & 0xffff)>>>8) | (regBuff & 0xffff <<8)));

        return regBuff;
    }

    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }
}
