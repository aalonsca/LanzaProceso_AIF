package es.neoris.operations.oms.createSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.amdocs.aif.consumer.OperationInputs;

import amdocs.core.mapping.Mappable;

/**
 * 	Retrieves the EpiSessionId for a stateless session. 
 *  The returned sessionId is passed as a parameter to all future business methods, 
 *  allowing the bean to retrieve state information for that session.
 *
 *		principalName 			-> java.lang.String
 * @author Neoris
 *
 */
public class InputParamsCreateSession 
implements OperationInputs, Mappable, Serializable
{

	private static final long serialVersionUID = 8802346207864925860L;
	
	// Input objects
	private String m_principalName;
	
	public String getM_principalName() {
		return m_principalName;
	}


	public void setM_principalName(String m_principalName) {
		this.m_principalName = m_principalName;
	}


	/**
	 * Constructor
	 */
	public InputParamsCreateSession () {
		/* default no-op constructor */
	}
	
	
	
	public void fromMap(Map map) {
        if (map != null) {
        	
            Object value = null;

            if ((value = map.get("principalName")) != null && value instanceof String) {
                this.m_principalName = (String) value;
            }
        }
		
	}

	
	public Map toMap() {

        HashMap<String, Object> retMap = new HashMap<String, Object>();

        retMap.put("principalName", this.m_principalName);
        return retMap;
		
	}
	
	
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.createSession.InputParamsCreateSession@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\nprincipalName=");
        buf.append(this.m_principalName == null ? "null" : this.m_principalName);
        buf.append("\n}");
        return buf.toString();
    }
	
}

