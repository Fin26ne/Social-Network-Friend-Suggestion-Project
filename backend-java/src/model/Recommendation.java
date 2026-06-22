package model;

import org.json.JSONObject;

public class Recommendation implements Comparable<Recommendation> {
    private User user;
    private int mutualFriends;
    private double jaccardSimilarity;

    public Recommendation(User user, int mutualFriends, double jaccardSimilarity) {
        this.user = user;
        this.mutualFriends = mutualFriends;
        this.jaccardSimilarity = jaccardSimilarity;
    }

    public User getUser() {
        return user;
    }

    public int getMutualFriends() {
        return mutualFriends;
    }

    public double getJaccardSimilarity() {
        return jaccardSimilarity;
    }

    @Override
    public int compareTo(Recommendation other) {
        // High similarity ranks higher
        if (this.jaccardSimilarity > other.jaccardSimilarity) {
            return 1;
        } else if (this.jaccardSimilarity < other.jaccardSimilarity) {
            return -1;
        }

        // If similarity is equal, compare mutual friends count
        if (this.mutualFriends != other.mutualFriends) {
            return Integer.compare(this.mutualFriends, other.mutualFriends);
        }

        // Fallback to alphabetical sorting of user ID for stability (reverse order so lower alphabet gets popped out of min heap last, i.e. ranks higher)
        return other.user.getId().compareTo(this.user.getId());
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("user", user.toJSONObject());
        json.put("mutualCount", mutualFriends);
        json.put("jaccardScore", jaccardSimilarity);
        return json;
    }

    @Override
    public String toString() {
        return String.format("%s (Mutual: %d, Jaccard: %.2f)", user.getName(), mutualFriends, jaccardSimilarity);
    }
}
