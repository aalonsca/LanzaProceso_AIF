package es.neoris.operations.oms.launchorder;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.services.oms.lib.StartOrderInput;
import com.amdocs.cih.services.oms.lib.StartOrderingProcessInput;

import amdocs.core.logging.Logger;
import amdocs.core.mapping.Mappable;

import com.amdocs.aif.consumer.OperationInputs;

/**
 * 	Get objects data from database to call AIF operation StartOrderingProcessService 
		ApplicationContext 			-> com.amdocs.cih.common.core.sn.ApplicationContext
		OrderingContext 			-> com.amdocs.cih.common.datatypes.OrderingContext
		StartOrderingProcessInput 	-> com.amdocs.cih.services.oms.lib.StartOrderingProcessInput
		MaskInfo 					-> com.amdocs.cih.common.core.MaskInfo

 * @author Neoris
 *
 */
public class InputParamsLaunchOrder 
implements OperationInputs, Mappable, Serializable
{

	private static final long serialVersionUID = -903978114783387772L;

	// Logger
	static final Logger CONSUMER_LOGGER = Logger.getLogger("es.neoris.operations.oms.launchorder.InputParamsLaunchOrder");
	
	
	// Input objects
	private ApplicationContext m_appContext;
	private OrderingContext m_orderContext;
	private StartOrderInput m_order;
	private MaskInfo m_mask;
	
	/**
	 * Constructor
	 */
	public InputParamsLaunchOrder () {
		/* default no-op constructor */
	}
	
	
	public ApplicationContext getAppContext() {
		return m_appContext;
	}

	
	public void setAppContext(ApplicationContext appContext) {
		this.m_appContext = appContext;
	}


	public OrderingContext getOrderContext() {
		return m_orderContext;
	}

	
	public void setOrderContext(OrderingContext orderContext) {
		this.m_orderContext = orderContext;
	}


	public StartOrderInput getOrder() {
		return m_order;
	}

	public void setOrder(StartOrderInput order) {
		this.m_order = order;
	}

	public MaskInfo getMask() {
		return m_mask;
	}

	public void setMask(MaskInfo mask) {
		this.m_mask = mask;
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
            if ((value = map.get("StartOrderInput")) != null && value instanceof StartOrderingProcessInput) {
                this.m_order = (StartOrderInput) value;
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
        retMap.put("StartOrderingProcessInput", this.m_order);
        retMap.put("maskInfo", this.m_mask);
        return retMap;
		
	}
	
    public String toString() {
        StringBuffer buf = new StringBuffer("es.neoris.operations.oms.launchorder.InputParamsLaunchOrder@");
        buf.append(Integer.toHexString(this.hashCode()) + " {");
        buf.append("\napplicationContext=");
        buf.append(this.m_appContext == null ? "null" : this.m_appContext.toString());
        buf.append(",");
        buf.append("\norderingContext=");
        buf.append(this.m_orderContext == null ? "null" : this.m_orderContext.toString());
        buf.append(",");
        buf.append("\nStartOrderInput=");
        buf.append(this.m_order == null ? "null" : this.m_order.toString());
        buf.append(",");
        buf.append("\nmaskInfo=");
        buf.append(this.m_mask == null ? "null" : this.m_mask.toString());
        buf.append("\n}");
        return buf.toString();
    }
	
}

