package es.neoris.operations.oms.createSession;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.DynamicAttribute;
import com.amdocs.cih.common.datatypes.OrderActionUserAction;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.amdocs.cih.services.oms.lib.StartOrderInput;
import com.amdocs.cih.services.oms.lib.StartOrderOutput;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionStatusRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionTypeRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionUserActionRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderModeRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderStatusRVT;
import com.amdocs.cih.services.oms.rvt.referencetable.SalesChannelRVT;
import com.amdocs.cih.services.order.lib.OrderHeader;
import com.amdocs.cih.services.order.lib.OrderID;
import com.amdocs.cih.services.orderaction.lib.OrderAction;
import com.amdocs.cih.services.orderaction.lib.OrderActionData;
import com.amdocs.cih.services.orderaction.lib.OrderActionDetails;
import com.amdocs.cih.services.orderaction.lib.OrderActionID;
import com.amdocs.cih.services.party.lib.OrganizationID;
import com.amdocs.cih.services.subscription.lib.SubscriptionGroupID;

import amdocs.bpm.ejb.ProcMgrSession;
import amdocs.core.logging.LogLevel;
import amdocs.core.logging.Logger;
import amdocs.epi.session.EpiSessionId;
import amdocs.epi.util.IdGen;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP.DynOrderMode;
import es.neoris.operations.MainClass;

/**
 * @author Neoris
 *
 */
public class CreateSession 
extends es.neoris.operations.MainClass 
{
	
	// Logger
	static final Logger CONSUMER_LOGGER = Logger.getLogger("es.neoris.operations.oms.createSession.CreateSession");
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/createSession";
	static final String sNombreFich = "createsession.properties";
	static final String sRutaIni = System.getProperty(sDirEject, ".");

	private static String strURL_WLS = null;
	private static String strUser_WLS = null;
	private static String strPassword_WLS = null;
	private static String strDebug = null;
	
	// Properties for WL connection
	private static final String JNDI = "amdocs/bpm/ejb/aif/ProcMgrSession";
	private final static String initialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	private Object objref = null;

	// Variables to call service
	private InputParamsCreateSession m_input;
	private OutputParamsCreateSession m_output;
	
	
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
			CONSUMER_LOGGER.log(LogLevel.SEVERE, "Error getting " + sNombreFich + ": " + e.getLocalizedMessage());
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
				CONSUMER_LOGGER.log(LogLevel.INFO, "URL:" + strURL_WLS);
				CONSUMER_LOGGER.log(LogLevel.INFO, "USER:" + strUser_WLS);
			}

		}catch(Exception e) {
			CONSUMER_LOGGER.log(LogLevel.SEVERE,"ERROR Opening .properties FAILED: " + e.toString()); 
			throw new Exception("Error loading .properties file. Exiting...");
		}
	}		


	/**
	 * Prepared WL connection to execute the operation
	 * @return 0 -> OK
	 *        -1 -> Error connecting
	 */
	public EpiSessionId execProc() {
		
		if (getDebugMode()) {
			CONSUMER_LOGGER.log(LogLevel.INFO,"Entering prepareConnWL..."); 
		}
		
		// Generating WL connection
		try {
			EpiSessionId sessionID = new EpiSessionId();
			
			// Defining properties to connect 
			Properties propertiesCon = new Properties();
			propertiesCon.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			propertiesCon.put(InitialContext.PROVIDER_URL, strURL_WLS);
			if (!"".equals(strUser_WLS) && strUser_WLS != null) {
				propertiesCon.put(InitialContext.SECURITY_PRINCIPAL, strUser_WLS);
				propertiesCon.put(InitialContext.SECURITY_CREDENTIALS, strPassword_WLS == null ? "" : strPassword_WLS);
			}

			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.INFO,"Properties created: " + propertiesCon.toString());
				
			}
			
			// Open a RMI connection to server
			InitialContext context = new InitialContext(propertiesCon);
			objref = context.lookup(JNDI);
			ProcMgrSession AIFservice = (ProcMgrSession) PortableRemoteObject.narrow(objref, ProcMgrSession.class);
			
			// Get the EpiSessionID object
			m_input.setM_principalName(IdGen.uniqueId());			
			sessionID = AIFservice.createSession(m_input.getM_principalName());			
			m_output.setM_sessionID(service);
			
			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.INFO,"Session created.");
			}
			
			return sessionID;
			
		}catch(Exception e){
			
			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR APM Session initialization FAILED: " + e.toString());
			}
			
			return sessionID;
		}
		
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


	public EpiSessionId getService() {
		return service;
	}


	public void setService(EpiSessionId service) {
		this.service = service;
	}

}
