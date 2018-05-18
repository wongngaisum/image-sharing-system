import java.io.*;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * A message object that is used to store peer list, command, data and so on.
 */
public class Message implements Serializable {

    /**
     * The sender's name
     */
    public String me;

    /**
     * The receiver's name
     */
    public String you;

    /**
     * The command, e.g. swap image, update image, login
     */
    public String command;

    /**
     * The location of a block, total no. of blocks and so on
     */
    public int blockNo;

    /**
     * Image chunk and other data
     */
    public Object data;

    /**
     * Peer list storing information about peers (ip, name, port, etc)
     */
    public Object peerList;

    /**
     * Store information in a message object
     * @param me The sender's name
     * @param you The receiver's name
     * @param command The command, e.g. swap image, update image, login
     * @param blockNo The location of a block, total no. of blocks and so on
     * @param data Image chunk and other data
     * @param peerList Peer list storing information about peers (ip, name, port, etc)
     */
    public Message(String me, String you, String command, int blockNo, Object data, Object peerList) {

        this.me = me;
        this.you = you;
        this.command = command;
        this.blockNo = blockNo;
        this.data = data;
        this.peerList = peerList;

    }

}
