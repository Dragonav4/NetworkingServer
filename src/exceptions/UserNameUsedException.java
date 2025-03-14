package exceptions;

public class UserNameUsedException extends Exception {
    public UserNameUsedException() {
        super("This user name already taken, please use another one");
    }
}
