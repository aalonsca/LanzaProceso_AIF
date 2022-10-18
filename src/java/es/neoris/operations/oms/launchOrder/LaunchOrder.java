package es.neoris.operations.oms.launchOrder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.amdocs.cih.common.core.MaskInfo;
import com.amdocs.cih.common.core.sn.ApplicationContext;
import com.amdocs.cih.common.datatypes.OrderingContext;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.amdocs.cih.services.oms.lib.StartOrderingProcessInput;
import com.amdocs.cih.services.oms.lib.StartOrderingProcessOutput;
import com.amdocs.cih.services.order.lib.Order;

import es.neoris.operations.MainClass;

/**
 * @author Neoris
 *
 */
public class LaunchOrder 
extends es.neoris.operations.MainClass 
{
	// Logger
	//static final Logger CONSUMER_LOGGER = Logger.getLogger("es.neoris.operations.oms.launchorder.LaunchOrder");
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/launchorder";
	static final String sNombreFich = "launchorder.properties";
	static final String sRutaIni = "res/";

	private static HashMap<String, String> m_credential = new HashMap<String, String>();
	private static String strURL_WLS = null;
	private static String strUser_WLS = null;
	private static String strPassword_WLS = null;
	private static String strDS_WLS_OMS = null;
	private static String strDS_WLS_PC = null;
	private static String strDebug = null;
	private static String strDBName = null;

	
	// Properties for WL connection
	private static final String JNDI = "/omsserver_weblogic/com/amdocs/cih/services/oms/interfaces/IOmsServicesRemote";
	private Object objref = null;
	private static IOmsServicesRemote service = null;


	// Input variables from main classes
	private String m_strIDContract;
	private String m_strProcessName;
	private String m_strVersion;
	private String m_strObjidLanzado;	
	private Order m_order;
	


	// Variables to call service
	private InputParamsLaunchOrder m_input;
	private OutputParamsLaunchOrder m_output;
	
	
	/**
	 * Default no-operative constructor
	 */
	public LaunchOrder() throws Exception {
		throw new Exception("Cannot create object. Use LaunchOrder(String, String, String) instead.");
	}
	
	
	/**
	 * Create new object and load .properties
	 * @param strIDContract		-> id object when the process will be attach 
	 * @param strProcessName	-> process name to create
	 * @param strVersion		-> process version 
	 * @throws Exception
	 */
	
	@Deprecated	
	public LaunchOrder(String strIDContract, String strProcessName, String strVersion ) 
			throws Exception {

		// Initialize class properties
		setStrIDContract(strIDContract);
		setStrProcessName(strProcessName);
		setStrVersion(strVersion);
		
		//Get info from .properties files 
		try {
			LaunchOrder.getConfiguration();
		}
		catch (Exception e) {
			System.out.println("Error getting " + sNombreFich + ": " + e.getLocalizedMessage());
			throw new Exception("Error reading .properties file");
			
		}
		
	}

	/**
	 * Create new object and load .properties
	 * @param Order				-> Order object, retrieve by the service 
	 * @param strProcessName	-> process name to create
	 * @param strVersion		-> process version 
	 * @throws Exception
	 */
	public LaunchOrder(Order order, String strProcessName, String strVersion ) 
			throws Exception {

		// Initialize class properties
		setStrProcessName(strProcessName);
		setStrVersion(strVersion);
		setM_order(order);
		
		//Get info from .properties files 
		try {
			LaunchOrder.getConfiguration();
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
			strDS_WLS_OMS = properties.getProperty("WLS_DS_OMS");
			strDS_WLS_PC =  properties.getProperty("WLS_DS_PC");
			strDebug = properties.getProperty("DEBUG");
	
			// Loading data for db connection
			m_credential.put("DB_USER_OMS", properties.getProperty("DB_USER_OMS"));
			m_credential.put("DB_PASS_OMS", properties.getProperty("DB_PASS_OMS"));
			m_credential.put("DB_USER_PC", properties.getProperty("DB_USER_PC"));
			m_credential.put("DB_PASS_PC", properties.getProperty("DB_PASS_PC"));
			m_credential.put("DB_NAME", properties.getProperty("DB"));
			m_credential.put("JDBC_DB", properties.getProperty("DB_JDBC"));
			m_credential.put("JDBC_PORT", properties.getProperty("DB_PORT"));
			
			
			if ((!"".equals(strDebug)) && "1".equals(strDebug)) {
				
				setDebugMode(true);
				System.out.println( "URL:" + strURL_WLS);
				System.out.println( "USER:" + strUser_WLS);
				System.out.println( "DATASOURCE OMS:" + strDS_WLS_OMS);
				System.out.println( "DATASOURCE PC:" + strDS_WLS_PC);
				System.out.println( "DB:" + strDBName);			
				System.out.println( "USER_OMS:" + m_credential.get("DB_USER_OMS"));
				System.out.println( "USER_PC:" + m_credential.get("DB_USER_PC"));
				System.out.println( "JDBC:" + m_credential.get("JDBC_DB"));
				System.out.println( "PORT:" + m_credential.get("JDBC_PORT"));
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
	 * Generate and execute process related to TBORDER 
	 * @return 0 --> OK
	 *        -1 --> ERROR
	 */
	public OutputParamsLaunchOrder execProcess() {
		OutputParamsLaunchOrder out = new OutputParamsLaunchOrder();
		
		//boolean commit = true;

		if (getDebugMode()) {
			System.out.println("Entering execProcess");			
		}
		
		 //Open WL connection through RMI
		if (prepareConnWL() < 0) {
			if (getDebugMode()) {
				System.out.println("ERROR WL connection failed. Exiting...");				
			}
			return out;
		}
		
		// Calculate mandatory info to generate input object 
		try {
	          
	          /* TO DO --> Enganchar la sesion con el EJB al que nos hemos conectado
	          //session = (IlSession) EpiSessionContext.findSession(tPooledId);	     
	          session.startTransaction();  // Llamamos asi, aunque es static, para garantizar que se use la sesion a la que nos hemos conectado
          
			  if (getDebugMode()) {
				System.out.println("Transaction started: ");
				System.out.println(session.toString());
				CONSUMER_LOGGER.log(LogLevel.DEBUG, "Transaction started: " + session.toString());	
			  }
			  */
	          
      		// Open db connections...
      		//MainClass.openDBConnection("LAUNCHORDER", getM_credential());
      		//if (MainClass.oraConexionPC == null || MainClass.oraConexionOMS == null) {
      		//	// An error occurred. Exit method 
			//	if (getDebugMode()) {
			//		System.out.println("ERROR checking DB connections. Exiting");	
			//	}
    		//	return out;
      		//}


      		// Fill the input object: m_input
      		// ApplicationContext
      		m_input.setM_appContext(getInputAppContext());
      		
      		// OrderingContext
      		m_input.setM_orderContext(getInputOrderingContext());
      		
      		// StartOrderInput
      		m_input.setM_order(getStartOrderInput(getM_order()));
      		
      		// MaksInfo
      		m_input.setM_mask(getInputMaskInfo());

      		//Call the AIF Service
      		StartOrderingProcessOutput output = service.startOrderingProcess(m_input.getM_appContext(), m_input.getM_orderContext(), m_input.getM_order(), m_input.getM_mask());

      		if (output == null) {
				if (getDebugMode()) {
					System.out.println("ERROR launching order : " + m_input.getM_order().getOrderID().getOrderID());
				}
      			
      		}else{
      			
      			out.setM_order(output);
      			
      		}
      		
		} catch (Exception e) {
			if (getDebugMode()) {
				System.out.println("ERROR creating connection to WL: " + e.toString());
			}
			return out;
		}
		
		return out;
			
	}	

	
	public int writeResult() {
		
		//Guardamos el objid recuperado en un fichero de texto
		BufferedWriter bw = null;
		try {
			
			File fichero = new File(sDirEject + "/out", getStrIDContract() + "_" + getStrVersion());
			bw = new BufferedWriter(new FileWriter(fichero));
			//bw.write(output.getM_order().getOrderID().toString());

		} catch (IOException e) {
			if (getDebugMode()) {
				System.out.println("ERROR handling output file : " + e.toString());
				return -1;
			}
				
		} finally {
			try {
				bw.close();
			} catch (Exception e) {
				System.out.println("ERROR closing output file : " + e.toString());
				return -1;
				
			}
		}
		return 0;
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
	 *  Initialize MakInfo
	 * @return MaskInfo
	 */
	private MaskInfo getInputMaskInfo() {
		MaskInfo mask = new MaskInfo();
		
		return mask;
	}
	

	/**
	 * Fill the StartOrderInput object
	 * @param conn  	--> db connection. If closed, open it
	 * @param orderId   --> order id when the process will be attached
	 * @return StartOrderInput
	 */
	private StartOrderingProcessInput getStartOrderInput(Order order) {
		
		StartOrderingProcessInput sti = new StartOrderingProcessInput();
		
		if (getDebugMode()) {
			System.out.println("Getting StartOrderInput details: " + order.getOrderID().getOrderID());				
		}

		try{
			// StartOrderInput
			sti.setOrderID(order.getOrderID());
      		
		}catch (Exception e){
			if (getDebugMode()) {
				System.out.println("ERROR getting StartOrderInput details: " + e.toString());	
			}
			
			return null;
			
		}
		
		return sti;
	}			
	

	
	
	public InputParamsLaunchOrder getM_input() {
		return m_input;
	}

	public void setM_input(InputParamsLaunchOrder m_input) {
		this.m_input = m_input;
	}

	public OutputParamsLaunchOrder getM_output() {
		return m_output;
	}

	public void setM_output(OutputParamsLaunchOrder m_output) {
		this.m_output = m_output;
	}

	public String getStrIDContract() {
		return m_strIDContract;
	}

	public void setStrIDContract(String strIDContract) {
		this.m_strIDContract = strIDContract;
	}

	public String getStrProcessName() {
		return m_strProcessName;
	}

	public void setStrProcessName(String strProcessName) {
		this.m_strProcessName = strProcessName;
	}

	public String getStrVersion() {
		return m_strVersion;
	}

	public void setStrVersion(String strVersion) {
		this.m_strVersion = strVersion;
	}

	public String getStrObjidLanzado() {
		return m_strObjidLanzado;
	}

	public void setStrObjidLanzado(String strObjidLanzado) {
		this.m_strObjidLanzado = strObjidLanzado;
	}	

	public static HashMap<String, String> getM_credential() {
		return m_credential;
	}


	public static void setM_credential(HashMap<String, String> m_credential) {
		LaunchOrder.m_credential = m_credential;
	}

	public static IOmsServicesRemote getService() {
		return service;
	}


	public void setService(IOmsServicesRemote service) {
		this.service = service;
	}

	public Order getM_order() {
		return m_order;
	}


	public void setM_order(Order m_order) {
		this.m_order = m_order;
	}

}
