package es.neoris.operations.oms.retrieveOrder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.amdocs.aif.consumer.OperationResults;
import com.amdocs.cih.services.order.lib.RetrieveOrderOutput;

import amdocs.core.mapping.Mappable;


/**
 * 	Return object from call AIF operation retrieveOrder
		RetrieveOrderOutput	-> com.amdocs.cih.services.order.lib.RetrieveOrderOutput;
 * @author Neoris
 *
 */
public class OutputParamsRetrieveOrder 
implements OperationResults, Mappable, Serializable
{


	private static final long serialVersionUID = -4867701155499570457L;
	RetrieveOrderOutput m_order;


	public OutputParamsRetrieveOrder() {
		/* default no-op constructor */
	}



	public RetrieveOrderOutput getM_order() {
		return m_order;
	}



	public void setM_order(RetrieveOrderOutput m_order) {
		this.m_order = m_order;
	}

	
	
	public void fromMap(Map map) {
        if (map != null) {
            Object value = null;
            value = map.get("RetrieveOrderOutput");
            if (value != null && value instanceof RetrieveOrderOutput) {
                this.m_order = (RetrieveOrderOutput) value;
            }
        }
    }

    public Map toMap() {
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("RetrieveOrderOutput", this.m_order);
        return retMap;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.retrieveOrder.OutputParamsRetrieveOrder@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\nsessionID=");
        buf.append(this.m_order == null ? "null" : this.m_order.toString());
        buf.append("\n}");
        return buf.toString();
    }
    
    
	
}
