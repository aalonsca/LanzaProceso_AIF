package es.neoris.operations.oms.retrieveOrder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.amdocs.aif.consumer.OperationInputs;
import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.services.oms.lib.StartOrderInput;
import com.amdocs.cih.services.order.lib.RetrieveOrderInput;

import amdocs.core.mapping.Mappable;

/**
 * 	Input parameters for retrieveOrder service 
 *		
 * @author Neoris
 *
 */
public class InputParamsRetrieveOrder 
implements OperationInputs, Mappable, Serializable
{

	private static final long serialVersionUID = 8802346207864925860L;
	
	// Input objects
	private ApplicationContext m_appContext;
	private OrderingContext m_orderContext;
	private RetrieveOrderInput m_order = new RetrieveOrderInput();
	private MaskInfo m_mask;


	/**
	 * Constructor
	 */
	public InputParamsRetrieveOrder () {
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


	public RetrieveOrderInput getM_order() {
		return m_order;
	}


	public void setM_order(RetrieveOrderInput m_order) {
		this.m_order = m_order;
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
            if ((value = map.get("RetrieveOrderInput")) != null && value instanceof RetrieveOrderInput) {
                this.m_order = (RetrieveOrderInput) value;
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
        retMap.put("RetrieveOrderInput", this.m_order);
        retMap.put("maskInfo", this.m_mask);
        return retMap;
		
	}
	
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.retrieveOrder.InputParamsRetrieveOrder@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\napplicationContext=");
        buf.append(this.m_appContext == null ? "null" : this.m_appContext.toString());
        buf.append(",");
        buf.append("\norderingContext=");
        buf.append(this.m_orderContext == null ? "null" : this.m_orderContext.toString());
        buf.append(",");
        buf.append("\nRetrieveOrderInput=");
        buf.append(this.m_order == null ? "null" : this.m_order.toString());
        buf.append(",");
        buf.append("\nmaskInfo=");
        buf.append(this.m_mask == null ? "null" : this.m_mask.toString());
        buf.append("\n}");
        return buf.toString();
    }
	
}

