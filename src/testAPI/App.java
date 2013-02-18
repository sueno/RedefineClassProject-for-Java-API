package testAPI;

import java.util.Scanner;

public class App implements AppInterface{

	private Integer sc;
	public App() {
		sc = new Integer(10);
	}
	public void run () {
		System.out.println(sc.toString());
	}
}
