package es.neoris.operations;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import com.clarify.cbo.Application;
import com.clarify.cbo.Session;

import es.neoris.operations.oms.createSession.CreateSession;
import es.neoris.operations.oms.launchOrder.LaunchOrder;
import es.neoris.operations.oms.launchOrder.OutputParamsLaunchOrder;
import es.neoris.operations.oms.retrieveOrder.OutputParamsRetrieveOrder;
import es.neoris.operations.oms.retrieveOrder.RetrieveOrder;




/**
 * Execute an AIF service. Previous, always execute CreateSession AIF.
 * @author NEORIS
 * @version: 1.0
 */
public class MainClass {


	// Variables to control connection to db
	private static String ORACLE_DRIVER_CLFY = null;
	protected static Connection oraConexionOMS = null;
	protected static Connection oraConexionPC = null;
	protected static boolean DebugMode = false;
	protected static String ticketAMS;
	//protected static Application clfyApp = null;
	//protected static Session clfySession = null;

	private Application clfyApp = null;
	private Session clfySession = null;
	
	// Variables to retrieve the results
	static final String sDirEject = "res/";
	static final String s_nombreFich ="mainclass.properties";
	protected static final String initialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	
	//Variables to get .properties values
	private static String strDebug = null;

	
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
			MainClass proceso = new MainClass(true);

		}catch(Exception e) {
			System.out.println("ERROR " + e.toString());
			System.exit(0);
		}
		
		CreateSession session = new CreateSession();		
		try {
						
			ticketAMS = session.execProc().getM_sessionID().getSecurityProfileID(); 
			if (ticketAMS == null) {
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
					RetrieveOrder order = new RetrieveOrder();
					order.setM_orderId(args[1]); //Fill the orderID through the input param
					OutputParamsRetrieveOrder pOrder = order.execProc();
					
					if (pOrder == null) {
						if (getDebugMode()) {
							System.out.println("Error getting order info" + args[1]);
							throw new Exception("Error getting order info: " + args[1]);
						}
						
					}
					
					
					//LaunchOrder service					
					LaunchOrder process = new LaunchOrder(pOrder.getM_order().getOrder(0), args[2], args[3]);	
					OutputParamsLaunchOrder output = process.execProcess();
					
					if (output == null) {
						
						if (getDebugMode()) {
							System.out.println("Error executing process " + args[1] + " --> " + args[2] + "(" + args[3] + ")");
							throw new Exception("Error executing process " + args[1] + " --> " + args[2] + "(" + args[3] + ")");
						}
						
						
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
		
		//finally {
			
			//session.releaseSession();
			
		//}
			
		
	}

	/**
	 * Constructor
	 */
	public MainClass () {
		//no-op constructor
		
	}

	static {
		
		boolean bLoaded = false;
		System.out.println(System.getProperty("java.library.path"));
		String strLib = "JDispLoad";
		//KCboNull temp = new KCboNull();
		Throwable loadError = null;
	    
		for (int i = 0; (!bLoaded) && (i < 64);) {
			try
			{
				if (i != 0) strLib = "JDispLoad" + i;
				String strLibPath = System.mapLibraryName(strLib);
				System.loadLibrary(strLibPath);
				bLoaded = true;
				if (i > 0) {}
				i++;
			}
			catch (Throwable t)
			{
			   i++;
				loadError = t;
			}
		}

		if (bLoaded) {
			int jarVer = 0;
			/*
			try {
				jarVer = buildCboJarVersion();
			} catch (Throwable t) {}
			
			if (jarVer > 0) CBO_JAR_VERSION = jarVer;
			try {
				initIDs(12 + CBO_JAR_VERSION * 100, null);
			} catch (Throwable t) {
				bLoaded = false;
				t.printStackTrace();
			}
			*/
		}
		else
		{
		loadError.printStackTrace();
		System.out.println();
		System.out.println("There may not be enough copies of the library for the class loaders");
		System.out.println("that need it. Check your system settings, and make sure that there are");
		System.out.println("enough copies of JDispLoad for the class loaders that need them.");
		System.out.println("You can make up to 63 copies of the library - 16 are provided by default.");
		}
	}			


	
	
	/**
	 * Constructor
	 */
	public MainClass (boolean initApp) {
		System.out.println("Initalizing services...");

		if (initApp) {
			
			System.out.println(System.getProperty("java.library.path"));
			
			clfyApp = new Application();
	
			String strModuleDir = clfyApp.getModuleDir();
			System.out.println("ClarifyEnv.xml Dir: " + strModuleDir);
	
			clfySession = clfyApp.getGlobalSession();
			

		}
			
		
	}


	
	/**
	 * Open db connections
	 * @param strService
	 *   LAUNCHORDER  	-> open connection using OMS a PC users 
	 *   ALL 			-> every defined connection
	 * @param Credentials<String, String>
	 * 	    
	 * @throws SQLException
	 */
	protected static void openDBConnection(String strService, HashMap<String, String> prop) throws SQLException {

		if (getDebugMode()) {
			System.out.println("Entering openDBConnection with value : " + strService);
		}

		
		if (!"".equals(strService) && ("LAUNCHORDER".equals(strService) || "ALL".equals(strService))) {

			//Open db connection using OMS user
			try {
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				ORACLE_DRIVER_CLFY = "jdbc:oracle:thin:@" + (String) prop.get("JDBC_DB") + ":" + (String) prop.get("JDBC_PORT") + ":" + (String) prop.get("DB_NAME");
				oraConexionOMS = DriverManager.getConnection(ORACLE_DRIVER_CLFY, (String) prop.get("DB_USER_OMS"), (String) prop.get("DB_PASS_OMS"));

				if (getDebugMode()) {
					System.out.println("ORACLE OMS connection opened");
				}

			} catch (SQLException sqle) {
				
				if (getDebugMode()) {
					System.out.println("ERROR Opening Oracle OMS DB connection FAILED: " + sqle.toString());
				}
				
				throw new SQLException("Error opening Oracle db connection: " +  (String) prop.get("DB_NAME") + ":" + (String) prop.get("JDBC_PORT"));
			}
			

			//Open db connection using PC user
			try {
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				ORACLE_DRIVER_CLFY = "jdbc:oracle:thin:@" + (String) prop.get("JDBC_DB") + ":" + (String) prop.get("JDBC_PORT") + ":" + (String) prop.get("DB_NAME");
				oraConexionPC = DriverManager.getConnection(ORACLE_DRIVER_CLFY, (String) prop.get("DB_USER_PC"), (String) prop.get("DB_PASS_PC"));

				if (getDebugMode()) {
					System.out.println("ORACLE PC connection opened");
				}

			} catch (SQLException sqle) {
				
				if (getDebugMode()) {
					System.out.println( "ERROR Opening Oracle PC DB connection FAILED: " + sqle.toString());
				}
				
				throw new SQLException("Error opening Oracle db connection: " +  (String) prop.get("DB_NAME") + ":" + (String) prop.get("JDBC_PORT"));
			}
			
		}
		
	}	
	
	/**
	 * Close every opened connection
	 * @throws SQLException
	 */
	protected static void closeDBConnection() throws SQLException {

		if (getDebugMode()) {
			System.out.println( "Entering closeDBConnection");
		}

		//Try to close OMS a PC connection
		if (oraConexionOMS != null) { 
			try {
				oraConexionOMS.close();
			} catch (SQLException sqle) {
				if (getDebugMode()) {
					System.out.println( "ERROR Closing Oracle OMS DB connection FAILED: " + sqle.toString());
				}
					
				throw new SQLException("Error closing OMS db connection");
			}
		}

		if (oraConexionPC != null) { 
			try {
				oraConexionPC.close();
			} catch (SQLException sqle) {
				if (getDebugMode()) {
					System.out.println( "ERROR Closing Oracle PC DB connection FAILED: " + sqle.toString());
				}
					
				throw new SQLException("Error closing PC db connection");
			}
		}
		
		if (getDebugMode()) {
			System.out.println("Connections closed");

		}
	}
		

	// GETTERS Y SETTERS
	protected static boolean getDebugMode() {
		return DebugMode;
	}

	protected static void setDebugMode(boolean debugMode) {
		DebugMode = debugMode;
	}

	
}
