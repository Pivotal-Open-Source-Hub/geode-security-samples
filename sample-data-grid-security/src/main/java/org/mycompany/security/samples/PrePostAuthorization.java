/*=========================================================================
* This implementation is provided on an "AS IS" BASIS,  WITHOUT WARRANTIES
* OR CONDITIONS OF ANY KIND, either express or implied."
*==========================================================================
*/

package org.mycompany.security.samples;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;

/**
 * A sample implementation of the <code>AccessControl</code> interface that
 * allows authorization depending on the value of the lastUsername field 
 * 
 * @author Guillermo Tantachuco
 */

public class PrePostAuthorization implements AccessControl {
	
  private DistributedMember remoteDistributedMember;
  private LogWriter logger;
  private Region<String, String> permissionPerRole;
  private String endUser ="";
  
  private static final String ROLE_EMPLOYEE = "employee";
  private static final String ROLE_CUSTOMER = "customer";
  
  public static final Set<OperationCode> PERMISSIONS_ROLE_EMPLOYEE = new HashSet<OperationCode>(Arrays.asList(
	  OperationCode.GET, OperationCode.QUERY, 
	  OperationCode.EXECUTE_CQ, OperationCode.CLOSE_CQ,
      OperationCode.STOP_CQ, OperationCode.REGISTER_INTEREST,
      OperationCode.UNREGISTER_INTEREST, OperationCode.KEY_SET,
      OperationCode.CONTAINS_KEY, OperationCode.EXECUTE_FUNCTION, 
	  OperationCode.PUT, OperationCode.PUTALL));

  public static final Set<OperationCode> PERMISSIONS_ROLE_CUSTOMER = new HashSet<OperationCode>(Arrays.asList(
		  OperationCode.GET, OperationCode.PUT ));
  
  public PrePostAuthorization() {
  }

  public static AccessControl create() {
    return new PrePostAuthorization();
  }

  /**
   * init: 
   */
  public void init(Principal principal, 
                   DistributedMember remoteMember,
                   Cache cache) throws NotAuthorizedException {

    this.logger = cache.getSecurityLogger();
	this.logger.config("PrePostAuthorization init - Username [" + principal.getName() + "]");
	
    this.remoteDistributedMember = remoteMember;
    this.permissionPerRole = cache.getRegion("PermissionPerRole");
    this.endUser = principal.getName();
    
  }

  /**
   * authorizeOperation:
   */
  public boolean authorizeOperation(String regionName, OperationContext context) {
	boolean isUserAuthorized = false;
	
    OperationCode opCode = context.getOperationCode();   
        
    // Obtain role of End-User executing this operation on this region
    String endUserRole = this.permissionPerRole.get(this.endUser + "," + regionName);
    
	if (endUserRole == null) {
		isUserAuthorized = false;
  	} else {
  		if (ROLE_EMPLOYEE.equalsIgnoreCase(endUserRole) && 
  				PERMISSIONS_ROLE_EMPLOYEE.contains(opCode)) {
  				isUserAuthorized = true;
  			} else if (ROLE_CUSTOMER.equalsIgnoreCase(endUserRole) &&
  				PERMISSIONS_ROLE_CUSTOMER.contains(opCode)) {
  				isUserAuthorized = true;
  			} else {
  				isUserAuthorized = false;
  			}
  	}
	
    this.logger.config(
    		"AUDIT_TRAIL: " + (isUserAuthorized? "ALLOWED" : "DENIED") +
    		" This Geode user [" + this.endUser +
    		"] with this role [" + endUserRole +
	    	"] invoked this operation [" + opCode +
	        "] in region [" + regionName + 
	        "] from this remote client: " + remoteDistributedMember);
	
	return isUserAuthorized;	
  }
    
  public void close() {
  }

}
