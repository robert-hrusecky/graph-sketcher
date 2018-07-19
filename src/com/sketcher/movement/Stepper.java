package com.sketcher.movement;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

/**
 * Class for control of the 28byj-48 stepper motor. Provides an abstraction of
 * fractional steps for ease of use. The current version uses half stepping.
 * 
 * @author robert
 *
 */
public class Stepper {

	/**
	 * Delay between each step.
	 */
	public static final long DELAY = 1;
	/**
	 * Number of GPIOs requried to drive the motor
	 */
	public static final int NUM_PINS = 4;
	private static final boolean[][] PIN_MATRIX = { { true, false, false, false }, { true, true, false, false },
			{ false, true, false, false }, { false, true, true, false }, { false, false, true, false },
			{ false, false, true, true }, { false, false, false, true }, { true, false, false, true } };
	private static final double GEAR_RATIO = 25792.0 / 405.0;
	/**
	 * Number of steps in one revolution of the motor.
	 */
	public static final double STEPS_PER_REVOLUTION = PIN_MATRIX.length * 8 * GEAR_RATIO;

	private GpioPinDigitalOutput[] pins;
	private int position;
	private double accumulator;

	/**
	 * Creates a stepper motor object and initializes the gpio pins.
	 * 
	 * @param gpios
	 *            The pins the motor is connected to
	 * @param gpio
	 *            the controller
	 */
	public Stepper(Pin[] gpios, GpioController gpio) {

		if (gpios.length != NUM_PINS) {
			throw new IllegalArgumentException("Incorrect pin quantity.");
		}

		position = 0;
		accumulator = 0.0;
		pins = new GpioPinDigitalOutput[NUM_PINS];
		for (int i = 0; i < pins.length; i++) {
			pins[i] = gpio.provisionDigitalOutputPin(gpios[i]);
			pins[i].setShutdownOptions(true, PinState.LOW);
			pins[i].low();
		}
	}

	/**
	 * Gives the current position of the motor.
	 * 
	 * @return the current position (in steps)
	 */
	public double getPosition() {
		return position + accumulator;
	}

	/**
	 * Move a fractional number of steps.
	 * 
	 * @param steps
	 *            the number of steps. A negative argument moves in reverse.
	 */
	public void step(double steps) {
		int fullSteps = (int) steps;
		accumulator += steps % 1;
		fullSteps += (int) accumulator;
		accumulator %= 1;
		step(fullSteps);
	}

	/**
	 * Move an integer number of steps.
	 * 
	 * @param steps
	 *            the number of steps. A negative argument moves in reverse.
	 */
	public void step(int steps) {
		if (steps > 0) {
			stepForward(steps);
		} else {
			stepBackward(-steps);
		}
	}

	/**
	 * Move forward a number of steps.
	 * 
	 * @param steps
	 *            the number of steps
	 */
	private void stepForward(int steps) {
		int newPosition = position + steps;
		if (steps != 0) {
			position++;
			setPins();
		}
		while (position < newPosition) {
			delay();
			position++;
			setPins();
		}
	}

	/**
	 * Move backward a number of steps
	 * 
	 * @param steps
	 *            the number of steps
	 */
	private void stepBackward(int steps) {
		int newPosition = position - steps;
		if (steps != 0) {
			position--;
			setPins();
		}
		while (position > newPosition) {
			delay();
			position--;
			setPins();
		}
	}

	/**
	 * Set the pins at the current posiiton
	 */
	private void setPins() {
		int row = Math.floorMod(position, PIN_MATRIX.length);
		for (int i = 0; i < pins.length; i++) {
			pins[i].setState(PIN_MATRIX[row][i]);
		}
	}

	/**
	 * Delay for the standard delay time stored in {@link #DELAY}
	 */
	public static void delay() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reset the position to zero. Do not move.
	 */
	public void zero() {
		position = 0;
		accumulator = 0.0;
	}

}
