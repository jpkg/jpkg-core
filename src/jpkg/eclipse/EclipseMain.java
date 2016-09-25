package jpkg.eclipse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class EclipseMain {

	public static void run(String[] args) {
		
		String buildpath;

		String classpath = "";
		
		// Where to actually do the building
		if(args.length == 0)
			buildpath = ".";
		else
			buildpath = args[0];
		
		System.out.println("Setting up eclipse workspace in " + buildpath);
		
		ArrayList<String> deps = new ArrayList<>();
		try {
			Scanner sc = new Scanner(new File(buildpath + "/bin/DEPS"));
			sc.useDelimiter(File.pathSeparator);
			while(sc.hasNext())
				deps.add(sc.next());
			sc.close();
			
			Scanner cp = new Scanner(new File(buildpath + "/.classpath"));
			

			while(cp.hasNext())
				classpath += (cp.nextLine()) + "\n";
			
			cp.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		deps.remove(0);
		
		System.out.println("Injecting dependencies: " + deps);
		
		for(String s : deps) {
			File f = new File(s);
			try {
				String path = f.getCanonicalPath();
				
				String tag = "\t<classpathentry kind=\"lib\" path=\"" + path + "\"/>\n";
				
				if(!classpath.contains(tag)) {
					classpath = classpath.substring(0, classpath.length() - 13) + tag + "</classpath>";
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(new File(buildpath + "/.classpath"));
			fos.write(classpath.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
