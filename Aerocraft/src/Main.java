import gnu.io.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;

public class Main  implements SerialPortEventListener {
    
    SerialPort serialPort;
    private BufferedReader input;
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    Robot robot;
    Thread keyPressThread;
    Thread stopCheckThread;

    public int BPM;
    public long before;
    public int nowTime[];
    public int count;
    public boolean isFirst;
    boolean isDash;

    public void variableInit(){
        BPM = 0;
        before = 0;
        nowTime = new int[2];
        count = 0;
        isFirst = true;
        isDash = false;
    }

    public void initialize(String port) {

        variableInit();

        serialPort = null;
        try {
            robot = new Robot();
        } catch (AWTException ae) {
            ae.printStackTrace();
        }

        CommPortIdentifier portId = null;
        try {
            portId = CommPortIdentifier.getPortIdentifier(port);
        } catch (NoSuchPortException e) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        keyPressThread = new Thread() {
            public void run() {
                while(true){
                    if(BPM >= 90) {
                        isDash = true;
                        robot.keyPress(KeyEvent.VK_W);
                        robot.keyPress(KeyEvent.VK_L);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_L);

                    } else if (BPM >= 40) {
                        if(isDash){
                            robot.keyRelease(KeyEvent.VK_W);
                            robot.delay(100);
                        }
                        isDash = false;
                        robot.keyPress(KeyEvent.VK_W);
                    } else {
                        isDash = false;
                        robot.keyRelease(KeyEvent.VK_W);
                    }

                    robot.delay(100);
                }
            }
        };
        stopCheckThread = new Thread() {
            @Override
            public void run() {
                while(true){
                    if((System.currentTimeMillis() - before) >= 1000 && before != 0){
                        BPM = 0;
                        count = 0;
                        isFirst=true;
                        before = 0;
                        nowTime[0] = 0;
                        nowTime[1] = 0;

                        robot.keyRelease(KeyEvent.VK_W);
                        robot.delay(50);
                        robot.keyPress(KeyEvent.VK_W);
                        robot.delay(50);
                        robot.keyRelease(KeyEvent.VK_W);

                        System.out.println("stop");
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        System.out.println("Started");
        keyPressThread.start();
        stopCheckThread.start();
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = input.readLine();

                if(before == 0){
                    before = System.currentTimeMillis();
                    BPM = 60;
                    System.out.println("start");
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



}