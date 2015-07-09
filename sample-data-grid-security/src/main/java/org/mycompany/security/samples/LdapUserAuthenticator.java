/*=========================================================================
* This implementation is provided on an "AS IS" BASIS,  WITHOUT WARRANTIES
* OR CONDITIONS OF ANY KIND, either express or implied."
*==========================================================================
*/

package org.mycompany.security.samples;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;

import java.security.Principal;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.util.Hashtable;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * A sample implementation of the <code>Authenticator</code> interface that
 * allows LDAP authentication 
 * 
 * @author Guillermo Tantachuco
 */

public class LdapUserAuthenticator implements Authenticator {
  protected LogWriter securitylog;    
  protected LogWriter systemlog;
  private String ldapUrl = null;
  private String basedn = null;
  private String filter = null;
  public static final String LDAP_URL = "security-ldap-url";
  public static final String LDAP_BASEDN_NAME = "security-ldap-basedn";
  public static final String LDAP_FILTER = "security-ldap-filter";

  public static Authenticator create() {
    return new LdapUserAuthenticator();
  }

  public LdapUserAuthenticator() {
  }

  public Principal authenticate(Properties props, DistributedMember member) {

      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, this.ldapUrl);
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      
      String uid = props.getProperty(UserPasswordAuthInit.USER_NAME);
      if (uid == null) {
          throw new AuthenticationFailedException(
                                                  "LdapUserAuthenticator: user name property ["
                                                  + UserPasswordAuthInit.USER_NAME + "] not provided");
      }
      String password = props.getProperty(UserPasswordAuthInit.PASSWORD);
      if (password == null) {
          password = "";
      }
      
      DirContext ctx = null;
      try {
          // Step 1: Bind anonymously
          ctx = new InitialDirContext(env);
          
          // Step 2: Search the directory
          String base = this.basedn;
          String filter = this.filter;
          SearchControls ctls = new SearchControls();
          ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
          ctls.setReturningAttributes(new String[0]);
          ctls.setReturningObjFlag(true);
          NamingEnumeration enm = ctx.search(base, filter, new String[] { uid }, ctls);
          
          String dn = null;
          
          if (enm.hasMore()) {
              SearchResult result = (SearchResult) enm.next();
              dn = result.getNameInNamespace();
              
              this.systemlog.config("dn: "+dn);
          }
          
          if (dn == null || enm.hasMore()) {
              // uid not found or not unique
              throw new NamingException("Authentication failed");
          }
          
          // Step 3: Bind with found DN and given password
          ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
          ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
          
          // Perform a lookup in order to force a bind operation with JNDI
          ctx.lookup(dn);
          this.systemlog.config("Authentication successful");
          
          enm.close();
          ctx.close();
      } catch (NamingException ne) {
          throw new AuthenticationFailedException(
                                                  "LdapUserAuthenticator: Naming Exception --> Failure with provided username, password "
                                                  + "combination for user name: " + uid + "\n" + ne);
      } catch (Exception e) {
          throw new AuthenticationFailedException(
                                                  "LdapUserAuthenticator: Unknown Exception --> Failure with provided username, password "
                                                  + "combination for user name: " + uid + "\n" + e);
      }
      
      return new UsernamePrincipal(uid);
  }
    
  public void init(Properties securityProps, LogWriter systemLogger,
	      LogWriter securityLogger) throws AuthenticationFailedException {
	      
	    this.systemlog = systemLogger;
	    this.securitylog = securityLogger;
	      
	    this.ldapUrl = securityProps.getProperty(LDAP_URL);
	    if (this.ldapUrl == null || this.ldapUrl.length() == 0) {
	      throw new AuthenticationFailedException(
	          "LdapUserAuthenticator: LDAP URL property [" + LDAP_URL
	              + "] not specified");
	    }
	      
	    this.basedn = securityProps.getProperty(LDAP_BASEDN_NAME);
	    if (this.basedn == null || this.basedn.length() == 0) {
	      throw new AuthenticationFailedException(
	          "LdapUserAuthenticator: LDAP base DN property [" + LDAP_BASEDN_NAME
	              + "] not specified");
	    }

	    this.filter = securityProps.getProperty(LDAP_FILTER);
	    if (this.filter == null || this.filter.length() == 0) {
	          throw new AuthenticationFailedException(
	                "LdapUserAuthenticator: LDAP FILTER property [" + LDAP_FILTER
	                + "] not specified");
	    }
	      
	  }
    
  public void close() {
  }

}
