package es.neoris.operations.oms.createSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.amdocs.aif.consumer.OperationResults;
import amdocs.core.mapping.Mappable;
import amdocs.epi.session.EpiSessionId;

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
	private EpiSessionId m_sessionID;
	
	
	public EpiSessionId getM_sessionID() {
		return m_sessionID;
	}


	public void setM_sessionID(EpiSessionId m_sessionID) {
		this.m_sessionID = m_sessionID;
	}


	public OutputParamsCreateSession() {
		/* default no-op constructor */
	}
 
	
	
	public void fromMap(Map map) {
        if (map != null) {
            Object value = null;
            value = map.get("sessionID");
            if (value != null && value instanceof EpiSessionId) {
                this.m_sessionID = (EpiSessionId) value;
            }
        }
    }

    public Map toMap() {
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("sessionID", this.m_sessionID);
        return retMap;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.createSession.OutputParamsLaunchOrder@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\nsessionID=");
        buf.append(this.m_sessionID == null ? "null" : this.m_sessionID.toString());
        buf.append("\n}");
        return buf.toString();
    }
    
    
	
}
