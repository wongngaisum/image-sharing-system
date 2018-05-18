import java.io.Serializable;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * Stores the information of a client
 */
public class PeerInformation implements Serializable {

    /**
     * Username
     * Last login date
     * IP address
     */
    private String name, date, ip;

    /**
     * Port number
     */
    private int port;

    /**
     * Stores information in a peer object
     * @param name Username
     * @param date Last login date
     * @param IP IP address
     * @param port Port number
     */
    public PeerInformation(String name, String date, String IP, int port) {

        this.name = name;
        this.date = date;
        this.ip = IP;
        this.port = port;

    }

    /**
     * Generate a string containing the peer information
     * @return Peer name, last login date, ip address, port number
     */
    public String toString() {

        return "User: " + getName() + " Last login date: " + getDate() + " Peer IP: " + ip + " Port number: " + port;

    }

    /**
     * Get client name
     * @return client name
     */
    public String getName() {

        return name;

    }

    /**
     * Get client's last login date
     * @return Client's last login date
     */
    public String getDate() {

        return date;

    }

    /**
     * Get client's ip address
     * @return Client's ip address
     */
    public String getIP() {

        return ip;

    }

    /**
     * Get client's port number
     * @return Client's port number
     */
    public int getPort() {

        return port;

    }

}
