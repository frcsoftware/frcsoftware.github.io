package first.robot.simulation;

import static org.wpilib.units.Units.Inches;
import static org.wpilib.units.Units.KilogramSquareMeters;
import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.system.DCMotor;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StructPublisher;
import org.wpilib.simulation.DifferentialDrivetrainSim;
import org.wpilib.simulation.OnboardIMUSim;

import com.ctre.phoenix6.hardware.TalonFX;

public class DrivetrainSim {
    
    private final TalonFX leftTalon;
    private final TalonFX rightTalon;

    private final double kGearRatio = 10.71;
    private final double kWheelRadiusMeters = Inches.of(3.0).in(Meters);
    private static final double kBusVoltage = 12.0;

    private final DifferentialDrivetrainSim driveSim = 
        new DifferentialDrivetrainSim(
            DCMotor.getKrakenX60(2), // 2 Kraken Moters on each side of the drivetrain
            kGearRatio, // Gear ratio between the drive motor and drive wheel
            KilogramSquareMeters.of(2.1).magnitude(), // MOI of the robot (measured from CAD model)
            Kilograms.of(26.5).magnitude(), // Mass of the robot
            kWheelRadiusMeters, // Radius of the drive wheels
            Inches.of(21.5).in(Meters), // Distance between the left and right wheels
            null);
    
    private final StructPublisher<Pose2d> simPosePublisher = 
            NetworkTableInstance.getDefault()
                .getStructTopic("SimPose", Pose2d.struct)
                .publish();
    
    private final DoublePublisher leftPositionPub = 
            NetworkTableInstance.getDefault()
                .getDoubleTopic("DrivetrainSim/LeftPositionMeters")
                .publish();

    private final DoublePublisher rightPositionPub = 
            NetworkTableInstance.getDefault()
            .getDoubleTopic("DrivetrainSim/RightPositionMeters")
            .publish();
    
    private final DoublePublisher leftVelocityPub =
            NetworkTableInstance.getDefault()
            .getDoubleTopic("DrivetrainSim/LeftVelocityMPS")
            .publish();

    private final DoublePublisher rightVelocityPub =
            NetworkTableInstance.getDefault()
            .getDoubleTopic("DrivetrainSim/RightVelocityMPS")
            .publish();
    
    /**
     * 
     * @param leftTalon the left-side TalonFX motor controller
     * @param rightTalon the left-side TalonFX motor controller
     * 
     */
    public DrivetrainSim(TalonFX leftTalon, TalonFX rightTalon) {
        this.leftTalon = leftTalon;
        this.rightTalon = rightTalon;
    }


    public void periodic() {
        double leftMotorVoltage = leftTalon.getThrottle() * kBusVoltage;
        double rightMotorVoltage = rightTalon.getThrottle() * kBusVoltage;

        driveSim.setInputs(leftMotorVoltage, rightMotorVoltage);
        driveSim.update(0.02);

        OnboardIMUSim.setYaw(driveSim.getHeading().getRadians());

        simPosePublisher.set(driveSim.getPose());
        leftPositionPub.set(driveSim.getLeftPosition());
        rightPositionPub.set(driveSim.getRightPosition());
        leftVelocityPub.set(driveSim.getLeftVelocity());
        rightVelocityPub.set(driveSim.getRightVelocity());
    }

}
