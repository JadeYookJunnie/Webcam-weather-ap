package com.example.assignment3;
import static org.mockito.Mockito.*;
import junit.framework.TestCase;

import org.junit.Test;

public class MapsActivityTest extends TestCase {
    @Test
    public void testWorks() {
        Validator validator = new Validator();
        int test = validator.exTest(2, 3);
        assertEquals(5, test);
    }


}