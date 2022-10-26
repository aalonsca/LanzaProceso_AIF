package es.neoris.operations.oms.retrieveOrder;

import java.util.HashMap;
import java.util.Properties;

import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.amdocs.cih.services.order.lib.OrderID;
import com.amdocs.cih.services.order.lib.OrderRef;
import com.amdocs.cih.services.order.lib.RetrieveOrderInput;
import com.amdocs.svcparams.IOmsServicesRetrieveOrderInputs;
import com.amdocs.svcparams.IOmsServicesRetrieveOrderResults;

import es.neoris.operations.BaseAIF;



/**
 * @author Neoris
 *
 */
public class RetrieveOrder 
extends es.neoris.operations.BaseAIF 
{
	
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/retrieveOrder/";
	static final String sNombreFich = "retrieveorder.properties";
	static final String sRutaIni = "res/";

	// Properties for WL connection
	protected static Boolean debugMode = false;
	private HashMap<String, String> connectionProp = new HashMap<String, String>();
	private static final String JNDI = "/omsserver_weblogic/com/amdocs/cih/services/oms/interfaces/IOmsServicesRemote";
	private static IOmsServicesRemote service = null;
	
	
	// Variables to call service
	private IOmsServicesRetrieveOrderInputs m_input;
	private IOmsServicesRetrieveOrderResults m_output;
	
	private String m_orderId;
	

	/**
	 * Default no-operative constructor
	 */
	public RetrieveOrder() {
		// no-op constructor
	};
	
	/**
	 * Reads .properties file to prepare a connection to WL
	 * @param getConf
	 * @throws Exception
	 */
	public RetrieveOrder(Boolean getConf) 
		throws Exception { 
		
		if (getConf) {
			//Get info from .properties files 
			try {
				Properties properties = BaseAIF.getProperties(sRutaIni, sNombreFich);
				
				connectionProp.put("WLS_URL", properties.getProperty("WLS_URL"));
				connectionProp.put("WLS_USER", properties.getProperty("WLS_USER"));
				connectionProp.put("WLS_PASS", properties.getProperty("WLS_PASS"));
				connectionProp.put("DEBUG", properties.getProperty("DEBUG"));

				if ((!"".equals(properties.getProperty("DEBUG"))) && "1".equals(properties.getProperty("DEBUG"))) 
					RetrieveOrder.debugMode = true;				
				
			}
			catch (Exception e) {
				
				System.out.println("Error getting " + sNombreFich + ": " + e.getLocalizedMessage());
				throw new Exception("Error reading .properties file");
			}
		}
		
	}
	

	/**
	 * Execute service retrieveOrder
	 * @return 0 -> OK
	 *        -1 -> Error connecting
	 */
	public IOmsServicesRetrieveOrderResults execProc() {
		
		m_input = new IOmsServicesRetrieveOrderInputs();
		m_output = new IOmsServicesRetrieveOrderResults();
		
		if (RetrieveOrder.debugMode) {
			System.out.println("Entering execProcess");			
		}
		
		try {
			
			//Open WL connection through RMI
			service = ((IOmsServicesRemoteHome) BaseAIF.createEJBObject(connectionProp, JNDI, RetrieveOrder.debugMode)).create();
			
			// Fill the input parameters
	  		m_input.setApplicationContext(BaseAIF.appContext);
	  		m_input.setOrderingContext(BaseAIF.orderingContext);
	  		m_input.setMaskInfo(BaseAIF.maskInfo);
	  		m_input.setRetrieveOrderInput(getInputRetrieveOrder(m_orderId));

			//Call the AIF service
	  		m_output.setRetrieveOrderOutput(service.retrieveOrder(m_input.getApplicationContext(), m_input.getOrderingContext(), m_input.getRetrieveOrderInput(), m_input.getMaskInfo()));
	  		return m_output;
	  		
		}catch(Exception e) {
			if (RetrieveOrder.debugMode) {
				System.out.println("ERROR calling AIF service." + e.toString());				
			}
			return m_output;
			
		}
		
	}
	

	/**
	 *  Initialize RetrieveOrderInput
	 * @return RetrieveOrderInput
	 */
	private RetrieveOrderInput getInputRetrieveOrder(String p_OrderID) {
		RetrieveOrderInput order = new RetrieveOrderInput();
		OrderRef orderRef = new OrderRef();
		
		// Fill the orderID object
		OrderID orderID = new OrderID();		
		orderID.setOrderID(p_OrderID);
		
		// Fill the orderRef
		orderRef.setOrderID(orderID);
		
		//Fill the array of OrderRef
		OrderRef[] arrOrderRef = new OrderRef[1];
		arrOrderRef[0] = orderRef;
		
		//Fill the RetrieveOrderInput. Only index = 0
		order.setOrderRef(arrOrderRef);
		
		
		return order;
	}
	


	public String getM_orderId() {
		return m_orderId;
	}


	public void setM_orderId(String m_orderId) {
		this.m_orderId = m_orderId;
	}

	
	
}
