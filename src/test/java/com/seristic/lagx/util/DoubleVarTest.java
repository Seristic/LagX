package com.seristic.lagx.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DoubleVarTest {

    @Test
    public void testGetVar1() {
        DoubleVar<String, Integer> doubleVar = new DoubleVar<>("Hello", 42);
        assertEquals("Hello", doubleVar.getVar1());
    }

    @Test
    public void testGetVar2() {
        DoubleVar<String, Integer> doubleVar = new DoubleVar<>("Hello", 42);
        assertEquals(42, doubleVar.getVar2());
    }

    @Test
    public void testConstructor() {
        DoubleVar<Double, String> doubleVar = new DoubleVar<>(3.14, "Pi");
        assertEquals(3.14, doubleVar.getVar1());
        assertEquals("Pi", doubleVar.getVar2());
    }
}