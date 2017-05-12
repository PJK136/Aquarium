package fr.aquarium;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.invoke.MethodHandles;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArduinoUsbChannel {
    protected final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    protected final SerialPort serialPort;

    protected PipedInputStream vcpOutputDataStream;
    protected PipedOutputStream vcpOutputInnerStreamWriter;

    protected PipedInputStream vcpInputInnerStream;
    protected PipedOutputStream vcpInputDataStreamWriter;

    protected Thread writingThread;

    public ArduinoUsbChannel(String port) throws IOException {

        this.serialPort = new SerialPort(port);

        this.vcpOutputDataStream = new PipedInputStream();
        this.vcpOutputInnerStreamWriter = new PipedOutputStream(vcpOutputDataStream);

        this.vcpInputInnerStream = new PipedInputStream();
        this.vcpInputDataStreamWriter = new PipedOutputStream(this.vcpInputInnerStream);

    }

    public static String getOneComPort() {

        String myVirtualComPort = null;

        logger.debug("COM Port Names : ");

        String[] portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            logger.debug(portName);
            if (myVirtualComPort == null) {
                myVirtualComPort = portName;
            }
        }

        return myVirtualComPort;
    }

    public void open() throws SerialPortException, IOException {

        //logger.debug("Opening VCP...");
        serialPort.openPort();//Open serial port
        serialPort.setParams(SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(115200, 8, 1, 0);

        int mask = SerialPort.MASK_RXCHAR; // + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
        serialPort.setEventsMask(mask);//Set mask
        serialPort.addEventListener(new SerialPortEventListener() {

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.isRXCHAR()) {//If data is available

                    try {
                        byte buffer[] = serialPort.readBytes();

                        //logger.debug("=> VCP Data String received: " + new String(buffer));

                        if (buffer != null) {
                            vcpOutputInnerStreamWriter.write(buffer);
                            vcpOutputInnerStreamWriter.flush();
                        }

                    } catch (SerialPortException ex) {
                        logger.error(ex.getClass().getName(), ex);
                    } catch (IOException ex) {
                        logger.error(ex.getClass().getName(), ex);
                    }
                } else if (event.isCTS()) {//If CTS line has changed state
                    if (event.getEventValue() == 1) {//If line is ON
                        logger.debug("CTS - ON");
                    } else {
                        logger.debug("CTS - OFF");
                    }
                } else if (event.isDSR()) {///If DSR line has changed state
                    if (event.getEventValue() == 1) {//If line is ON
                        logger.debug("DSR - ON");
                    } else {
                        logger.debug("DSR - OFF");
                    }
                }
            }
        });

        this.writingThread = new Thread(new Runnable() {

            @Override
            public void run() {

                PipedInputStream input = (PipedInputStream) ArduinoUsbChannel.this.vcpInputInnerStream;

                try {

                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = input.read(buffer)) > 0) {

                        //logger.debug("=> VCP Data String to be written: " + new String(buffer, 0, length));

                        for (int i = 0; i < length; i++) {
                            //VCPChannel.this.serialPort.writeBytes(data);
                            ArduinoUsbChannel.this.serialPort.writeByte(buffer[i]);
                        }

                    }
                } catch (IOException ex) {
                    logger.error(ex.getClass().getName(), ex);
                } catch (SerialPortException ex) {
                    logger.error(ex.getClass().getName(), ex);
                }
            }
        });

        this.writingThread.start();
    }

    public void close() throws IOException {

        this.vcpOutputDataStream.close();
        this.vcpInputDataStreamWriter.close();

        //logger.debug("Closing VCP...");
        try {
            this.serialPort.closePort();//Close serial port
        } catch (SerialPortException ex) {
            logger.error(ex.getClass().getName(), ex);
        }

        this.vcpOutputInnerStreamWriter.close();
        this.vcpInputInnerStream.close();

        this.writingThread.interrupt();
        try {
            this.writingThread.join(1000);
        } catch (InterruptedException ex) {
            logger.error(ex.getClass().getName(), ex);
        }
    }

    public OutputStream getWriter() {
        return this.vcpInputDataStreamWriter;
    }

    public InputStream getReader() {
        return this.vcpOutputDataStream;
    }

}
