package es.neoris.operations.oms.createSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.amdocs.aif.consumer.OperationInputs;
import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.services.oms.lib.CreateOMSSessionRequest;

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
	private ApplicationContext m_appContext;
	private OrderingContext m_orderContext;
	private CreateOMSSessionRequest m_OMSSession;
	private MaskInfo m_mask;


	/**
	 * Constructor
	 */
	public InputParamsCreateSession () {
		/* default no-op constructor */
	}
	

	public ApplicationContext getM_appContext() {
		return m_appContext;
	}



	public void setM_appContext(ApplicationContext m_appContext) {
		this.m_appContext = m_appContext;
	}



	public OrderingContext getM_orderContext() {
		return m_orderContext;
	}



	public void setM_orderContext(OrderingContext m_orderContext) {
		this.m_orderContext = m_orderContext;
	}



	public CreateOMSSessionRequest getM_OMSSession() {
		return m_OMSSession;
	}



	public void setM_OMSSession(CreateOMSSessionRequest m_OMSSession) {
		this.m_OMSSession = m_OMSSession;
	}



	public MaskInfo getM_mask() {
		return m_mask;
	}



	public void setM_mask(MaskInfo m_mask) {
		this.m_mask = m_mask;
	}

	
	public void fromMap(Map map) {
        if (map != null) {
        	
            Object value = null;

            if ((value = map.get("applicationContext")) != null && value instanceof ApplicationContext) {
                this.m_appContext = (ApplicationContext) value;
            }
            if ((value = map.get("orderingContext")) != null && value instanceof OrderingContext) {
                this.m_orderContext = (OrderingContext) value;
            }
            if ((value = map.get("CreateOMSSessionRequest")) != null && value instanceof CreateOMSSessionRequest) {
                this.m_OMSSession = (CreateOMSSessionRequest) value;
            }
            if ((value = map.get("maskInfo")) != null && value instanceof MaskInfo) {
                this.m_mask = (MaskInfo) value;
            }
        }
		
	}

	
	public Map toMap() {

        HashMap<String, Object> retMap = new HashMap<String, Object>();

        retMap.put("applicationContext", this.m_appContext);
        retMap.put("orderingContext", this.m_orderContext);
        retMap.put("CreateOMSSessionRequest", this.m_OMSSession);
        retMap.put("maskInfo", this.m_mask);
        return retMap;
		
	}
	
	
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.createSession.InputParamsCreateSession@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\napplicationContext=");
        buf.append(this.m_appContext == null ? "null" : this.m_appContext.toString());
        buf.append(",");
        buf.append("\norderingContext=");
        buf.append(this.m_orderContext == null ? "null" : this.m_orderContext.toString());
        buf.append(",");
        buf.append("\nCreateOMSSessionRequest=");
        buf.append(this.m_OMSSession == null ? "null" : this.m_OMSSession.toString());
        buf.append(",");
        buf.append("\nmaskInfo=");
        buf.append(this.m_mask == null ? "null" : this.m_mask.toString());
        buf.append("\n}");
        return buf.toString();
    }
	
}

