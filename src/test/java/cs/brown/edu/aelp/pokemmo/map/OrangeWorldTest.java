package cs.brown.edu.aelp.pokemmo.map;

import org.junit.Test;
import static org.junit.Assert.*;

public class OrangeWorldTest {
  @Test public void walksAroundObstacle() {
    Chunk c = new Chunk(1, 5, 3, "test");
    c.setBlocked(1,2,true);
    int[] step=OrangeWorld.nextStep(c,1,1,1,3);
    assertEquals(1,Math.abs(step[0]-1)+Math.abs(step[1]-1));
    assertTrue(c.isWalkable(step[0],step[1]));
    assertNotEquals(2,step[1]);
    for(int i=0;i<5;i++) step=OrangeWorld.nextStep(c,step[0],step[1],1,3);
    assertArrayEquals(new int[]{1,3},step);
  }
  @Test public void unreachableAndOutsideStayStill() {
    Chunk c = new Chunk(1, 3, 3, "test");
    for(int row=0;row<3;row++) c.setBlocked(row,1,true);
    assertArrayEquals(new int[]{1,0},OrangeWorld.nextStep(c,1,0,1,2));
    assertArrayEquals(new int[]{1,0},OrangeWorld.nextStep(c,1,0,-1,2));
    assertFalse(c.isWalkable(3,0));
  }
}
