import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * P2P image sharing system - server program
 * It will send out blocks of images and peer information to the other peers.
 */
public class ImageServer {

    private int maxNoOfPeers;   // max number of peers to be returned to a login request
    private PeerList peers = new PeerList();
    private ArrayList<ObjectOutputStream> writers = new ArrayList<ObjectOutputStream>();
    private JFrame jf;
    private JPanel imgBlock;
    private BufferedImage img;
    private BufferedImage[] imgs;
    private JLabel[] labels;    // labels that display the divided image
    private int pressed, released;  // save the pressed and released image blocks
    private boolean changingIcon = false;

    public static void main(String[] args) {

        if (args.length == 0)
            new ImageServer().start(5);
        else
            new ImageServer().start(Integer.parseInt(args[0]));

    }

    /**
     * Start of the program
     * @param m Max number of peers to be returned to a login request
     */
    public void start(int m) {

        maxNoOfPeers = m;
        new Thread(new GUI()).start();
        new Thread(new newPeersHandler()).start();
        new Thread(new activeCheck()).start();
        new Thread(new imgSharing()).start();
        new Thread(new sendList()).start();

    }

    /**
     * sendList thread
     * It sends peer list to clients and listen to port 8999
     */
    public class sendList implements Runnable {

        /**
         * It sends peer list to clients and listen to port 8999
         */
        public void run() {

            try {

                ServerSocket ss = new ServerSocket(8999);   // clients request list in port 8999

                while (true) {

                    Socket s = ss.accept();
                    ObjectOutputStream w = new ObjectOutputStream(s.getOutputStream());
                    w.writeObject(new Message("", "", "UPDATE_PEERS", 0, null, peers.getPeers(maxNoOfPeers)));
                    System.out.println("Send updated list to IP " + s.getLocalAddress().getHostAddress() + " Port " + s.getPort());
                    w.flush();
                    w.close();
                    s.close();

                }

            } catch (Exception e) {

                // e.printStackTrace();

            }

        }

    }

    /**
     * activeCheck thread
     * It checks whether the clients are still active or not (timeout? / closed)
     */
    public class activeCheck implements Runnable {

        /**
         * It checks whether the clients are still active or not (timeout? / closed)
         */
        public void run() {

            try {

                while (true) {

                    Thread.sleep(10000);    // check every 10 seconf
                    for (int i = peers.getSize() - 1; i >= 0; i--) {

                       try {

                           Socket s = new Socket();
                           s.connect(new InetSocketAddress(peers.getIP(i),peers.getPort(i)),2000);  // timeout if > 2 second

                       } catch (SocketTimeoutException e) { // connection timeout

                           System.err.println("Peer " + peers.getName(i) + " IP " + peers.getIP(i) + " Port " + peers.getPort(i) + " disconnected");
                           peers.remove(i);
                           writers.remove(i);

                       } catch (ConnectException e) {   // connection refused

                           System.err.println("Peer " + peers.getName(i) + " IP " + peers.getIP(i) + " Port " + peers.getPort(i) + " disconnected");
                           peers.remove(i);
                           writers.remove(i);

                       } catch (Exception e) {

                           // e.printStackTrace();

                       }

                    }

                }

            } catch (Exception e) {

                // e.printStackTrace();

            }

        }

    }

    /**
     * imgSharing thread
     * It creates a new serversocket and an upload thread for each new client
     */
    public class imgSharing implements Runnable {

        private int port = 9001;    // starting port number for the first client

        /**
         * It creates a new serversocket and an upload thread for each new client
         */
        public void run() {

            ServerSocket ss = null;

            while (ss == null && port < 65536){

                try{

                    ss = new ServerSocket(port);

                } catch (Exception e) {

                    port++;

                }

            }

            while (true) {

                try {

                    new Thread(new upload(ss.accept())).start();    // create an upload thread for each client

                } catch (Exception e) {

                    // e.printStackTrace();

                }

            }

        }

        /**
         * upload thread
         * Each thread upload image blocks to one client
         */
        public class upload implements Runnable{

            private Socket s;
            private ObjectOutputStream writer;

            /**
             * Set up the upload thread
             * @param s Client socket
             * @throws Exception  Cannot connect client / get output stream
             */
            public upload(Socket s) throws Exception{

                this.s = s;
                writer = new ObjectOutputStream(s.getOutputStream());
                writers.add(writer);

            }

            /**
             * Each thread upload image blocks to one client
             */
            public void run(){

                try{

                    // random block number
                    int blockNo = new Random().nextInt(100);

                    while (true){

                        Thread.sleep(10);
                        writer.reset();

                        // upload image blocks in a round robin fashion
                        if (blockNo < 99)
                            blockNo++;
                        else
                            blockNo = 0;

                        while (changingIcon == true) {

                            Thread.sleep(500);

                        }
                        writer.writeObject(new Message("", "", "SEND_IMG_BLOCK", blockNo, labels[blockNo].getIcon(), null));
                        System.out.println("Send to IP: " + s.getLocalAddress().getHostAddress() + " Port: " + s.getPort() + " Block: " + (blockNo));
                        writer.flush();

                    }

                } catch (Exception ex){

                    // ex.printStackTrace();

                }

            }
        }

    }

    /**
     * newPeersHandler thread
     * It listens to port 9000 and handle new peer logins and add clients to peer list
     */
    public class newPeersHandler implements Runnable {

        /**
         * It listens to port 9000 and handle new peer logins and add clients to peer list
         */
        public void run() {

            try {

                Record record = new Record();
                ArrayList<User> users = record.loadRecord("User.txt");

                ServerSocket ss = new ServerSocket(9000);   // listen to port 9000

                while(true) {

                    Socket s = ss.accept();

                    ObjectInputStream reader = new ObjectInputStream(s.getInputStream());
                    ObjectOutputStream writer = new ObjectOutputStream(s.getOutputStream());
                    Message msg = (Message) reader.readObject();

                    boolean success = false;
                    for (int i = 0; i < users.size(); i++)
                        if (users.get(i).getName().equals(msg.me))
                            if (users.get(i).login((msg.data.toString())) == 1) {  // login success

                                writer.writeObject(new Message("Teacher", msg.me, "LOGIN_OK",  100, s.getPort(), peers.getPeers(maxNoOfPeers)));

                                peers.printList();
                                peers.addPeer(new PeerInformation(users.get(i).getName(), users.get(i).getLastLoginDate(), s.getInetAddress().getHostAddress(), s.getPort()));
                                writers.add(new ObjectOutputStream(s.getOutputStream()));
                                record.storeRecord(users, "User.txt"); // update last login date
                                success = true;

                            }

                    if (success == false) { // login locked or fail

                        writer.writeObject(new Message("Teacher", msg.me, "LOGIN_FAILED", 100, null, null));
                        record.storeRecord(users, "User.txt");  // update login fail times

                    }

                    writer.flush();

                    reader.close();
                    writer.close();
                    s.close();

                }

            } catch (Exception e) {

                // e.printStackTrace();

            }

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
            jf = new JFrame("Image Server");
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.setSize(700, 750);
            jf.setResizable(false);

            // image blocks
            imgBlock = new JPanel();
            imgBlock.setSize(700,  700);
            imgBlock.setLayout(new GridLayout(10, 10, 0, 0));
            imgBlock.setPreferredSize(new Dimension(700, 700));
            jf.getContentPane().add(BorderLayout.NORTH, imgBlock);

            JButton selectImg = new JButton("Load another image");
            jf.getContentPane().add(BorderLayout.SOUTH, selectImg);
            selectImg.addActionListener(new SelectImage());

            // require select image before showing main form
            selectImg.doClick();
            jf.setVisible(true);

        }

        /**
         * Listener for image selection
         */
        class SelectImage implements ActionListener {

            /**
             * Select image for peer sharing
             * @param ae Action event
             */
            public void actionPerformed(ActionEvent ae) {

                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {

                    File selectedFile = fileChooser.getSelectedFile();

                    try {

                        img = resizeImage(new File(selectedFile.getAbsolutePath()), 700, 700);

                    } catch (Exception e) {

                        // if the image file fails to load, the program terminates
                        JOptionPane.showMessageDialog(null, "Load fail", "Error", JOptionPane.INFORMATION_MESSAGE);
                        if (img == null)
                            System.exit(1);
                        else
                            return;

                    }

                    divideImage();
                    setImages();

                } else {

                    // if the image file fails to load, the program terminates
                    System.exit(1);

                }

            }

        }

        /**
         * Listener for mouse movements on image labels
         */
        class LabelListener implements MouseListener {

            /**
             * Not used
             */
            public void mouseEntered(MouseEvent e) { }

            /**
             * Not used
             */
            public void mouseClicked(MouseEvent e) { }

            /**
             * Not used
             */
            public void mouseExited(MouseEvent e) { }

            /**
             * Swap images and send swap image command to all existing peers
             * @param e Mouse event
             */
            public void mouseReleased(MouseEvent e) {

                int x = e.getXOnScreen() - jf.getLocation().x;
                int y = e.getYOnScreen() - jf.getLocation().y - (jf.getHeight() - jf.getContentPane().getHeight());

                if (x > 700 || y > 700 || x < 0 || y < 0)  return; // released coordinate out of game area

                int c = x / 70;  // column no.
                int r = y / 70; // row no.
                released = r * 10 + c; // index of images

                // swap images
                Icon tmp = labels[pressed].getIcon();
                labels[pressed].setIcon(labels[released].getIcon());
                labels[released].setIcon(tmp);

                try {

                    // send swap image command to all existing peers
                    for (ObjectOutputStream w : writers)
                        w.writeObject(new Message("", "", "SWAP_IMG_BLOCK", pressed, released, null));

                } catch (Exception ex) {

                    // ex.printStackTrace();

                }

            }

            /**
             * Get the pressed position of images
             * @param e Mouse event
             */
            public void mousePressed(MouseEvent e) {

                int x = e.getXOnScreen() - jf.getLocation().x;
                int y = e.getYOnScreen() - jf.getLocation().y - (jf.getHeight() - jf.getContentPane().getHeight());
                int c = x / 70;  // row no.
                int r = y / 70; // column no.
                pressed = r * 10 + c;  // index of images

            }

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

                    imgs[count] = new BufferedImage(chunkWidth, chunkHeight, img.getType());
                    Graphics2D gr = imgs[count++].createGraphics();
                    gr.drawImage(img, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                    gr.dispose();

                }

        }

        /**
         * Display the images on screen
         */
        public void setImages() {

            imgBlock.removeAll();
            imgBlock.revalidate();
            imgBlock.repaint();
            changingIcon = true;
            labels = new JLabel[imgs.length];
            for (int i = 0; i < imgs.length; i++) {

                labels[i] = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().createImage(imgs[i].getSource())));
                labels[i].addMouseListener(new LabelListener());
                imgBlock.add(labels[i]);

            }

            SwingUtilities.updateComponentTreeUI(jf);   // refresh
            changingIcon = false;

        }
    }

}
