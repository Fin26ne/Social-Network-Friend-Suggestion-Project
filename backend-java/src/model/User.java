package model;

import org.json.JSONObject;

public class User {
    private String id;
    private String name;
    private String username;
    private String bio;
    private String joinedDate;

    public User(String id, String name, String username, String bio, String joinedDate) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.bio = bio;
        this.joinedDate = joinedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(String joinedDate) {
        this.joinedDate = joinedDate;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("username", username);
        json.put("displayName", name);
        json.put("age", 20); // Default age for UI compatibility
        json.put("bio", bio);
        json.put("joinedDate", joinedDate);
        return json;
    }

    public static User fromJSONObject(JSONObject json) {
        return new User(
            json.getString("id"),
            json.getString("name"),
            json.getString("username"),
            json.optString("bio", ""),
            json.optString("joinedDate", "")
        );
    }

    @Override
    public String toString() {
        return name + " (@" + username + ")";
    }
}
