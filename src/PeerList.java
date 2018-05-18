import java.io.*;
import java.util.*;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * A list storing peer information objects
 */
public class PeerList implements Serializable {

    /**
     * An array containing PeerInformation objects
     */
    private ArrayList<PeerInformation> peerList = new ArrayList<PeerInformation>();

    /**
     * Get number of stored objects
     * @return Number of stored objects
     */
    public synchronized int getSize() {

        return peerList.size();

    }

    /**
     * Remove a record by index
     * @param index index of the array
     */
    public synchronized void remove(int index) {

        peerList.remove(index);

    }

    /**
     * Get the peer list containg peer objects
     * @return the peer list
     */
    public synchronized ArrayList<PeerInformation> getList() {

        return peerList;

    }

    /**
     * Print all information about clients
     */
    public synchronized void printList() {

        System.out.println("Number of active peers: " + getSize()); // Print information when add clients
        System.out.println("Current Peers: ");
        for (PeerInformation p : peerList)
            System.out.println(p.toString());

    }

    /**
     * Add peer to the list
     * @param peer Peer information object
     */
    public synchronized void addPeer(PeerInformation peer) {

        peerList.add(peer);

    }

    /**
     * Get IP address of a client
     * @param index Index of array
     * @return IP address of a client
     */
    public synchronized String getIP(int index) {

        return peerList.get(index).getIP();

    }

    /**
     * Get client name
     * @param index Index of array
     * @return Client name
     */
    public synchronized String getName(int index) {

        return peerList.get(index).getName();

    }

    /**
     * Get port number of a client
     * @param index Index of array
     * @return Port number of a client
     */
    public synchronized int getPort(int index) {

        return peerList.get(index).getPort();

    }

    /**
     * Get list that contains at most certain number of peers
     * @param number Maximum number of peers
     * @return List that contains at most certain number of peers
     */
    public synchronized PeerList getPeers(int number) {

        PeerList p = new PeerList();
        for (int i = 0; i < number && i < peerList.size(); i++)
            p.addPeer(peerList.get(i));
        return p;

    }

}
