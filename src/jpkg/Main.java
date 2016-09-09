package jpkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import jpkg.build.BuildMain;
import jpkg.fetch.FetchMain;

public final class Main {
	public static File configfile = new File("config.cfg");
	public static Config mainconfig = new Config();
	
	public static void main(String[] args) {
		if(args.length < 1) {
			System.err.println("Expected at least one argument!");
			return;
		}
		
		Scanner sc = null;
		try {
			sc = new Scanner(configfile);
		} catch (FileNotFoundException e) {
			System.err.println("Config file not found!");
			return;
		}
		
		mainconfig.populate(sc);
		
		String[] args_sub = Arrays.copyOfRange(args, 1, args.length);
		
		switch(args[0]) {
		
		case "build":
			BuildMain.run(args_sub);
			break;
			
		case "fetch":
			FetchMain.run(args_sub);
			break;
		
		}
	}

}
