package com.sketcher.movement;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class Stepper {
	
	public static final long DELAY = 3;

	public static final int STEPS_PER_REV = 512;
	public static final int NUM_PINS = 4;
	private static final boolean[][] PIN_MATRIX = {
			{ true, true, false, false },
			{ false, true, true, false },
			{ false, false, true, true },
			{ true, false, false, true },
	};

	private GpioPinDigitalOutput[] pins;
	private int position;
	private double accumulator;

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
	
	public double getPosition() {
		return position + accumulator;
	}
	
	public void round() {
		step((int) Math.round(accumulator));
		accumulator = 0.0;
	}

	public void step(double steps) {
		int fullSteps = (int) steps;
		accumulator += steps % 1;
		fullSteps += (int) accumulator;
		accumulator %= 1;
		step(fullSteps);
	}

	public void step(int steps) {
		if (steps > 0) {
			stepForward(steps);
		} else {
			stepBackward(-steps);
		}
	}
	
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
	
	private void setPins() {
		int row = Math.floorMod(position, pins.length);
		for (int i = 0; i < pins.length; i++) {
			pins[i].setState(PIN_MATRIX[row][i]);
		}
	}
	
	public static void delay() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
