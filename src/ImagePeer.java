import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * P2P image sharing system - client (peer) program
 * It will download blocks of image from the list of peers and send out blocks of images to the other peers.
 */
public class ImagePeer {

    private JFrame jf;
    private JPanel imgBlock;
    private BufferedImage img = null;
    private BufferedImage[] imgs = null;
    private JLabel[] labels;    // labels that contains the divided image
    private int port;
    private String server;
    private PeerList peerList = new PeerList();
    private boolean initialized = false;    // setup GUI and load default photo

    public static void main(String[] args) {

        new ImagePeer().start();

    }

    /**
     * Start of the program
     */
    public void start() {

        // ask information
        String address = JOptionPane.showInputDialog(null, "Connect to server:", "Input", JOptionPane.QUESTION_MESSAGE);
        String ac = JOptionPane.showInputDialog(null, "Username:", "Input", JOptionPane.QUESTION_MESSAGE);
        String pw = JOptionPane.showInputDialog(null, "Password:", "Input", JOptionPane.QUESTION_MESSAGE);

        try {

            // connect to server
            Socket s = new Socket(address, 9000);

            ObjectOutputStream w = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream r = new ObjectInputStream(s.getInputStream());

            Hash h = new Hash();    // hash password
            w.writeObject(new Message(ac, "Teacher", "LOGIN", 0, h.getHashCode(pw), null)); // check login status
            w.flush();
            Message m = (Message) r.readObject();
            if (m.command.equals("LOGIN_FAILED")) { // display message and exit if fail

                JOptionPane.showMessageDialog(null, "Login fail", "Message", JOptionPane.INFORMATION_MESSAGE);
                System.exit(1);

            }

            server = address;
            peerList = (PeerList) m.peerList;
            port = (int) m.data;

            new Thread(new GUI()).start();
            while (initialized == false){   // wait until setup all GUI components

                Thread.sleep(500);

            }

            new Thread(new upload()).start();
            new Thread(new updateList()).start();
            new Thread(new download(address, 9001)).start();    // try to connect server by port 9001, 9002, 9003...

        } catch (Exception e) {

            System.exit(1);

        }

    }

    /**
     * GUI thread
     * Set up GUI components
     */
    public class GUI implements Runnable {

        /**
         * Set up GUI components
         */
        public void run() {

            // JFrame
            jf = new JFrame("Image Peer");
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.setSize(700, 720);
            jf.setResizable(false);

            // Image blocks
            imgBlock = new JPanel();
            imgBlock.setSize(700,  700);
            imgBlock.setLayout(new GridLayout(10, 10, 0, 0));
            imgBlock.setPreferredSize(new Dimension(700, 700));
            jf.getContentPane().add(BorderLayout.NORTH, imgBlock);

            // Load default photo
            try {

                img = resizeImage(new File("default.png"), 700, 700);

            } catch (Exception e) {

                img = null;

            }
            divideImage();
            setImages();

            jf.setVisible(true);
        }

        /**
         * Resize selected image to certain size
         * @param img image file selected by user
         * @param height desired height
         * @param width desired width
         * @return buffered image
         * @throws IOException Exception when cant open image
         */
        public BufferedImage resizeImage(File img, int height, int width) throws IOException {

            BufferedImage image = ImageIO.read(img);
            Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, image.getType());
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();
            return resized;

        }

        /**
         * Divide the selected image into chunks
         */
        public void divideImage() {

            int chunkWidth = 70;
            int chunkHeight = 70;
            int count = 0;
            imgs = new BufferedImage[100];
            for (int x = 0; x < 10; x++)
                for (int y = 0; y < 10; y++) {

                    try{

                        imgs[count] = new BufferedImage(chunkWidth, chunkHeight, img.getType());
                        Graphics2D gr = imgs[count++].createGraphics();
                        gr.drawImage(img, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                        gr.dispose();

                    } catch (Exception e){

                        // e.printStackTrace();

                    }

                }

        }

        /**
         * Display the images on screen
         */
        public void setImages() {

            imgBlock.removeAll();
            imgBlock.revalidate();
            imgBlock.repaint();
            labels = new JLabel[imgs.length];
            for (int i = 0; i < imgs.length; i++) {

                labels[i] = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().createImage(imgs[i].getSource())));
                imgBlock.add(labels[i]);

            }

            SwingUtilities.updateComponentTreeUI(jf);   // refresh
            initialized = true;

        }

    }

    /**
     * updateList thread
     * Get new peer list every 10 second
     */
    public class updateList implements Runnable {

        /**
         * Get new peer list every 10 second
         */
        public synchronized void run() {

            try {

                while(true) {

                    Thread.sleep(10000);

                    Socket s = new Socket(server, 8999);    // Request list in port 8999
                    ObjectInputStream r = new ObjectInputStream(s.getInputStream());
                    Message m = (Message) r.readObject();
                    peerList = (PeerList) m.peerList;   // Store new peer list

                    for (int i = peerList.getSize() - 1; i >= 0; i--)
                        new Thread(new download(peerList.getIP(i), peerList.getPort(i))).start();   // start thread to download image blocks from other peers

                }

            } catch (Exception e){

                // e.printStackTrace();

            }

        }

    }

    /**
     * upload thread
     * Accept connection from other peers and send out blocks of images to others
     */
    public class upload implements Runnable {

        /**
         * Accept connection from other peers and send out blocks of images to others
         */
        public void run() {

            ServerSocket ss = null;
            try {

                ss = new ServerSocket(port);    // accept connection from other peers and send out blocks of images to others

            } catch (Exception e) {

                // e.printStackTrace();

            }

            while (true) {

                Socket s;

                try {

                    s = ss.accept();

                    new Thread(new uploader(s)).start();    // create an uploader thread for each incoming client

                } catch (Exception e){

                    // e.printStackTrace();

                }

            }

        }

        /**
         * upload thread
         * Each thread upload image blocks to one client
         */
        public class uploader implements Runnable {

            private Socket s;
            private ObjectOutputStream writer;

            /**
             * Set up uploader thread
             * @param s Client socket
             * @throws Exception Cannot connect client / get output stream
             */
            public uploader(Socket s) throws Exception {

                this.s = s;
                writer = new ObjectOutputStream(s.getOutputStream());

            }

            /**
             * Each thread upload image blocks to one client
             */
            public void run() {

                try{

                    // random block number
                    int blockNo = new Random().nextInt(100);

                    while (true){

                        Thread.sleep(500);
                        writer.reset();

                        // upload image blocks in a round robin fashion
                        if (blockNo < 99)
                            blockNo++;
                        else
                            blockNo = 0;

                        writer.writeObject(new Message("", "", "SEND_IMG_BLOCK", blockNo, labels[blockNo].getIcon(), null));
                        System.out.println("Send to IP: " + s.getLocalAddress().getHostAddress() + " Port: " + s.getPort() + " Block: " + (blockNo));
                        writer.flush();

                    }

                } catch (Exception e){

                    // e.printStackTrace();

                }

            }

        }

    }

    /**
     * download thread
     * Try to download blocks of image from the list of peers in parallel (server & clients)
     */
    public class download implements Runnable {

        private String serverIP;
        private int serverPort;

        /**
         * Set up download thread
         * @param serverIP Server IP address
         * @param serverPort Server port number
         */
        public download(String serverIP, int serverPort) {

            this.serverIP=serverIP;
            this.serverPort=serverPort;

        }

        /**
         * Try to download blocks of image from the list of peers in parallel (server & clients)
         */
        public void run() {

            try {

                ObjectInputStream reader = new ObjectInputStream(new Socket(serverIP, serverPort).getInputStream());

                while (true){

                    Message newBlock = (Message)reader.readObject();
                    System.out.println("IP: " + serverIP + " Port: " + serverPort + " Command: " + newBlock.command + " Block: " + newBlock.blockNo + " " + newBlock.data);

                    if (newBlock.command.equals("SEND_IMG_BLOCK"))
                        labels[newBlock.blockNo].setIcon((Icon) newBlock.data);
                    else if (newBlock.command.equals("SWAP_IMG_BLOCK")) {

                        Icon tmp = labels[newBlock.blockNo].getIcon();
                        labels[newBlock.blockNo].setIcon(labels[(int) newBlock.data].getIcon());
                        labels[(int) newBlock.data].setIcon(tmp);

                    }

                    jf.repaint();   // refresh

                }

            } catch (Exception e){

                // e.printStackTrace();

            }

        }
    }

}
