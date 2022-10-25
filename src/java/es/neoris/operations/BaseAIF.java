package es.neoris.operations;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.clarify.cbo.Application;
import com.clarify.cbo.Session;

import amdocs.epi.session.EpiSessionId;
import es.neoris.operations.oms.createSession.CreateSession;
import es.neoris.operations.oms.createSession.InputParamsCreateSession;
import es.neoris.operations.oms.createSession.OutputParamsCreateSession;
import es.neoris.operations.oms.launchOrder.LaunchOrder;
import es.neoris.operations.oms.launchOrder.OutputParamsLaunchOrder;
import es.neoris.operations.oms.retrieveOrder.OutputParamsRetrieveOrder;
import es.neoris.operations.oms.retrieveOrder.RetrieveOrder;




/**
 * Execute an AIF service. Previous, always execute CreateSession AIF.
 * @author NEORIS
 * @version: 1.0
 */
public class BaseAIF {

	// Variables to control connection to db
	private static String ORACLE_DRIVER_CLFY = null;
	protected static Connection oraConexionOMS = null;
	protected static Connection oraConexionPC = null;

	//protected static boolean DebugMode = false;
	// Variables to control WL connection and Amdocs sessions
	protected static Application clfyApp = null;
	protected static Session clfySession = null;
	protected static final String initialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	
	//static final String sDirEject = "res/";
	//static final String s_nombreFich ="mainclass.properties";

	//Output variables
	protected static String ticketAMS;
	protected static String profileID;
	protected static String orderID;
	
	/**
	 * Main function
	 * @param args
	 *   0 -> service name
	 *   1 -> id order
	 *   2 -> process name
	 *   3 -> process version
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		//Previous validation
		if (args.length == 0) {
			throw new Exception("Error. Input parameters missing.");
		}
		
		try {
			// New parent object. Initialize remote services
			BaseAIF proceso = new BaseAIF(true);

		}catch(Exception e) {
			System.out.println("ERROR " + e.toString());
			System.exit(0);
		}
		
		//New session through .properties values
		CreateSession session = new CreateSession(true);		
		try {
						
			profileID = session.execProc().getM_sessionID().getSecurityProfileID(); 
			if (profileID == null) {
				System.out.println("Error getting ticket AMS.");
				throw new Exception("Error getting ticket AMS.");
			}
 
		}catch(Exception e) {
			System.out.println("Error creating OMSSession.");
			throw new Exception("Error creating OMSSession.");
		}
		
		
		try {
			 
			// The args[0] contains the service name to execute
			if (! "".equals(args[0])) {
				
				String service =args[0];				
				if ("LAUNCHORDER".equalsIgnoreCase(service)) {
					
					//Retrieve the order info
					RetrieveOrder order = new RetrieveOrder(true);
					order.setM_orderId(args[1]); //Fill the orderID through the input param
					
					//Call the service to retrieve the Order object
					OutputParamsRetrieveOrder outOrder =order.execProc(); 
					
					orderID = outOrder.getM_order().getOrder(0).getOrderID().getOrderID();					
					if (orderID == null) {
						System.out.println("Error getting order info" + orderID);
						throw new Exception("Error getting order info: " + orderID);
					}
					
					//LaunchOrder service					
					LaunchOrder process = new LaunchOrder(true);
					process.setM_order(outOrder.getM_order().getOrder(0));
					
					OutputParamsLaunchOrder output = process.execProc();
					
					if (output == null) {
						System.out.println("Error executing process " + args[1] + " --> " + args[2] + "(" + args[3] + ")");
						throw new Exception("Error executing process " + args[1] + " --> " + args[2] + "(" + args[3] + ")");
						
					}else{
						
						if (process.writeResult() < 0) {
							System.out.println("Error writing final file." + args[1] + " --> " + args[2] + "(" + args[3] + ")");
							throw new Exception("Error writing final file " + args[1] + " --> " + args[2] + "(" + args[3] + ")");
						}
						
					}
					
				}
			}
			
		}catch(Exception e) {
			System.out.println("Error executing " + args[0] + "." + e.toString());
			throw new Exception("Error executing " + args[0] + ". Exiting...");
		}
		
	}

	/**
	 * Constructor
	 */
	public BaseAIF () {
		//no-op constructor
		
	}

	/**
	 * Constructor
	 */
	public BaseAIF (boolean initApp) {
		System.out.println("Initalizing services...");

		if (initApp) {
			
			//System.out.println(System.getProperty("java.library.path"));
			//System.out.println(System.getProperty("path"));
			
			clfyApp = new Application();
	
			String strModuleDir = clfyApp.getModuleDir();
			System.out.println("ClarifyEnv.xml Dir: " + strModuleDir);
	
			clfySession = clfyApp.getGlobalSession();
			ticketAMS = clfySession.getAsmTicket();

		}
			
		
	}


	/**
	 * Gets service configuration from .properties file
	 * @throws IOException
	 */
	protected static Properties getProperties(String sRutaIni, String sNombreFich) 
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

			return properties;

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
	protected static IOmsServicesRemote prepareConnWL(Map connectionProp, String JNDI, Boolean debug) {
		
		if (debug) {
			System.out.println("Entering prepareConnWL..."); 
		}
		
		Object objref = null;
		
		// Generating WL connection
		try {
			
			// Defining properties to connect 
			Properties propertiesCon = new Properties();
			propertiesCon.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			propertiesCon.put(InitialContext.PROVIDER_URL, connectionProp.get("WLS_URL"));
			if (!"".equals(connectionProp.get("WLS_USER")) && connectionProp.get("WLS_USER") != null) {
				propertiesCon.put(InitialContext.SECURITY_PRINCIPAL, connectionProp.get("WLS_USER"));
				propertiesCon.put(InitialContext.SECURITY_CREDENTIALS, connectionProp.get("WLS_PASS") == null ? "" : connectionProp.get("WLS_PASS"));
			}

			if (debug) {
				System.out.println("Properties created: " + propertiesCon.toString());
				
			}
			
			// Open a RMI connection to server
			InitialContext context = new InitialContext(propertiesCon);
			objref = context.lookup(JNDI);
			IOmsServicesRemoteHome AIFservice = (IOmsServicesRemoteHome) PortableRemoteObject.narrow(objref, IOmsServicesRemoteHome.class);
			return (IOmsServicesRemote) AIFservice.create();
			
			
		}catch(Exception e){
			
			if (debug) {
				System.out.println("ERROR APM Session initialization FAILED: " + e.toString());
			}
			
			return null;
		}

		
	}

	
	/**
	 * Open db connections
	 * @param strService
	 *   LAUNCHORDER  	-> open connection using OMS a PC users 
	 *   ALL 			-> every defined connection
	 * @param Credentials<String, String>
	 * @return Map <String -> name, Connection -> dbConnection> 	    
	 * @throws SQLException
	 */
	protected static Map openDBConnection(String strService, HashMap<String, String> prop, Boolean debug) throws SQLException {

		HashMap connection = new HashMap<String, Connection>();
		
		if (debug) 
			System.out.println("Entering openDBConnection with value : " + strService);

		
		if (!"".equals(strService) && ("LAUNCHORDER".equals(strService) || "ALL".equals(strService))) {

			//Open db connection using OMS user
			try {
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				ORACLE_DRIVER_CLFY = "jdbc:oracle:thin:@" + (String) prop.get("JDBC_DB") + ":" + (String) prop.get("JDBC_PORT") + ":" + (String) prop.get("DB_NAME");
				oraConexionOMS = DriverManager.getConnection(ORACLE_DRIVER_CLFY, (String) prop.get("DB_USER_OMS"), (String) prop.get("DB_PASS_OMS"));

				connection.put("OMS", oraConexionOMS);
				if (debug) System.out.println("ORACLE OMS connection opened");


			} catch (SQLException sqle) {
				
				if (debug) System.out.println("ERROR Opening Oracle OMS DB connection FAILED: " + sqle.toString());
				throw new SQLException("Error opening Oracle db connection: " +  (String) prop.get("DB_NAME") + ":" + (String) prop.get("JDBC_PORT"));
			}
			

			//Open db connection using PC user
			try {
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				ORACLE_DRIVER_CLFY = "jdbc:oracle:thin:@" + (String) prop.get("JDBC_DB") + ":" + (String) prop.get("JDBC_PORT") + ":" + (String) prop.get("DB_NAME");
				oraConexionPC = DriverManager.getConnection(ORACLE_DRIVER_CLFY, (String) prop.get("DB_USER_PC"), (String) prop.get("DB_PASS_PC"));

				connection.put("PC", oraConexionPC);
				if (debug) System.out.println("ORACLE PC connection opened");
				
			} catch (SQLException sqle) {
				
				if (debug) System.out.println( "ERROR Opening Oracle PC DB connection FAILED: " + sqle.toString());
				throw new SQLException("Error opening Oracle db connection: " +  (String) prop.get("DB_NAME") + ":" + (String) prop.get("JDBC_PORT"));
			}
			
		}
		
		return connection;
		
	}	
	
	/**
	 * Close every opened connection
	 * @throws SQLException
	 */
	protected static void closeDBConnection(HashMap<String, Connection> connection, Boolean debug) throws SQLException {

		if (debug) {
			System.out.println( "Entering closeDBConnection");
		}

		//Try to close OMS a PC connection
		if (connection.get("OMS") != null) { 
			try {
				
				connection.get("OMS").close();
			} catch (SQLException sqle) {
				if (debug) System.out.println( "ERROR Closing Oracle OMS DB connection FAILED: " + sqle.toString());
				throw new SQLException("Error closing OMS db connection");
			}
		}

		if (connection.get("PC") != null) { 
			try {
				connection.get("PC").close();
			} catch (SQLException sqle) {
				if (debug) System.out.println( "ERROR Closing Oracle PC DB connection FAILED: " + sqle.toString());
				throw new SQLException("Error closing PC db connection");
			}
			
		}
		
		if (debug) System.out.println("Connections closed");

	}
		


	
}
