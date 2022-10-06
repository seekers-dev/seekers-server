package org.seekers;

import org.seekers.grpc.RemoteServer;

import javafx.application.Application;

public class Launcher {	
	public static void main(String[] args) {
		new RemoteServer();
		Application.launch(App.class, args);
	}
}
