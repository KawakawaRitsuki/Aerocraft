import gnu.io.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.awt.*;
import java.io.*;
import java.util.Enumeration;

public class Main  implements SerialPortEventListener {
    SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private static final String PORT_NAMES[] = {
            "/dev/cu.usbmodem1411", // Mac OS X
            "/dev/cu.usbmodem1421", // Mac OS X
    };
    /**
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 9600;

    Robot robot;

    public void initialize() {
        try {
            robot = new Robot();
        } catch (AWTException ae) {
            ae.printStackTrace();
        }
        // the next line is for Raspberry Pi and
        // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
        System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/cu.usbmodem1411");

        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    private int BPM;
    private long before = 0;
    private int nowTime[] = new int[2];
    private int count = 0;
    private boolean isFirst = true;
    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = input.readLine();
//                System.out.println(inputLine);

                if(before == 0){
                    before = System.currentTimeMillis();
                    BPM = 60;
                } else {
                    long now = System.currentTimeMillis();
                    nowTime[count] = (int)(now - before);
                    before = now;
                    count++;
                    if (count == 2) count = 0;

                    int sum = 0;
                    for(int i:nowTime){
//                        System.out.println(i);
                        sum = sum + i;
                    }
                    BPM = 60000 / sum * 2;
                    if(isFirst){
                        BPM = BPM / 2;
                        isFirst=false;
                    }
                    System.out.println(BPM);
                }

            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    boolean isDash = false;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.initialize();
        Thread t = new Thread() {
            public void run() {
                while(true){
                    if(main.BPM >= 90) {
                        main.isDash = true;
                        main.robot.keyPress(java.awt.event.KeyEvent.VK_L);
                        main.robot.keyPress(java.awt.event.KeyEvent.VK_W);
                    } else if (main.BPM >= 40) {
                        if(main.isDash){
                            main.robot.keyRelease(java.awt.event.KeyEvent.VK_L);
                            main.robot.keyPress(java.awt.event.KeyEvent.VK_K);
                            main.robot.delay(100);
                            main.robot.keyRelease(java.awt.event.KeyEvent.VK_K);
                            main.isDash = false;
                        }
                        main.robot.keyPress(java.awt.event.KeyEvent.VK_W);
                    } else {
                        if(main.isDash){
                            main.isDash = false;
                            main.robot.keyRelease(java.awt.event.KeyEvent.VK_L);
                        }
                        main.robot.keyRelease(java.awt.event.KeyEvent.VK_W);
                    }
                }
            }
        };
        t.start();
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if((System.currentTimeMillis() - main.before) >= 1000 && main.before != 0){
                        main.BPM = 0;
                        main.count = 0;
                        main.isFirst=true;
                        main.before = 0;
                        main.nowTime[0] = 0;
                        main.nowTime[1] = 0;
                        System.out.println("TEST");
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        t2.start();
        System.out.println("Started");
    }
}
