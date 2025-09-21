

import org.junit.jupiter.api.Test;

import com.seristic.lagx.util.ColorUtil;

import static org.junit.jupiter.api.Assertions.*;

public class ColorUtilTest {

    @Test
    public void testColor() {
        String input = "&aHello &cWorld";
        String expected = "§aHello §cWorld";
        assertEquals(expected, ColorUtil.color(input));
    }

    @Test
    public void testGetColorFromPercentage() {
        assertEquals("&a", ColorUtil.getColorFromPercentage(95.0));
        assertEquals("&2", ColorUtil.getColorFromPercentage(75.0));
        assertEquals("&c", ColorUtil.getColorFromPercentage(25.0));
        assertEquals("&4", ColorUtil.getColorFromPercentage(15.0));
    }

    @Test
    public void testGetColorFromTps() {
        assertEquals("&a", ColorUtil.getColorFromTps(19.5));
        assertEquals("&e", ColorUtil.getColorFromTps(16.0));
        assertEquals("&c", ColorUtil.getColorFromTps(8.0));
        assertEquals("&4", ColorUtil.getColorFromTps(3.0));
    }

    @Test
    public void testGetColorFromMemory() {
        assertEquals("&a", ColorUtil.getColorFromMemory(50.0));
        assertEquals("&6", ColorUtil.getColorFromMemory(80.0));
        assertEquals("&c", ColorUtil.getColorFromMemory(90.0));
        assertEquals("&4", ColorUtil.getColorFromMemory(96.0));
    }
}