package jpkg;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Config {

	private Map<String, String> configvalues = new HashMap<>();
	
    public void populate(Scanner sc) {
    	while(sc.hasNext()) {
    		String line = sc.nextLine()
    				.split("#", 2) [0];	// Handle comments
    		
    		if(line.isEmpty())
    			continue;
    		
    		String[] parts = line.split("=", 2);
    		
    		configvalues.put(parts[0], parts[1]);
    	}
    }
    
    public String getConfigFor(String cfgvalue) {
    	return configvalues.get(cfgvalue);
    }
    
    public int getIntConfigFor(String cfgvalue) {
    	return Integer.parseInt(getConfigFor(cfgvalue));
    }

}
