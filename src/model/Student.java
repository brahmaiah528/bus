package model;

/**
 * Represents a Student passenger.
 * Subclass extending Passenger, implementing customized fee calculation with student subsidy (20% discount).
 */
public class Student extends Passenger {
    private static final long serialVersionUID = 1L;

    // Standard fixed tier rates
    public static final double STUDENT_MONTHLY_BASE = 500.0;
    public static final double STUDENT_SEMESTER_BASE = 2500.0;
    public static final double STUDENT_DISCOUNT_PERCENT = 20.0; // 20% discount

    public Student() {
        super();
        setPassengerType("STUDENT");
    }

    public Student(String passengerId, String name, String phone, String email, int validityDays) {
        super(passengerId, name, phone, email, "STUDENT", validityDays);
    }

    /**
     * Polymorphic implementation of pass fee calculation for Students:
     * - Takes base route fare into account.
     * - Monthly: (Base Route Fare * 20 days or default ₹500 base) with 20% discount applied.
     * - Semester: (Base Route Fare * 100 days or default ₹2500 base) with 20% discount applied.
     */
    @Override
    public double calculatePassFee(double baseFare, String passDuration) {
        double nominalFee;
        if ("SEMESTER".equalsIgnoreCase(passDuration)) {
            // Either calculate from route fare or use baseline tiered rate
            nominalFee = (baseFare > 0) ? (baseFare * 90) : STUDENT_SEMESTER_BASE;
        } else {
            // Default Monthly
            nominalFee = (baseFare > 0) ? (baseFare * 22) : STUDENT_MONTHLY_BASE;
        }

        // Apply 20% Student Educational Subsidy
        double finalFee = nominalFee * (1.0 - (STUDENT_DISCOUNT_PERCENT / 100.0));
        return Math.round(finalFee * 100.0) / 100.0;
    }
}
