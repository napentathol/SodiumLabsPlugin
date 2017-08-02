package us.sodiumlabs.plugins.mobs;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class RICOTest {
    @Test
    public void calculateDamage() throws Exception {
        final Logger logger = mock(Logger.class);
        final Random random = mock(Random.class);

        final RICO rico = new RICO(logger, random);

        Mockito.when(random.nextDouble()).thenReturn(0.0);
        assertEquals(0, rico.calculateDamage(10,10));
        assertEquals(-5, rico.calculateDamage(10,5));
        assertEquals(-9, rico.calculateDamage(10,1));

        Mockito.when(random.nextDouble()).thenReturn(0.71);
        assertEquals(5, rico.calculateDamage(10,10));
        assertEquals(0, rico.calculateDamage(10,5));
        assertEquals(-4, rico.calculateDamage(10,1));

        Mockito.when(random.nextDouble()).thenReturn(1.0);
        assertEquals(10, rico.calculateDamage(10,10));
        assertEquals(5, rico.calculateDamage(10,5));
        assertEquals(1, rico.calculateDamage(10,1));

        Mockito.when(random.nextDouble()).thenReturn(-0.71);
        assertEquals(5, rico.calculateDamage(10,10));
        assertEquals(0, rico.calculateDamage(10,5));
        assertEquals(-4, rico.calculateDamage(10,1));

        Mockito.when(random.nextDouble()).thenReturn(-1.0);
        assertEquals(10, rico.calculateDamage(10,10));
        assertEquals(5, rico.calculateDamage(10,5));
        assertEquals(1, rico.calculateDamage(10,1));

    }

}