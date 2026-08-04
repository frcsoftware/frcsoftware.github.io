package first.robot;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.vision.apriltag.AprilTagFieldLayout;
import org.wpilib.vision.apriltag.AprilTagFields;

public class FieldConstants {

    public static final double fieldWidth = AprilTagFieldLayout.loadField(AprilTagFields.k2025ReefscapeWelded).getFieldWidth();
    public static final double fieldLength = AprilTagFieldLayout.loadField(AprilTagFields.k2025ReefscapeWelded).getFieldLength();

    public static final Translation2d BLUE_REEF_CENTER = new Translation2d(4.263, 0);
    public static final Translation2d RED_REEF_CENTER = mirrorX(BLUE_REEF_CENTER);







    public static Translation2d mirrorX(Translation2d translation) {
        return new Translation2d(-translation.getX(), translation.getY());
    }

    public static Pose2d mirrorX(Pose2d pose) {
        return new Pose2d(mirrorX(pose.getTranslation()), Rotation2d.kCCW_Pi_2.minus(pose.getRotation()));
    }

    public static Translation2d mirrorY(Translation2d translation) {
        return new Translation2d(translation.getX(), -translation.getY());
    }

    public static Pose2d mirrorY(Pose2d pose) {
        return new Pose2d(mirrorY(pose.getTranslation()), pose.getRotation().unaryMinus());
    }

    public static Translation2d rotateAboutCenter(Translation2d translation) {
        return mirrorX(mirrorY(translation));
    }

    public static Pose2d rotateAboutCenter(Pose2d pose) {
        return mirrorX(mirrorY(pose));
    }
}
