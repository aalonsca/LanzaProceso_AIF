package es.neoris.operations;


import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.clarify.cbo.Application;

import amdocs.bpm.BusinessProcess;
import amdocs.bpm.FocusKind;
import amdocs.bpm.ProcInstManager;
import amdocs.bpm.ProcInstManagerFactory;
import amdocs.bpm.ProcInstTraversal;
import amdocs.bpm.ProcessStatus;
import amdocs.bpm.RootCreateInfo;
import amdocs.bpm.Step;
import amdocs.bpm.ejb.ProcMgrSession;
import amdocs.bpm.ejb.ProcMgrSessionHome;
import amdocs.core.AlreadyInitializedException;
import amdocs.core.logging.LogLevel;
import amdocs.core.logging.Logger;
import amdocs.epi.session.EpiSessionContext;
import amdocs.epi.session.EpiSessionId;
import amdocs.epi.util.EpiCollections;
import amdocs.epi.util.ListSet;
import amdocs.epi.util.params.StringHolder;
import amdocs.ofc.process.BaseProcInst;
import amdocs.ofc.process.BaseStepInst;
import amdocs.ofc.process.Milestone;
import amdocs.ofc.process.MilestoneStep;
import amdocs.ofc.process.ProcessCreateInfo;
import amdocs.oms.apcore.ApItem;
import amdocs.oms.bpaproxy.OmsBpaCase;
import amdocs.oms.cih.mapping.CihOrderActionFilter;
import amdocs.oms.infra.IlSession;  // ofc.jar
import amdocs.oms.infra.domains.ActionTypeTP;
import amdocs.oms.infra.domains.ActionTypeTP.ActionType;
import amdocs.oms.infra.domains.BooleanValTP;
import amdocs.oms.infra.domains.BooleanValTP.BooleanVal;
import amdocs.oms.infra.domains.ConfigurationTP;
import amdocs.oms.infra.domains.LanguageCodeTP;
import amdocs.oms.infra.domains.OrderActionStatusTP;
import amdocs.oms.infra.domains.OrderActionStatusTP.OrderActionStatus;
import amdocs.oms.infra.domains.LanguageCodeTP.LanguageCode;
import amdocs.oms.infra.domains.MilestoneStatusTP;
import amdocs.oms.infra.domains.OrderActionParentRelationTP;
import amdocs.oms.infra.domains.OrderActionParentRelationTP.OrderActionParentRelation;
import amdocs.oms.infra.domains.OrderStatusTP;
import amdocs.oms.infra.domains.OrderUnitTypeTP;
import amdocs.oms.infra.domains.OrderStatusTP.OrderStatus;
import amdocs.oms.infra.domains.OrderUnitTypeTP.OrderUnitType;
import amdocs.oms.infra.domains.RecontactPeriodTP;
import amdocs.oms.infra.domains.RecontactPeriodTP.RecontactPeriod;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP.DynOrderMode;
import amdocs.oms.infra.exceptions.OmsCreateRequestFailedException;
import amdocs.oms.infra.exceptions.OmsDataNotFoundException;
import amdocs.oms.infra.exceptions.OmsInvalidImplementationException;
import amdocs.oms.infra.exceptions.OmsInvalidUsageException;
import amdocs.oms.ocs.InitialProcessService;

import amdocs.oms.oem.OmOrder;  // core_domain.jar
import amdocs.oms.oem.OmOrderAction;
import amdocs.oms.oem.OmOrderHolder;
import amdocs.oms.osmancust.OmOrderAddition;
import amdocs.oms.pc.PcProcessDefinition;
//import amdocs.oms.pc.PcProcessDefinitionAug;
import amdocs.oms.rootset.AlRootSet;
import amdocs.oms.rootset.RootSetBase;
//import sun.org.mozilla.javascript.internal.Callable;

import com.amdocs.cih.services.oms.lib.StartOrderInput;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionStatusRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionTypeRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionUserActionRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderModeRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderStatusRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderUserActionRVT;
import com.amdocs.cih.services.oms.rvt.referencetable.SalesChannelRVT;
import com.amdocs.cih.services.order.lib.Order;
import com.amdocs.cih.services.order.lib.OrderID;
import com.amdocs.cih.services.orderaction.lib.OrderAction;
import com.amdocs.cih.services.orderaction.lib.OrderActionData;
import com.amdocs.cih.services.orderaction.lib.OrderActionDetails;
import com.amdocs.cih.services.orderaction.lib.OrderActionID;
import com.amdocs.cih.services.order.lib.OrderDetails;
import com.amdocs.cih.services.order.lib.OrderHeader;
import com.amdocs.cih.services.party.lib.OrganizationID;
import com.amdocs.cih.services.party.lib.PersonHeader;
import com.amdocs.cih.services.party.lib.PersonID;
import com.amdocs.cih.services.subscription.lib.SubscriptionGroupID;
import com.amdocs.aif.AifInitializer;
import com.amdocs.cih.common.datatypes.DynamicAttribute;
import com.amdocs.cih.common.datatypes.OrderActionUserAction;
import com.amdocs.cih.common.datatypes.OrderUserAction;
import com.clarify.cbo.Session;
import com.clarify.cbo.SqlExec;
import com.clarify.cbo.Application;

//++paco
import java.sql.Statement;
import amdocs.uams.UamsSecurityException;
import es.neoris.operations.oms.launchorder.LaunchOrder;
import es.neoris.operations.oms.launchorder.OutputParamsLaunchOrder;
import amdocs.epi.util.IdGen;
import amdocs.oms.pc.PcProcessDefinitionAug;
import java.sql.PreparedStatement;
import java.util.HashMap;
import amdocs.epi.lock.DistributedLockManager;
import amdocs.epi.lock.LockManagerFactory;
import amdocs.epi.proxy.meta.MetaProxy;
import amdocs.epi.proxy.session.bean.ProxySession;
import amdocs.epi.proxy.session.bean.ProxySessionBean;
import amdocs.epi.proxy.session.bean.ProxySessionHome;
import amdocs.epi.datamanager.DataManagerFactory;
import amdocs.epi.datamanager.DataManagerCls;
import amdocs.epi.datamanager.DataManager;




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
	
	private Object objref = null;
	private EpiSessionId tPooledId = null;
	private ProcMgrSession procSess = null;
	private ProcMgrSessionHome procMgrLocal = null;
	private ProxySession pSession = null; 
	private IlSession session = null;

	private ProxySessionHome proxy = null;
	
			
	
	protected String salesChannel = null;
	protected static Application clfyAppOms = null;
	protected static Session clfySessionOms = null;
	protected static Application clfyAppPC = null;
	protected static Session clfySessionPC = null;
	protected static Connection oraConexionOMS = null;
	protected static Connection oraConexionPC = null;
	protected static SqlExec clfySqlExecOms = null;
	protected static SqlExec clfySqlExecPC = null;
	
	protected OmOrder order = null;
	protected Order inputOrder = null;
	protected StartOrderInput startOrderInput = null;
	protected PcProcessDefinition processDef = null;
	
	protected static boolean DebugMode = false;
	//++paco
	protected static HashMap<String, Object> settingMap = new HashMap<String, Object>();
	protected static Object m_TableNameReplacementConfigObj = null;
	protected static Object m_PartitionerInitialValueConfigObj = null;
	protected static Object m_LogTransContentsOnRollbackConfigObj = null;
	
	protected static DataManagerFactory dManagerFactory = null;	
	protected static DataManagerCls DataManager = null;	
	//--paco

	/**
	 * Funcion de entrada
	 * @param args
	 *   0 -> id de la orden
	 *   1 -> name del proceso
	 *   2 -> version del proceso
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
					//LaunchOrder service
					LaunchOrder process = new LaunchOrder(args[1], args[2], args[3]);	
					OutputParamsLaunchOrder output = process.execProcess();
					
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
	protected OmOrder getOrder() {
		return order;
	}
	protected void setOrder(OmOrder order) {
		this.order = order;
	}
	protected String getSalesChannel() {
		return salesChannel;
	}
	protected void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	protected static boolean getDebugMode() {
		return DebugMode;
	}

	protected static void setDebugMode(boolean debugMode) {
		DebugMode = debugMode;
	}

	protected PcProcessDefinition getProcessDef() {
		return processDef;
	}

	protected void setProcessDef(PcProcessDefinition processDef) {
		this.processDef = processDef;
	}

	
}
