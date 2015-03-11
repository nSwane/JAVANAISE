package test;

import java.util.TimerTask;

public class Revive extends TimerTask{

	@Override
	public void run() {
		TestStarter.startCoordinator();
	}

}