package cs.brown.edu.aelp.pokemmo.server;
import com.google.gson.Gson;
import cs.brown.edu.aelp.pokemmo.data.DataSource.AuthException;
import cs.brown.edu.aelp.pokemmo.data.authentication.User;
import cs.brown.edu.aelp.pokemmo.data.authentication.UserManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import spark.Request;
import spark.Response;
import spark.Route;

public class GuestHandler implements Route {
  private static final Gson GSON = new Gson();
  private static final String[] STARTERS = {"bulbasaur", "charmander", "squirtle"};
  @Override
  public Object handle(Request req, Response res) {
    res.type("application/json");
    res.header("Cache-Control", "no-store");
    Map<String, Object> out = new HashMap<>();
    String name = req.queryParams("name");
    try {
      if (name == null || !name.trim().matches("[A-Za-z0-9]{4,12}")) {
        throw new AuthException("Use 4-12 letters or numbers.");
      }
      name = name.trim();
      String resumeId = req.queryParams("id");
      String resumeToken = req.queryParams("token");
      User u;
      if (resumeId != null || resumeToken != null) {
        if (resumeId == null || resumeToken == null || !resumeId.matches("[0-9]{1,10}")) {
          throw new AuthException("Invalid recovery code.");
        }
        try {
          u = UserManager.authenticate(Integer.parseInt(resumeId), resumeToken);
        } catch (AuthException | NumberFormatException e) {
          throw new AuthException("Could not resume this player. Check your recovery code.");
        }
        if (!name.equals(u.getUsername())) throw new AuthException("Recovery code does not match that name.");
      } else {
        String starter = STARTERS[ThreadLocalRandom.current().nextInt(STARTERS.length)];
        u = UserManager.register(name, UUID.randomUUID().toString(),
            name + "@players.invalid", starter, starter);
      }
      out.put("success", true);
      out.put("id", u.getId());
      out.put("token", u.getToken());
    } catch (AuthException e) {
      out.put("success", false);
      out.put("message", e.getMessage() == null ? "Could not join. Try another name." : e.getMessage());
    }
    return GSON.toJson(out);
  }
}
