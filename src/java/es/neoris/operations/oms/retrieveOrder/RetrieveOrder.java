package es.neoris.operations.oms.retrieveOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.amdocs.cih.services.order.lib.OrderID;
import com.amdocs.cih.services.order.lib.OrderRef;
import com.amdocs.cih.services.order.lib.RetrieveOrderInput;
import com.amdocs.cih.services.order.lib.RetrieveOrderOutput;

import amdocs.epi.session.EpiSessionId;
import es.neoris.operations.BaseAIF;
import es.neoris.operations.oms.createSession.CreateSession;



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
	private InputParamsRetrieveOrder m_input = new InputParamsRetrieveOrder();
	private OutputParamsRetrieveOrder m_output = new OutputParamsRetrieveOrder();
	private EpiSessionId sessionID;

	
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
	public OutputParamsRetrieveOrder execProc() {
		
		m_input = new InputParamsRetrieveOrder();
		m_output = new OutputParamsRetrieveOrder();
		sessionID = new EpiSessionId();
		
		if (RetrieveOrder.debugMode) {
			System.out.println("Entering execProcess");			
		}
		
		try {
			
			//Open WL connection through RMI
			service = BaseAIF.prepareConnWL(connectionProp, JNDI, RetrieveOrder.debugMode);
			
			// Fill the input parameters
	  		m_input.setM_appContext(getInputAppContext());
	  		m_input.setM_orderContext(getInputOrderingContext());
	  		m_input.setM_order(getInputRetrieveOrder(m_orderId));
	  		m_input.setM_mask(getInputMaskInfo());

			//Call the AIF service
	  		m_output.setM_order(service.retrieveOrder(m_input.getM_appContext(), m_input.getM_orderContext(), m_input.getM_order(), m_input.getM_mask()));
	  		return m_output;
	  		
		}catch(Exception e) {
			if (RetrieveOrder.debugMode) {
				System.out.println("ERROR calling AIF service." + e.toString());				
			}
			return m_output;
			
		}
		
	}
	
	/**
	 * Initialize ApplicationContext object
	 * @return ApplicationContext
	 */
	
	private ApplicationContext getInputAppContext() {
		ApplicationContext ctx = new ApplicationContext();  		
  		//ctx.setFormatLocale(new Locale("en_US_", "en", "US"));
  		ctx.setFormatLocale(BaseAIF.clfySession.getLocale());
  		
  		return ctx;
	}
	
	/**
	 * Initialize OrderingContext
	 * @return ApplicationContext
	 */
	
	private OrderingContext getInputOrderingContext() {
        OrderingContext ordCtx = new OrderingContext();
        ordCtx.setLocale(BaseAIF.clfySession.getLocale());
        ordCtx.setSecurityToken(BaseAIF.profileID);
        
		return ordCtx;
	}
		
	/**
	 *  Initialize MaskInfo
	 * @return MaskInfo
	 */
	private MaskInfo getInputMaskInfo() {
		MaskInfo mask = new MaskInfo();
		
		return mask;
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
	
	
	public void releaseSession() {
		setSessionID(null);
	}
	
	
	public EpiSessionId getSessionID() {
		return sessionID;
	}


	public void setSessionID(EpiSessionId sessionID) {
		this.sessionID = sessionID;
	}


	public InputParamsRetrieveOrder getM_input() {
		return m_input;
	}

	public void setM_input(InputParamsRetrieveOrder m_input) {
		this.m_input = m_input;
	}

	public OutputParamsRetrieveOrder getM_output() {
		return m_output;
	}

	public void setM_output(OutputParamsRetrieveOrder m_output) {
		this.m_output = m_output;
	}

	public String getM_orderId() {
		return m_orderId;
	}


	public void setM_orderId(String m_orderId) {
		this.m_orderId = m_orderId;
	}

	
	
}
