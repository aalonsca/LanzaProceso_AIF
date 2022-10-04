package es.neoris.operations.oms.launchorder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.amdocs.aif.consumer.OperationResults;
import com.amdocs.cih.services.oms.lib.StartOrderOutput;
import com.amdocs.cih.services.oms.lib.StartOrderingProcessOutput;

import amdocs.core.logging.Logger;
import amdocs.core.mapping.Mappable;

/**
 * 	Return object from call AIF operation StartOrderingProcessService 
		StartOrderingProcessOutput	-> com.amdocs.cih.services.oms.lib.StartOrderingProcessOutput
 * @author Neoris
 *
 */
public class OutputParamsLaunchOrder 
implements OperationResults, Mappable, Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -561774673747545030L;

	// Logger
	static final Logger CONSUMER_LOGGER = Logger.getLogger("es.neoris.operations.oms.launchorder.OutputParamsLaunchOrder");

	private StartOrderOutput m_order;
	
	
	public OutputParamsLaunchOrder() {
		/* default no-op constructor */
	}
	
    public StartOrderOutput getM_order() {
		return m_order;
	}

	public void setM_order(StartOrderOutput m_order) {
		this.m_order = m_order;
	}

	
	public void fromMap(Map map) {
        if (map != null) {
            Object value = null;
            value = map.get("StartOrderingProcessOutput");
            if (value != null && value instanceof StartOrderingProcessOutput) {
                this.m_order = (StartOrderOutput) value;
            }
        }
    }

    public Map toMap() {
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("StartOrderingProcessOutput", this.m_order);
        return retMap;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.launchorder.OutputParamsLaunchOrder@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\nStartOrderingProcessOutput=");
        buf.append(this.m_order == null ? "null" : this.m_order.toString());
        buf.append("\n}");
        return buf.toString();
    }
    
    
	
}
