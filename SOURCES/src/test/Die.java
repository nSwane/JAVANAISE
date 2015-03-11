package test;

import java.util.TimerTask;

public class Die extends TimerTask {

	public Die(){
		super();
		
	}

	@Override
	public void run() {
		TestStarter.stopCoordinator(false);
		
	}

}
