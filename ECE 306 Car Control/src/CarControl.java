import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

/**
 * 
 */

/**
 * @author Joshmosh
 *
 */
public class CarControl {
	static DataOutputStream stream;
	static final String NCSU_IP = "10.155.46.192";
	static final int PORT = 5125;
	static final double DEADZONE = .2;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		//Client should start the communication between the client and server.
		CarControl client = new CarControl();
		Socket connection = new Socket(NCSU_IP, PORT); //Network Address as a string and port number as integer
		stream = new DataOutputStream(connection.getOutputStream());
		ControllerManager controllers = new ControllerManager();
		controllers.initSDLGamepad();
		
		launchStayConnectedThread(client);
		
		client.controllerInputHandle(controllers);
		
		controllers.quitSDLGamepad();
	}

	private void controllerInputHandle(ControllerManager controllers) throws IOException {
		String message = "";
		boolean buttonPressed = false;
		while (true) {
			buttonPressed = false;
			ControllerState currState = controllers.getState(0);
			if (currState.guide || !currState.isConnected) {
				System.out.println("Controller Not Connected");
				break;
			}
			
			if(currState.a) {
				message = CarCommands.CALIBRATE_SPEED;
				buttonPressed = true;
			}
			if(currState.dpadLeft) {
				message = CarCommands.TRIM_LEFT;
				buttonPressed = true;
			}
			if(currState.dpadRight) {
				message = CarCommands.TRIM_RIGHT;
				buttonPressed = true;
			}
			if(currState.dpadUp) {
				message = CarCommands.TRIM_FORWARD;
				buttonPressed = true;
			}
			if(currState.dpadDown) {
				message = CarCommands.TRIM_REVERSE;
				buttonPressed = true;
			}
			if(currState.b) {
				message = CarCommands.EXIT;
				buttonPressed = true;
			}
			if(currState.x) {
				message = CarCommands.FOLLOW;
				buttonPressed = true;
			}
			if(currState.rightTrigger > .2) {
				message = CarCommands.FORWARD;
				buttonPressed = true;
			}
			if(currState.leftTrigger > .2) {
				message = CarCommands.REVERSE;
				buttonPressed = true;
			}
			if(currState.rightTrigger > .8) {
				message = CarCommands.FORWARD_FAST;
				buttonPressed = true;
			}
			if(currState.leftStickX < -DEADZONE) {
				message = CarCommands.LEFT;
				buttonPressed = true;
			}
			if(currState.leftStickX > DEADZONE) {
				message = CarCommands.RIGHT;
				buttonPressed = true;
			}
			if(buttonPressed) {
				System.out.println("Message Sent: " + message);
				sendMessage(message);
			}
			try {
				TimeUnit.MILLISECONDS.sleep(280);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void launchStayConnectedThread(CarControl client) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						TimeUnit.MILLISECONDS.sleep(750);
						client.sendMessage("%\n");
					} catch (InterruptedException | IOException e) {
						System.out.println("dissassoiated");
						break;
					}
				}
			}
		}) ;
		
		t.start();
	}
	
	private synchronized void sendMessage(String string) throws IOException {
		stream.writeUTF(string);
	}

}
