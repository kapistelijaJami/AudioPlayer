package dataanalysis;

import java.util.function.Consumer;

public class ThreadExecuter extends Thread {
	private Consumer<Object> method;
	
	public ThreadExecuter(Consumer<Object> method) {
		this.method = method;
	}
	
	@Override
	public void run() {
		method.accept(null);
	}
}
