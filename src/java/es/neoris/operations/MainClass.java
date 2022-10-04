package es.neoris.operations;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import com.clarify.cbo.Application;

import amdocs.core.logging.LogLevel;
import amdocs.core.logging.Logger;
import amdocs.epi.session.EpiSessionId;
import es.neoris.operations.oms.createSession.CreateSession;
import es.neoris.operations.oms.launchOrder.LaunchOrder;
import es.neoris.operations.oms.launchOrder.OutputParamsLaunchOrder;




/**
 * Lanza un proceso de APM y lo asocia al objeto de OMS correspondiente
 * @author NEORIS
 * @version: 1.0
 */
public class MainClass {

	// Logger
	private static final Logger CONSUMER_LOGGER = Logger.getLogger("es.neoris.operations.oms.launchorder.MainClass");

	// Variables to control connection to db
	private static String ORACLE_DRIVER_CLFY = null;
	
	// Variables to control session
	protected static Application app = null;
			
	protected static Connection oraConexionOMS = null;
	protected static Connection oraConexionPC = null;
	protected static boolean DebugMode = false;
	protected static EpiSessionId epiSession = null;
	
	// Variables to retrieve the results
	static final String sDirEject = "es/neoris/operations/oms/launchorder";
	static final String sRutaIni = System.getProperty(sDirEject, ".");


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
		
		// New parent object. Initialize remote services
		MainClass proceso = new MainClass();

		try {
			 
			// The args[0] contains the service name to execute
			if (! "".equals(args[0])) {
				
				String service =args[0];				
				if ("LAUNCHORDER".equalsIgnoreCase(service)) {
					
					// 
					
					//LaunchOrder service					
					LaunchOrder process = new LaunchOrder(args[1], args[2], args[3]);	
					OutputParamsLaunchOrder output = process.execProcess();
					
					//Guardamos el objid recuperado en un fichero de texto
					BufferedWriter bw = null;
					try {
						
						File fichero = new File(sRutaIni + "/out", process.getStrIDContract() + "_" + process.getStrVersion());
						bw = new BufferedWriter(new FileWriter(fichero));
						bw.write(output.getM_order().getOrderID().toString());
		
					} catch (IOException e) {
						if (getDebugMode()) {
							CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR handling output file : " + e.toString());	
						}
							
					} finally {
						try {
							bw.close();
						} catch (Exception e) {}
					}
					
					
				}
			}
			
		}catch(Exception e) {
			CONSUMER_LOGGER.log(LogLevel.SEVERE, "Error executing " + args[0] + "." + e.getLocalizedMessage());
			throw new Exception("Error executing " + args[0] + ". Exiting...");
		}
		

	}

	/**
	 * Constructor
	 */
	public MainClass () {
	
		//Start application services (Logger / AIF)
		app = new Application(false);
		app.initialize();
		
		try {
			CreateSession session = new CreateSession();
			
			epiSession = session.getM_output().getM_sessionID();
			
		}catch(Exception e) {
			CONSUMER_LOGGER.log(LogLevel.SEVERE, "Error creating EpiSessionID");
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
			CONSUMER_LOGGER.log(LogLevel.DEBUG, "Entering openDBConnection with value : " + strService);
		}

		
		if (!"".equals(strService) && ("LAUNCHORDER".equals(strService) || "ALL".equals(strService))) {

			//Open db connection using OMS user
			try {
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				ORACLE_DRIVER_CLFY = "jdbc:oracle:thin:@" + (String) prop.get("JDBC_DB") + ":" + (String) prop.get("JDBC_PORT") + ":" + (String) prop.get("DB_NAME");
				oraConexionOMS = DriverManager.getConnection(ORACLE_DRIVER_CLFY, (String) prop.get("DB_USER_OMS"), (String) prop.get("DB_PASS_OMS"));

				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.DEBUG, "ORACLE OMS connection opened");
				}

			} catch (SQLException sqle) {
				
				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR Opening Oracle OMS DB connection FAILED: " + sqle.toString());
				}
				
				throw new SQLException("Error opening Oracle db connection: " +  (String) prop.get("DB_NAME") + ":" + (String) prop.get("JDBC_PORT"));
			}
			

			//Open db connection using PC user
			try {
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				ORACLE_DRIVER_CLFY = "jdbc:oracle:thin:@" + (String) prop.get("JDBC_DB") + ":" + (String) prop.get("JDBC_PORT") + ":" + (String) prop.get("DB_NAME");
				oraConexionPC = DriverManager.getConnection(ORACLE_DRIVER_CLFY, (String) prop.get("DB_USER_PC"), (String) prop.get("DB_PASS_PC"));

				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.DEBUG, "ORACLE PC connection opened");
				}

			} catch (SQLException sqle) {
				
				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR Opening Oracle PC DB connection FAILED: " + sqle.toString());
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
			CONSUMER_LOGGER.log(LogLevel.DEBUG, "Entering closeDBConnection");
		}

		//Try to close OMS a PC connection
		if (oraConexionOMS != null) { 
			try {
				oraConexionOMS.close();
			} catch (SQLException sqle) {
				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR Closing Oracle OMS DB connection FAILED: " + sqle.toString());
				}
					
				throw new SQLException("Error closing OMS db connection");
			}
		}

		if (oraConexionPC != null) { 
			try {
				oraConexionPC.close();
			} catch (SQLException sqle) {
				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR Closing Oracle PC DB connection FAILED: " + sqle.toString());
				}
					
				throw new SQLException("Error closing PC db connection");
			}
		}
		
		if (getDebugMode()) {
			CONSUMER_LOGGER.log(LogLevel.INFO, "Connections closed");

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
