public class BcryptGen2 {
  public static void main(String[] args) {
    for (String arg : args) {
      System.out.println(arg + "=" + org.springframework.security.crypto.bcrypt.BCrypt.hashpw(arg, org.springframework.security.crypto.bcrypt.BCrypt.gensalt()));
    }
  }
}
