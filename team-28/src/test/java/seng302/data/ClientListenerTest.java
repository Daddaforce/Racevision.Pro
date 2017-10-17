package seng302.data;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.controllers.listeners.ClientListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class ClientListenerTest {
    private static ServerSocket testFeedSocket;

    private static final String TEST_FEED_ADDRESS = "127.0.0.1";
    private static final int TEST_FEED_PORT = 1234;
    private static final double DELTA = 1e-6;

    @BeforeClass
    public static void beforeClass() throws IOException{
        testFeedSocket = new ServerSocket(TEST_FEED_PORT);
    }

    @Test
    public void setUpConnectionTest(){
        ClientListener testReader = new ClientListener(TEST_FEED_ADDRESS, TEST_FEED_PORT);

        Assert.assertNull(testReader.getSocket());

        testReader.setUpConnection(TEST_FEED_ADDRESS, TEST_FEED_PORT);

        Assert.assertNotNull(testReader.getSocket());
        InetAddress address = testReader.getSocket().getInetAddress();

        Assert.assertEquals(TEST_FEED_PORT, testReader.getSocket().getPort());
        Assert.assertEquals(TEST_FEED_ADDRESS, address.getHostAddress());
    }

    @Test
    public void readXMLTest() throws IOException{
        //Test when integrating data generation
    }

    @Test
    public void byteArrayRangeToIntTest(){
        byte[] testByteArray = new byte[]{8, 52, 1, 0, 2};
        Assert.assertEquals(8, ClientListener.byteArrayRangeToInt(testByteArray, 0, 1));
        Assert.assertEquals(131073, ClientListener.byteArrayRangeToInt(testByteArray, 2, 5));
        Assert.assertEquals(78856, ClientListener.byteArrayRangeToInt(testByteArray, 0, 4));
    }

    @Test(expected=IllegalArgumentException.class)
    public void byteArrayRangeToIntExceptionTest(){
        byte[] testByteArray = new byte[]{8, 52, 1, 0, 2};
        ClientListener.byteArrayRangeToInt(testByteArray, 0, 5);
    }

    @Test
    public void intToLatLonTest(){
        Assert.assertEquals(180, ClientListener.intToLatLon((int)Math.pow(2, 31)), DELTA);
        Assert.assertEquals(-180, ClientListener.intToLatLon((int)-Math.pow(2, 31)), DELTA);
        Assert.assertEquals(0, ClientListener.intToLatLon(0), DELTA);
        Assert.assertEquals(41.90951585, ClientListener.intToLatLon(500000000), DELTA);
        Assert.assertEquals(-33.5276126, ClientListener.intToLatLon(-400000000), DELTA);
    }

    @Test
    public void intToHeadingTest(){
        Assert.assertEquals(0, ClientListener.intToHeading(0x0000), DELTA);
        Assert.assertEquals(90, ClientListener.intToHeading(0x4000), DELTA);
        Assert.assertEquals(180, ClientListener.intToHeading(0x8000), DELTA);
        Assert.assertEquals(270, ClientListener.intToHeading(0xC000), DELTA);

        Assert.assertEquals(241.5948486, ClientListener.intToHeading(0xABCD), DELTA);
        Assert.assertEquals(23.99963378, ClientListener.intToHeading(0x1111), DELTA);
    }
}
