package es.neoris.operations.oms.createSession;

import java.util.HashMap;
import java.util.Properties;

import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.amdocs.cih.services.oms.lib.CreateOMSSessionRequest;
import com.amdocs.svcparams.IOmsServicesCreateOMSSessionInputs;
import com.amdocs.svcparams.IOmsServicesCreateOMSSessionResults;

import es.neoris.operations.BaseAIF;

/** Create a new session for using others services.
 * @author Neoris
 *
 */
public class CreateSession 
extends es.neoris.operations.BaseAIF 
{
	
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/createSession/";
	static final String sNombreFich = "createsession.properties";
	static final String sRutaIni = "res/";
	
	
	// Properties for WL connection
	protected static Boolean debugMode = false;
	private HashMap<String, String> connectionProp = new HashMap<String, String>();
	private static final String JNDI = "/omsserver_weblogic/com/amdocs/cih/services/oms/interfaces/IOmsServicesRemote";
	private static IOmsServicesRemote service = null;
	
	// Variables to call service
	private IOmsServicesCreateOMSSessionInputs m_input;
	private IOmsServicesCreateOMSSessionResults m_output;
	
	/**
	 * Default no-operative constructor
	 */
	public CreateSession() {
		// no-op constructor
	}
	
	/**
	 * Reads .properties file to prepare a connection to WL
	 * @param getConf
	 * @throws Exception
	 */
	public CreateSession(Boolean getConf) 
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
					CreateSession.debugMode = true;				
				
			}
			catch (Exception e) {
				
				System.out.println("Error getting " + sNombreFich + ": " + e.getLocalizedMessage());
				throw new Exception("Error reading .properties file");
			}
		}
		
	}
	
	
	
	/**
	 * Prepared WL connection to execute the operation
	 * @return 0 -> OK
	 *        -1 -> Error connecting
	 */
	public IOmsServicesCreateOMSSessionResults execProc() {
		
		m_input = new IOmsServicesCreateOMSSessionInputs();
		m_output = new IOmsServicesCreateOMSSessionResults();

		if (CreateSession.debugMode) {
			System.out.println("Entering execProcess");			
		}
		

		try {

			//Open WL connection through RMI
			service =((IOmsServicesRemoteHome) BaseAIF.createEJBObject(connectionProp, JNDI, CreateSession.debugMode)).create();

			// Fill the input parameters
			m_input.setApplicationContext(BaseAIF.appContext);
			m_input.setOrderingContext(BaseAIF.orderingContext);
			m_input.setMaskInfo(BaseAIF.maskInfo);
			m_input.setCreateOMSSessionRequest(getInputOMSSession());
			
			//Call the AIF service
			m_output.setCreateOMSSessionResponse(service.createOMSSession(m_input.getApplicationContext(), m_input.getOrderingContext(), m_input.getCreateOMSSessionRequest(), m_input.getMaskInfo()));

			
			if (CreateSession.debugMode) {
				System.out.println("Session created.");
			}
		
			
		}catch(Exception e) {
		
			if (CreateSession.debugMode) {
				System.out.println("ERROR getting EPISession. Exiting..." + e.toString());				
			}
			
			return m_output;
		}
		
		
		return m_output;
		
	}
	
	
	private CreateOMSSessionRequest getInputOMSSession() {
		CreateOMSSessionRequest sessionRequest = new CreateOMSSessionRequest();
		
		String ticket = BaseAIF.ticketAMS;
		
		sessionRequest.setLanguage(BaseAIF.clfySession.getLocale().getDisplayLanguage());
		sessionRequest.setAsmTicket(ticket);
		
		return sessionRequest;
		
	}
	


}
