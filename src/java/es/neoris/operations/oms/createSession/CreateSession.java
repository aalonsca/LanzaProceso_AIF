package es.neoris.operations.oms.createSession;

import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.lib.CreateOMSSessionRequest;

import amdocs.epi.session.EpiSessionId;
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
	private InputParamsCreateSession m_input;
	private OutputParamsCreateSession m_output;
	private EpiSessionId sessionID;
	
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
	public OutputParamsCreateSession execProc() {
		
		m_input = new InputParamsCreateSession();
		m_output = new OutputParamsCreateSession();
		sessionID = new EpiSessionId();

		if (CreateSession.debugMode) {
			System.out.println("Entering execProcess");			
		}
		

		try {

			//Open WL connection through RMI
			service = BaseAIF.prepareConnWL(connectionProp, JNDI, CreateSession.debugMode);

			// Fill the input parameters
			m_input.setM_appContext(getInputAppContext());
			m_input.setM_orderContext(getInputOrderingContext());
			m_input.setM_mask(getInputMaskInfo());
			m_input.setM_OMSSession(getInputOMSSession());
			
			//Call the AIF service
			m_output.setM_sessionID(service.createOMSSession(m_input.getM_appContext(), m_input.getM_orderContext(), m_input.getM_OMSSession(), m_input.getM_mask()));			

			
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
	

	public void releaseSession() {
		setSessionID(null);
	}
	
	
	public EpiSessionId getSessionID() {
		return sessionID;
	}


	public void setSessionID(EpiSessionId sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * Initialize ApplicationContext object
	 * @return ApplicationContext
	 */
	
	private ApplicationContext getInputAppContext() {
		ApplicationContext ctx = new ApplicationContext();  		
  		ctx.setFormatLocale(new Locale("en_US_", "en", "US"));
  		//ctx.setFormatLocale(MainClass);
  		
  		return ctx;
	}
	
	/**
	 * Initialize OrderingContext
	 * @return ApplicationContext
	 */
	
	private OrderingContext getInputOrderingContext() {
        OrderingContext ordCtx = new OrderingContext();
        ordCtx.setLocale(new Locale("en_US_", "en", "US"));
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
	
	private CreateOMSSessionRequest getInputOMSSession() {
		CreateOMSSessionRequest sessionRequest = new CreateOMSSessionRequest();
		
		String ticket = BaseAIF.ticketAMS;
		
		sessionRequest.setLanguage(BaseAIF.clfySession.getLocale().getDisplayLanguage());
		//sessionRequest.setClientMachine("");
		sessionRequest.setAsmTicket(ticket);
		
		return sessionRequest;
		
	}
	
	public InputParamsCreateSession getM_input() {
		return m_input;
	}

	public void setM_input(InputParamsCreateSession m_input) {
		this.m_input = m_input;
	}

	public OutputParamsCreateSession getM_output() {
		return m_output;
	}

	public void setM_output(OutputParamsCreateSession m_output) {
		this.m_output = m_output;
	}

}
