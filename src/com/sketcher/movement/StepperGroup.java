package com.sketcher.movement;

/**
 * A clas for controlling two {@link Stepper} objects at the same time.
 * 
 * @author robert
 *
 */
public class StepperGroup {

	private Stepper stepper1, stepper2;

	/**
	 * Sets up this group.
	 * 
	 * @param stepper1
	 *            The first stepper
	 * @param stepper2
	 *            The second stepper
	 */
	public StepperGroup(Stepper stepper1, Stepper stepper2) {
		this.stepper1 = stepper1;
		this.stepper2 = stepper2;
	}

	/**
	 * Moves both motors a number of steps. The motors will start and stop at the
	 * same time.
	 * 
	 * @param steps1
	 *            Steps for motor 1.
	 * @param steps2
	 *            Steps for motor 2.
	 */
	public void step(double steps1, double steps2) {
		int numSteps = (int) Math.max(Math.abs(steps1), Math.abs(steps2));
		double increment1 = steps1 / (int) numSteps;
		double increment2 = steps2 / (int) numSteps;

		for (int i = 0; i < numSteps; i++) {
			stepper1.step(increment1);
			stepper2.step(increment2);
			Stepper.delay();
		}
	}

	/**
	 * Zeros both of the stepper motors.
	 */
	public void zero() {
		stepper1.zero();
		stepper2.zero();
	}
}
