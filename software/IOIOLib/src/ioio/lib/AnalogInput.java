package ioio.lib;

import android.util.Log;

/**
 * Represent and manage analog input pins on the IOIO.
 * 
 * @author arshan
 */
public class AnalogInput extends IOIOPin implements IOIOPacketListener {

	IOIOImplPic24f ioio;
	int value = 0;
	
	boolean active = false;
	private int reportPin = 0;
	
	public AnalogInput(IOIOImplPic24f ioio, int pin) {
		super(pin);
		this.ioio = ioio;
		init();
		ioio.registerListener(this);
	}
	
	private void init() {
		ioio.queuePacket(new IOIOPacket(
				Constants.SET_ANALOG_INPUT,
				new byte[]{(byte)pin}
		));
	}

	// TODO(TF): decide on units, mV? let the user set them?
	public float read() {
		return value;
	}

	public void handlePacket(IOIOPacket packet) {
		switch (packet.message){		
		case Constants.SET_ANALOG_INPUT:
			if (packet.payload[0] == pin) {
				active = true;
			}
			break;
			
		case Constants.REPORT_ANALOG_FORMAT:
			// Record where in the paylod my pin number is for future use.
			for (int x = 1; x < packet.payload.length; x++) {
				if (packet.payload[x] == pin) {
					reportPin = x-1;
					break;
				}
			}
			break;
			
		case Constants.REPORT_ANALOG_STATUS:
			// TODO(arshan): make these class vars.
			int offset = (int)(reportPin / 4) * 5;
			int rem  = reportPin % 4;
			// MSB
			value = packet.payload[offset+rem] << 2;
			// LSB
			value |= (int)(packet.payload[offset] & (0x3 << (rem*2))) >> (rem * 2);
			break;
		}
	}
}
