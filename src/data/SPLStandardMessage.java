package data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SPLStandardMessage implements Serializable {

    private static final long serialVersionUID = 2204681477211322628L;

    /**
     * Some constants from the C-structure.
     */
    public static final String SPL_STANDARD_MESSAGE_STRUCT_HEADER = "SPL ";
    public static final byte SPL_STANDARD_MESSAGE_STRUCT_VERSION = 6;
    public static final short SPL_STANDARD_MESSAGE_DATA_SIZE = 780;
    public static final byte SPL_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS = 5;
    public static final int SIZE = 4 // header size
            + 1 // byte for the version
            + 1 // player number
            + 1 // team number
            + 1 // fallen
            + 12 // pose
            + 8 // walking target
            + 8 // shooting target
            + 4 // ball age
            + 8 // ball position
            + 8 // ball velocity
            + SPL_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS // suggestions
            + 1 // intention
            + 2 // average walk speed
            + 2 // maximum kick distance
            + 1 // confidence of current position
            + 1 // confidence of current side
            + 2 // actual size of data
            + SPL_STANDARD_MESSAGE_DATA_SIZE; // data

    public String header;   // header to identify the structure
    public byte version;    // version of the data structure
    public byte playerNum;  // 1-5
    public byte teamNum;    // the number of the team (as provided by the organizers)
    public boolean fallen;  // whether the robot is fallen

    // position and orientation of robot
    // coordinates in millimeters
    // 0,0 is in center of field
    // +ve x-axis points towards the goal we are attempting to score on
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    // angle in radians, 0 along the +x axis, increasing counter clockwise
    public float[] pose;      // x,y,theta

    // the robot's target position on the field
    // the coordinate system is the same as for the pose
    // if the robot does not have any target, this attribute should be set to the robot's position
    public float[] walkingTo;

    // the target position of the next shot (either pass or goal shot)
    // the coordinate system is the same as for the pose
    // if the robot does not intend to shoot, this attribute should be set to the robot's position
    public float[] shootingTo;

    // Ball information
    public float ballAge;        // seconds since this robot last saw the ball. -1.f if we haven't seen it

    // position of ball relative to the robot
    // coordinates in millimeters
    // 0,0 is in centre of the robot
    // +ve x-axis points forward from the robot
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    public float[] ball;

    // velocity of the ball (same coordinate system as above)
    // the unit is millimeters per second
    public float[] ballVel;

    // describes what - in the robot's opinion - the teammates should do:
    public enum Suggestion {

        NOTHING, // 0 - nothing particular (default)
        KEEPER, // 1 - play keeper
        DEFENSE, // 2 - support defense
        OFFENSE, // 3 - support the ball
        PLAY_BALL // 4 - play the ball
    }
    public Suggestion[] suggestion;

    // describes what the robot intends to do
    public enum Intention {

        NOTHING, // 0 - nothing particular (default)
        KEEPER, // 1 - wants to be keeper
        DEFENSE, // 2 - wants to play defense
        PLAY_BALL, // 3 - wants to play the ball
        LOST       // 4 - robot is lost
    }
    public Intention intention;

    // the average speed that the robot has, for instance, when walking towards the ball
    // the unit is mm/s
    // the idea of this value is to roughly represent the robot's walking skill
    // it has to be set once at the beginning of the game and remains fixed
    public short averageWalkSpeed;

    // the maximum distance that the ball rolls after a strong kick by the robot
    // the unit is mm
    // the idea of this value is to roughly represent the robot's kicking skill
    // it has to be set once at the beginning of the game and remains fixed
    public short maxKickDistance;

    // describes the current confidence of a robot about its self-location,
    // the unit is percent [0,..100]
    // the value should be updated in the course of the game
    public byte currentPositionConfidence;

    // describes the current confidence of a robot about playing in the right direction,
    // the unit is percent [0,..100]
    // the value should be updated in the course of the game
    public byte currentSideConfidence;

    // buffer for arbitrary data
    public byte[] data;

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(header.getBytes());
        buffer.put(version);
        buffer.put(playerNum);
        buffer.put(teamNum);
        buffer.put(fallen ? (byte) 1 : (byte) 0);
        buffer.putFloat(pose[0]);
        buffer.putFloat(pose[1]);
        buffer.putFloat(pose[2]);
        buffer.putFloat(walkingTo[0]);
        buffer.putFloat(walkingTo[1]);
        buffer.putFloat(shootingTo[0]);
        buffer.putFloat(shootingTo[1]);
        buffer.putFloat(ballAge);
        buffer.putFloat(ball[0]);
        buffer.putFloat(ball[1]);
        buffer.putFloat(ballVel[0]);
        buffer.putFloat(ballVel[1]);
        for (final Suggestion s : suggestion) {
            buffer.put((byte) s.ordinal());
        }
        buffer.put((byte) intention.ordinal());
        buffer.putShort(averageWalkSpeed);
        buffer.putShort(maxKickDistance);
        buffer.put(currentPositionConfidence);
        buffer.put(currentSideConfidence);
        buffer.putShort((short) data.length);
        buffer.put(data);

        return buffer.array();
    }

    public boolean fromByteArray(ByteBuffer buffer) {
        try {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            byte[] header = new byte[4];
            buffer.get(header);
            this.header = new String(header);
            if (!this.header.equals(SPL_STANDARD_MESSAGE_STRUCT_HEADER)) {
                return false;
            } else {
                version = buffer.get();
                if (version != SPL_STANDARD_MESSAGE_STRUCT_VERSION) {
                    return false;
                } else {
                    playerNum = buffer.get();
                    if (playerNum < 1 || playerNum > 5) {
                System.out.println("test1");
                        return false;
                    }

                    teamNum = buffer.get();

                    switch (buffer.get()) {
                        case 0:
                            fallen = false;
                            break;
                        case 1:
                            fallen = true;
                            break;
                        default:
                System.out.println("test2");
                            return false;
                    }

                    pose = new float[3];
                    pose[0] = buffer.getFloat();
                    pose[1] = buffer.getFloat();
                    pose[2] = buffer.getFloat();

                    walkingTo = new float[2];
                    walkingTo[0] = buffer.getFloat();
                    walkingTo[1] = buffer.getFloat();

                    shootingTo = new float[2];
                    shootingTo[0] = buffer.getFloat();
                    shootingTo[1] = buffer.getFloat();

                    ballAge = buffer.getFloat();

                    ball = new float[2];
                    ball[0] = buffer.getFloat();
                    ball[1] = buffer.getFloat();

                    ballVel = new float[2];
                    ballVel[0] = buffer.getFloat();
                    ballVel[1] = buffer.getFloat();

                    this.suggestion = new Suggestion[SPL_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS];
                    for (int i = 0; i < SPL_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS; i++) {
                        final int s = (int) buffer.get();
                        if (s >= Suggestion.values().length) {
                System.out.println("test3");
                            return false;
                        }
                        this.suggestion[i] = Suggestion.values()[s];
                    }

                    int intention = (int) buffer.get();
                    if (intention >= Intention.values().length) {
                System.out.println("test4");
                        return false;
                    }
                    this.intention = Intention.values()[intention];

                    averageWalkSpeed = buffer.getShort();
                    maxKickDistance = buffer.getShort();

                    currentPositionConfidence = buffer.get();
                    if (currentPositionConfidence < 0 || currentPositionConfidence > 100) {
                System.out.println("test5");
                        return false;
                    }
                    currentSideConfidence = buffer.get();
                    if (currentSideConfidence < 0 || currentSideConfidence > 100) {
                System.out.println(currentSideConfidence);
                        return false;
                    }

                    short numOfDataBytes = buffer.getShort();
                    if (numOfDataBytes > SPL_STANDARD_MESSAGE_DATA_SIZE) {
                System.out.println("test7");
                        return false;
                    }
                    data = new byte[numOfDataBytes];
                    buffer.get(data, 0, numOfDataBytes);

                    return true;
                }
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}
