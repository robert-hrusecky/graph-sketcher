package com.sketcher.movement;

public class StepperGroup {
	
	private Stepper stepper1, stepper2;

	public StepperGroup(Stepper stepper1, Stepper stepper2) {
		this.stepper1 = stepper1;
		this.stepper2 = stepper2;
	}

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
}
