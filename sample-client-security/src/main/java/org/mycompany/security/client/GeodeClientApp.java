package org.mycompany.security.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.mycompany.security.domain.Account;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.*;
import com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer;

public class GeodeClientApp {
	
  public static void main(String[] args) throws Exception {
	Properties prop = new Properties();
	String username = "";
	String password = "";
	
	try {
		prop.load(GeodeClientApp.class.getResourceAsStream("/common.env"));
		int operation = messageAndPrompt();
	      
	    if (operation < 1 || operation > 3) {
	       System.out.println("Invalid selection: " + operation);
	       System.exit(1);
	    }
	    
	    switch (operation) {
	    case 1:
	    	username = prop.getProperty("CUSTOMER_APP_USER");
	    	password = prop.getProperty("CUSTOMER_APP_PASSWORD");
	    	runScenarios(username, password);
	        break;
	    case 2:
	    	username = prop.getProperty("EMPLOYEE_APP_USER");
	    	password = prop.getProperty("EMPLOYEE_APP_PASSWORD");
	    	runScenarios(username, password);
	        break;
	    default:
	        System.out.println("Invalid option: " + operation);
	        break;
	    }	    
	    
	} catch (Exception e) {
		System.out.println("This app could not find the file 'src/main/resources/common.env'");
		System.out.println("Please find the 'common.env' file and copy it to 'src/main/resources'");
		System.out.println("After that, please re-build this project");
		throw e;
	}
  }
  
  private static void runScenarios(String username, String password) throws Exception {
	ClientCache cache = null;
	Region<String, Account> region = null;
	  
	try {
		System.out.println("Client app connecting to Geode cluster");
	    cache = new ClientCacheFactory()
	      .addPoolLocator("localhost", 10334)
	      .set("security-client-auth-init", "org.mycompany.security.samples.UserPasswordAuthInit.create")
	      .set("security-username", username)
	      .set("security-password", password)
	      .setPdxSerializer(new ReflectionBasedAutoSerializer("org.mycompany.security.domain.Account"))
	      .create();
	    
		System.out.println("Creating Account proxy region in client app");
	    region = cache
	      .<String, Account>createClientRegionFactory(ClientRegionShortcut.PROXY)
	      .create("Account");
	} catch (Exception e) {
		System.out.println("Error creating the Client Cache'");
		System.out.println(e.getCause());
		throw e;
	}
	
    System.out.println("Attempting to PUT individual Account objects into the server's Account region");
    for (int i = 0; i < 2; i++) {
        try {
           Account account = new Account();
           account.setAccountId("100" + i);
           account.setAccountNumber("123456789012345" + i);
           account.setAccountType("CHECKING");
           account.setBalance((double) 100 + i);
           account.setCreditLine((double) 0);
           
           System.out.println("Ready to PUT account [" + account.getAccountId() + "] into the server's Account region");
           region.put(account.getAccountId(), account);
           
       } catch (Exception e) {
         System.err.println("The following error occurred:" + e.getCause());
       }
    }
    
    pressEnterToContinue();
    
    System.out.println("Attempting to GET individual account objects from the server's Account region");
    for (int i = 0; i < 2; i++) {
    	try {
        	String accountId = "100" + i;
        	
        	System.out.println("Ready to GET account [" + accountId + "] from the server's Account region");
            Account account = region.get(accountId);
            
            if (account != null) {
                System.out.println("==> " + account.getAccountId() + ", " + account.getAccountNumber());
            } else {
                System.out.println("The above accountId DOES NOT exist");
            }
            
        } catch (Exception e) {
          System.err.println("The following error occurred:" + e.getCause());
        }
    }
    
    pressEnterToContinue();
    
    Map<String, Account> accountList = new HashMap<String, Account>();
    System.out.println("Attempting to PUT-ALL Account objects into the server's Account region");
    for (int i = 0; i < 2; i++) {
        Account account = new Account();
        account.setAccountId("200" + i);
        account.setAccountNumber("223456789012345" + i);
        account.setAccountType("SAVINGS");
        account.setBalance((double) 200 + i);
        account.setCreditLine((double) 0);
        accountList.put(account.getAccountId(), account);
    }
    
	try {
	    System.out.println("Ready to PUT-ALL into the server's Account region");
	    region.putAll(accountList);
    } catch (Exception e) {
        System.err.println("The following error occurred:" + e.getCause());
    }

    cache.close();
  }
  
  private static int messageAndPrompt() throws Exception {
      System.out.println(" ");
      System.out.println("*** This sample app showcases Geode Authentication and Authorization ***");
      System.out.println(" ");
      System.out.println("==> Authentication: to connect to a Geode cluster, this app creates a connection pool ");
      System.out.println("  using an LDAP service account");
      System.out.println(" ");
      System.out.println("==> Authorization: You can run this app using one of these service accounts:");
      System.out.println("  a) Service account for customer-facing app; which can only PUT and GET a given entry");
      System.out.println("  b) Service account for employee-only app; which can execute PUT, PUT-ALL, GET, QUERY, etc");
      System.out.println(" ");
      System.out.println(" Enter 1 to verify access of Customer-facing app");
      System.out.println(" Enter 2 to verify access of Employee-only app");
      System.out.println(" ");
      System.out.println(" Your selection: ");
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      
      int operation = 0;
      String input = "";
      try {
        input = bufferedReader.readLine();
        operation = Integer.parseInt(input);
      } 
      catch (NumberFormatException nfe) {
        System.out.println("Invalid selection: " + input);
      }      
      return operation;	  
  }
  
  private static void pressEnterToContinue() throws Exception {
	 System.out.println(" ");
	 System.out.println("Press Enter to continue.");
	 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	 bufferedReader.readLine();
  }

}