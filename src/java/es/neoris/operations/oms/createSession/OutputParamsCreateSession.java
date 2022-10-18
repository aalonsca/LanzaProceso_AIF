package es.neoris.operations.oms.createSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.amdocs.aif.consumer.OperationResults;
import com.amdocs.cih.services.oms.lib.CreateOMSSessionResponse;

import amdocs.core.mapping.Mappable;

/**
 * 	Return object from call AIF operation StartOrderingProcessService 
		StartOrderingProcessOutput	-> com.amdocs.cih.services.oms.lib.StartOrderingProcessOutput
 * @author Neoris
 *
 */
public class OutputParamsCreateSession 
implements OperationResults, Mappable, Serializable
{



	private static final long serialVersionUID = -4867701155499570457L;
	private CreateOMSSessionResponse m_sessionID;
	
	
	public CreateOMSSessionResponse getM_sessionID() {
		return m_sessionID;
	}


	public void setM_sessionID(CreateOMSSessionResponse m_sessionID) {
		this.m_sessionID = m_sessionID;
	}


	public OutputParamsCreateSession() {
		/* default no-op constructor */
	}
 
	
	
	public void fromMap(Map map) {
        if (map != null) {
            Object value = null;
            value = map.get("sessionID");
            if (value != null && value instanceof CreateOMSSessionResponse) {
                this.m_sessionID = (CreateOMSSessionResponse) value;
            }
        }
    }

    public Map toMap() {
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("CreateOMSSessionResponse", this.m_sessionID);
        return retMap;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.createSession.OutputParamsLaunchOrder@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\nCreateOMSSessionResponse=");
        buf.append(this.m_sessionID == null ? "null" : this.m_sessionID.toString());
        buf.append("\n}");
        return buf.toString();
    }
    
    
	
}
