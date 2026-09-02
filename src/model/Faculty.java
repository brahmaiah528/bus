package model;

/**
 * Represents a Faculty passenger.
 * Subclass extending Passenger, implementing customized fee calculation with staff benefit (10% discount).
 */
public class Faculty extends Passenger {
    private static final long serialVersionUID = 1L;

    // Standard fixed tier rates
    public static final double FACULTY_MONTHLY_BASE = 800.0;
    public static final double FACULTY_SEMESTER_BASE = 4000.0;
    public static final double FACULTY_DISCOUNT_PERCENT = 10.0; // 10% discount

    public Faculty() {
        super();
        setPassengerType("FACULTY");
    }

    public Faculty(String passengerId, String name, String phone, String email, int validityDays) {
        super(passengerId, name, phone, email, "FACULTY", validityDays);
    }

    /**
     * Polymorphic implementation of pass fee calculation for Faculty:
     * - Monthly: (Base Route Fare * 25 days or default ₹800 base) with 10% discount applied.
     * - Semester: (Base Route Fare * 110 days or default ₹4000 base) with 10% discount applied.
     */
    @Override
    public double calculatePassFee(double baseFare, String passDuration) {
        double nominalFee;
        if ("SEMESTER".equalsIgnoreCase(passDuration)) {
            nominalFee = (baseFare > 0) ? (baseFare * 110) : FACULTY_SEMESTER_BASE;
        } else {
            // Monthly
            nominalFee = (baseFare > 0) ? (baseFare * 25) : FACULTY_MONTHLY_BASE;
        }

        // Apply 10% Faculty Institutional Benefit
        double finalFee = nominalFee * (1.0 - (FACULTY_DISCOUNT_PERCENT / 100.0));
        return Math.round(finalFee * 100.0) / 100.0;
    }
}
