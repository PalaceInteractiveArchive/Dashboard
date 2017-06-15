package network.palace.dashboard.utils.chat;

import java.io.Serializable;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public interface StringDistance extends Serializable {

    double distance(String first, String second);
}
