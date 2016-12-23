package mc.fhooe.at.wyfiles.communication;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * @author Martin Macheiner
 *         Date: 23.12.2016.
 */

@JsonObject
public class WyfilesMessage {

    @JsonField
    public String filename;

    @JsonField
    public byte[] payload;

    public WyfilesMessage() {
        this("", null);
    }

    public WyfilesMessage(String filename, byte[] payload) {
        this.filename = filename;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Filename: " + filename + "\nPayload length: " + payload.length;
    }
}
