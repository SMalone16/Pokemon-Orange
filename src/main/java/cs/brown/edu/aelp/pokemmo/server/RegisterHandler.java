package cs.brown.edu.aelp.pokemmo.server;

import com.google.gson.Gson;
import cs.brown.edu.aelp.pokemmo.data.DataSource.AuthException;
import cs.brown.edu.aelp.pokemmo.data.authentication.User;
import cs.brown.edu.aelp.pokemmo.data.authentication.UserManager;
import java.util.HashMap;
import java.util.Map;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {

  private final Gson GSON = new Gson();

  @Override
  public Object handle(Request req, Response res) throws Exception {
    QueryParamsMap qm = req.queryMap();
    String user = qm.value("username");
    String pass = qm.value("password");
    String code = qm.value("classCode");
    // No student email addresses are collected. Preserve the existing unique DB field.
    String email = user == null ? null : user + "@players.invalid";
    String species = qm.value("species");
    String nickname = qm.value("nickname");
    Map<String, Object> vars = new HashMap<>();
    try {
      String expectedCode = System.getenv("CLASS_CODE");
      if (expectedCode != null && !expectedCode.equals(code)) {
        throw new AuthException("Incorrect class code.");
      }
      validateInput(user, pass, email, species, nickname);
      User u = UserManager.register(user, pass, email, species, nickname);
      vars.put("success", true);
      vars.put("token", u.getToken());
      vars.put("id", u.getId());
    } catch (AuthException e) {
      vars.put("success", false);
      vars.put("message", e.getMessage());
    }
    return GSON.toJson(vars);
  }

  public boolean validateInput(String name, String pass, String email,
      String species, String nickname) throws AuthException {
    if (name == null || pass == null || email == null
        || species == null || nickname == null) {
      throw new AuthException("Complete all fields.");
    }
    if (!name.matches("[A-Za-z0-9]{4,12}")) {
      throw new AuthException("Username: 4-12 letters or numbers.");
    }
    if (name.length() <= 3) {
      throw new AuthException("Username too short.");
    }
    if (name.length() >= 13) {
      throw new AuthException("Username too long.");
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
        throw new AuthException("Username must be alpha-numeric.");
      }
    }

    // The email field is a server-generated database placeholder, never a
    // student-provided email address. Accept the reserved .invalid domain.
    if (!email.equals(name + "@players.invalid")) {
      throw new AuthException("Invalid player account.");
    }

    // if (!pass.matches(
    // "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}"))
    // {
    // throw new AuthException(
    // "Password must have 8 chars, a digit, symbol, and both cases.");
    // }

    if (pass.length() > 128) {
      throw new AuthException("Password is too long.");
    }
    if (pass.length() < 8) {
      throw new AuthException("Password must be at least 8 characters.");
    }

    if (!(species.equals("bulbasaur") || species.equals("squirtle")
        || species.equals("charmander"))) {
      throw new AuthException("Invalid species selected.");
    }

    if (nickname.length() <= 3) {
      throw new AuthException("Pokemon nickname too short.");
    }
    if (!nickname.matches("[A-Za-z0-9]{4,12}")) {
      throw new AuthException("Pokemon nickname: 4-12 letters or numbers.");
    }
    if (nickname.length() >= 13) {
      throw new AuthException("Pokemon nickname too long.");
    }

    return true;

  }

}
