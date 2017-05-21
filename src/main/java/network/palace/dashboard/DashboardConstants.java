package network.palace.dashboard;

import network.palace.dashboard.handlers.ChatColor;

import java.util.regex.Pattern;

/**
 * @author Innectic
 * @since 5/17/2017
 *
 * The constant variables
 */
public class DashboardConstants {

    public static final String SWEAR_RESPONSE = ChatColor.RED + "Please do not swear!";
    public static final String SWEAR_WARNING = "Please keep chat appropriate";
    public static final String LINK_WARNING = ChatColor.RED + "Please do not attempt to advertise or share links.";
    public static final String EXCESSIVE_CAPS = ChatColor.RED + "Please do not use excessive capitals in your messages.";
    public static final String SPAM_WARNING = ChatColor.RED + "Please do not spam chat with excessive amounts of characters.";

    public static final String MUTED_CHAT = ChatColor.RED + "Chat is currently muted!";
    public static final String CHAT_DELAY = ChatColor.RED + "You must wait <TIME> seconds before chatting!";
    public static final String NEW_GUEST = ChatColor.RED + "New guests must be on the server for at least 15 minutes before talking in chat." +
            " Learn more at palnet.us.rules";
    public static final String MESSAGE_REPEAT = ChatColor.RED + "Please do not repeat the same message!";
    public static final String SKYPE_INFORMATION = ChatColor.RED + "Please do not ask for Skype information!";
    public static final String MUTED_PLAYER = ChatColor.RED + "You are muted! You will be unmuted in <TIME>.";
    public static final String MUTE_REASON = ChatColor.RED + "Mute Reason: <REASON>";

    public static final Pattern LINK_PATTERN = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?)|(([0-9a-z:/]+(\\.|\\(dot\\)\\(\\.\\" +
            ")))+(aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|network|org|pro|tel|travel|ac|ad|ae|" +
            "af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc" +
            "|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx|cy|de|dj|dk|dm|do|dz|ec|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|f" +
            "r|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|" +
            "je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml" +
            "|mo|mp|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|nom|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|" +
            "pt|pw|py|qa|re|ra|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sk|sl|sm|sn|so|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj" +
            "|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw|arpa)(:[0-" +
            "9]+)?((/([~0-9a-zA-Z#+%@./_-]+))?(/[0-9a-zA-Z+%@/&\\[\\];=_-]+)?)?)\\b");

}
