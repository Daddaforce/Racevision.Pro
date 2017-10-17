package seng302.utilities;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by gemma on 12/04/2017.
 */
public class PolarReaderTest {


    @Test
    public void getPolars() throws Exception {
        assertEquals(7, PolarReader.getPolarsForAC35Yachts().size());
    }


}