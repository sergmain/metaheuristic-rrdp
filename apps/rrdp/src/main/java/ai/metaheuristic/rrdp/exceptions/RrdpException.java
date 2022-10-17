package ai.metaheuristic.rrdp.exceptions;

/**
 * @author Sergio Lissner
 * Date: 10/16/2022
 * Time: 9:10 PM
 */
public class RrdpException extends RuntimeException {
    public RrdpException() {
    }

    public RrdpException(String message) {
        super(message);
    }

    public RrdpException(String message, Throwable cause) {
        super(message, cause);
    }
}
