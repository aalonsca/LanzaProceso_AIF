package es.neoris.operations.oms.createSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.amdocs.cih.services.oms.lib.CreateOMSSessionRequest;

import amdocs.epi.session.EpiSessionId;

/** Create a new session for using others services.
 * @author Neoris
 *
 */
public class CreateSession 
extends es.neoris.operations.MainClass 
{
	
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/createSession/";
	static final String sNombreFich = "createsession.properties";
	static final String sRutaIni = "res/";

	private static String strURL_WLS = null;
	private static String strUser_WLS = null;
	private static String strPassword_WLS = null;
	private static String strDebug = null;
	
	// Properties for WL connection
	//private static final String JNDI = "/omsserver_weblogic/amdocs/bpm/ejb/ProcMgrSession";
	private static final String JNDI = "/omsserver_weblogic/com/amdocs/cih/services/oms/interfaces/IOmsServicesRemote";
	private Object objref = null;
	private static IOmsServicesRemoteHome AIFservice;
	private static IOmsServicesRemote service = null;
	
	// Variables to call service
	private InputParamsCreateSession m_input;
	private OutputParamsCreateSession m_output;
	private EpiSessionId sessionID;
	
	/**
	 * Default no-operative constructor
	 */
	public CreateSession() 
		throws Exception { 
		
		//Get info from .properties files 
		try {
			CreateSession.getConfiguration();
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
			sessionID = new EpiSessionId();
			
			m_input = new InputParamsCreateSession();
			m_output = new OutputParamsCreateSession();
			
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
			AIFservice = (IOmsServicesRemoteHome) PortableRemoteObject.narrow(objref, IOmsServicesRemoteHome.class);
			service = AIFservice.create();
			
			return 0;
			
		}catch(Exception e){
			
			if (getDebugMode()) {
				System.out.println("ERROR APM Session initialization FAILED: " + e.toString());
			}
			
			return -1;
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

		if (getDebugMode()) {
			System.out.println("Entering execProcess");			
		}
		
		 //Open WL connection through RMI
		if (prepareConnWL() < 0) {
			if (getDebugMode()) {
				System.out.println("ERROR WL connection failed. Exiting...");				
			}
			return m_output;
		}
		

		try {
			// 
			m_input.setM_appContext(getInputAppContext());
			m_input.setM_orderContext(getInputOrderingContext());
			m_input.setM_mask(getInputMaskInfo());
			m_input.setM_OMSSession(getInputOMSSession());
			
			m_output.setM_sessionID(service.createOMSSession(m_input.getM_appContext(), m_input.getM_orderContext(), m_input.getM_OMSSession(), m_input.getM_mask()));			

			
			if (getDebugMode()) {
				System.out.println("Session created.");
			}
		
			
		}catch(Exception e) {
		
			if (getDebugMode()) {
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
  		//ctx.setFormatLocale(MainClass.m_app.getGlobalSession().getLocale());
  		
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
		
		String ticket = "";
		
		sessionRequest.setLanguage("en");
		sessionRequest.setClientMachine("");
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
