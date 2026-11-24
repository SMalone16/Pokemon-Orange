package cs.brown.edu.aelp.pokemmo.server;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class RegisterHandlerTest {
  @Test
  public void acceptsSyntheticEmailForClassroomRegistration() throws Exception {
    assertTrue(new RegisterHandler().validateInput(
        "TestPlayer", "gamepass123", "TestPlayer@players.invalid",
        "squirtle", "BlueBoi"));
  }
}
