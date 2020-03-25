package network.palace.dashboard.vote;

import com.google.gson.JsonObject;

/**
 * Created by Marc on 1/15/17.
 */
public class Vote {

    /**
     * The name of the vote service.
     */
    private String serviceName;

    /**
     * The username of the voter.
     */
    private String username;

    /**
     * The address of the voter.
     */
    private String address;

    /**
     * The date and time of the vote.
     */
    private String timeStamp;

    @Deprecated
    public Vote() {
    }

    public Vote(String serviceName, String username, String address, String timeStamp) {
        this.serviceName = serviceName;
        this.username = username;
        this.address = address;
        this.timeStamp = timeStamp;
    }

    private static String getTimestamp(JsonObject object) {
        try {
            return Long.toString(object.get("timestamp").getAsLong());
        } catch (Exception e) {
            return object.get("timestamp").getAsString();
        }
    }

    public Vote(JsonObject jsonObject) {
        this(jsonObject.get("serviceName").getAsString(), jsonObject.get("username").getAsString(),
                jsonObject.get("address").getAsString(), getTimestamp(jsonObject));
    }

    @Override
    public String toString() {
        return "Vote (from:" + serviceName + " username:" + username
                + " address:" + address + " timeStamp:" + timeStamp + ")";
    }

    /**
     * Sets the serviceName.
     *
     * @param serviceName The new serviceName
     */
    @Deprecated
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Gets the serviceName.
     *
     * @return The serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the username.
     *
     * @param username The new username
     */
    @Deprecated
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the address.
     *
     * @param address The new address
     */
    @Deprecated
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the address.
     *
     * @return The address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the time stamp.
     *
     * @param timeStamp The new time stamp
     */
    @Deprecated
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Gets the time stamp.
     *
     * @return The time stamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    public JsonObject serialize() {
        JsonObject ret = new JsonObject();
        ret.addProperty("serviceName", serviceName);
        ret.addProperty("username", username);
        ret.addProperty("address", address);
        ret.addProperty("timestamp", timeStamp);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vote vote = (Vote) o;

        return serviceName.equals(vote.serviceName) && username.equals(vote.username) &&
                address.equals(vote.address) && timeStamp.equals(vote.timeStamp);

    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + timeStamp.hashCode();
        return result;
    }
}