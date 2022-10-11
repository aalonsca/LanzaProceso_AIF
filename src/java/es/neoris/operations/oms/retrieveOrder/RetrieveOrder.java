package es.neoris.operations.oms.retrieveOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

/**
 * @author Neoris
 *
 */
public class RetrieveOrder 
extends es.neoris.operations.MainClass 
{
	
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/retrieveOrder/";
	static final String sNombreFich = "retrieveorder.properties";
	static final String sRutaIni = "res/";

	private static String strURL_WLS = null;
	private static String strUser_WLS = null;
	private static String strPassword_WLS = null;
	private static String strDebug = null;
	
	// Properties for WL connection
	private static final String JNDI = "/omsserver_weblogic/amdocs/bpm/ejb/ProcMgrSession";
	
	private Object objref = null;
	private static IOmsServicesRemote service = null;	
	
	// Variables to call service
	private InputParamsRetrieveOrder m_input;
	private OutputParamsRetrieveOrder m_output;
	private EpiSessionId sessionID;

	
	private String m_orderId;
	

	/**
	 * Default no-operative constructor
	 */
	public RetrieveOrder() 
		throws Exception { 
		
		//Get info from .properties files 
		try {
			RetrieveOrder.getConfiguration();
		}
		catch (Exception e) {
			
			System.out.println("Error getting " + sNombreFich + ": " + e.getLocalizedMessage());
			throw new Exception("Error reading .properties file");
		}
		
		
	}
	

	/**
	 * Gets service configuration from .properties file
	 * @throws IOException
	 */
	private static void getConfiguration() 
			throws IOException, Exception {
		
		try {
			//Get environment values from .properties
			Properties properties = new Properties();
			
			//Open file
			File fProp = new File(sRutaIni, sNombreFich);
			
			if (!fProp.canRead()) {
				throw new IOException("File does not exists: " + fProp.getAbsolutePath());
			}
			
			// Load .properties into local variables y close file
			FileInputStream fiProp = new FileInputStream(fProp);
			properties.load(fiProp);
			fiProp.close();
		
			strURL_WLS = properties.getProperty("WLS_URL");
			strUser_WLS = properties.getProperty("WLS_USER");
			strPassword_WLS = properties.getProperty("WLS_PASS");
			strDebug = properties.getProperty("DEBUG");
			
			if ((!"".equals(strDebug)) && "1".equals(strDebug)) {
				setDebugMode(true);
				System.out.println( "URL:" + strURL_WLS);
				System.out.println( "USER:" + strUser_WLS);
			}

		}catch(Exception e) {
			System.out.println("ERROR Opening .properties FAILED: " + e.toString()); 
			throw new Exception("Error loading .properties file. Exiting...");
		}
	}		



	/**
	 * Prepared WL connection to execute the operation
	 * @return 0 -> OK
	 *        -1 -> Error connecting
	 */
	private int prepareConnWL() {
		
		if (getDebugMode()) {
			System.out.println("Entering prepareConnWL..."); 
		}
		
		// Generating WL connection
		try {
			// Defining properties to connect 
			Properties propertiesCon = new Properties();
			propertiesCon.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			propertiesCon.put(InitialContext.PROVIDER_URL, strURL_WLS);
			if (!"".equals(strUser_WLS) && strUser_WLS != null) {
				propertiesCon.put(InitialContext.SECURITY_PRINCIPAL, strUser_WLS);
				propertiesCon.put(InitialContext.SECURITY_CREDENTIALS, strPassword_WLS == null ? "" : strPassword_WLS);
			}

			if (getDebugMode()) {
				System.out.println("Properties created: " + propertiesCon.toString());
				
			}
			
			// Open a RMI connection to server
			InitialContext context = new InitialContext(propertiesCon);
			objref = context.lookup(JNDI);
			IOmsServicesRemoteHome AIFservice = (IOmsServicesRemoteHome) PortableRemoteObject.narrow(objref, IOmsServicesRemoteHome.class);
			service = AIFservice.create();
			
			
			if (getDebugMode()) {
				System.out.println("Object created.");
			}
			
			return 0;
			
		}catch(Exception e){
			
			if (getDebugMode()) {
				System.out.println("ERROR APM Session initialization FAILED: " + e.toString());
			}
			
			return -1;
		}
		
	}
	

	/**
	 * Execute service retrieveOrder
	 * @return 0 -> OK
	 *        -1 -> Error connecting
	 */
	public OutputParamsRetrieveOrder execProc() {
		
		OutputParamsRetrieveOrder output = new OutputParamsRetrieveOrder();
		RetrieveOrderOutput orderOutput = new RetrieveOrderOutput();
		
		if (getDebugMode()) {
			System.out.println("Entering execProcess");			
		}
		
		 //Open WL connection through RMI
		if (prepareConnWL() < 0) {
			if (getDebugMode()) {
				System.out.println("ERROR WL connection failed. Exiting...");				
			}
			return output;
		}

  		// Fill the input object: m_input
  		// ApplicationContext
  		m_input.setM_appContext(getInputAppContext());
  		
  		// OrderingContext
  		m_input.setM_orderContext(getInputOrderingContext());
  		
  		// RetrieveOrderInput
  		m_input.setM_order(getInputRetrieveOrder(m_orderId));
  		
  		// MaksInfo
  		m_input.setM_mask(getInputMaskInfo());

  		//Call the AIF Service
  		try {
  			orderOutput = service.retrieveOrder(m_input.getM_appContext(), m_input.getM_orderContext(), m_input.getM_order(), m_input.getM_mask());
  		
	  		if (output != null) {
	  			output.setM_order(orderOutput);
	  		}
	  		
  		}catch(Exception e) {
			if (getDebugMode()) {
				System.out.println("ERROR getting orderOutput...");				
			}
			return output;

  		}
		
		return output;
		
	}
	
	/**
	 * Initialize ApplicationContext object
	 * @return ApplicationContext
	 */
	
	private ApplicationContext getInputAppContext() {
  		ApplicationContext ctx = new ApplicationContext();  		
  		//ctx.setFormatLocale(MainClass.m_app.getGlobalSession().getLocale());
  		
  		return ctx;
	}
	
	/**
	 * Initialize OrderingContext
	 * @return ApplicationContext
	 */
	
	private OrderingContext getInputOrderingContext() {
        OrderingContext ordCtx = new OrderingContext();
        //ordCtx.setLocale(MainClass.m_app.getGlobalSession().getLocale());
        //ordCtx.setSecurityToken(MainClass.m_app.getGlobalSession().getAsmTicket());
	
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
		
		//Fill the RetrieveOrderInput. Only index = 0
		order.setOrderRef(0, orderRef);
		
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
