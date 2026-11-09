package goblinbob.mobends.core.supporters;

import com.google.gson.annotations.SerializedName;

/**
 * Where an accessory's part is worn: the API's key for it, and the bone of the player's model
 * definition it follows (1.12's {@code BindPoint}). An unknown key reads as null.
 */
public enum BindPoint {
    @SerializedName("head") HEAD("head"),
    @SerializedName("body") BODY("body"),
    @SerializedName("leftArm") LEFT_ARM("leftArm"),
    @SerializedName("leftForearm") LEFT_FOREARM("leftForeArm"),
    @SerializedName("rightArm") RIGHT_ARM("rightArm"),
    @SerializedName("rightForearm") RIGHT_FOREARM("rightForeArm"),
    @SerializedName("leftThigh") LEFT_THIGH("leftLeg"),
    @SerializedName("leftShin") LEFT_SHIN("leftForeLeg"),
    @SerializedName("rightThigh") RIGHT_THIGH("rightLeg"),
    @SerializedName("rightShin") RIGHT_SHIN("rightForeLeg");

    private final String bone;

    BindPoint(String bone) {
        this.bone = bone;
    }

    /** The bone (or segment) of the player's definition the part follows. */
    public String bone() {
        return bone;
    }
}
