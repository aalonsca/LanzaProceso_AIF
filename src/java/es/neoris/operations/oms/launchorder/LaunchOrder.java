package es.neoris.operations.oms.launchorder;

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

import amdocs.core.logging.LogLevel;
import amdocs.core.logging.Logger;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP.DynOrderMode;
import es.neoris.operations.MainClass;

/**
 * @author Neoris
 *
 */
public class LaunchOrder 
extends es.neoris.operations.MainClass 
{
	// Logger
	static final Logger CONSUMER_LOGGER = Logger.getLogger("es.neoris.operations.oms.launchorder.LaunchOrder");
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/launchorder";
	static final String sNombreFich = "launchorder.properties";
	static final String sRutaIni = System.getProperty(sDirEject, ".");

	private static HashMap<String, String> m_credential = new HashMap<String, String>();
	private static String strURL_WLS = null;
	private static String strUser_WLS = null;
	private static String strPassword_WLS = null;
	private static String strDS_WLS_OMS = null;
	private static String strDS_WLS_PC = null;
	private static String strDebug = null;
	private static String strDBName = null;

	
	// Properties for WL connection
	private static final String JNDI = "com/amdocs/cih/services/oms/interfaces/IOmsServicesRemote";
	private final static String initialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	private Object objref = null;
	private IOmsServicesRemote service = null;
	
	// Input variables from main classes
	private String m_strIDContract;
	private String m_strProcessName;
	private String m_strVersion;
	private String m_strObjidLanzado;	

	// Variables to call service
	private InputParamsLaunchOrder m_input;
	private OutputParamsLaunchOrder m_output;
	
	// Queries to get order info from db
	private String strQueryProcessDef = "SELECT P.CID, P.PCVERSION_ID, P.PROCESS_MAP_ACTION, P.LINE_OF_BUSINESS, P.SALES_CHANNEL  FROM TBPROCESS P WHERE P.PROCESS_MAP_NAME = '%1'";
	private String strQueryOmsOrder = "SELECT T.CTDB_LAST_UPDATOR, T.ORDER_MODE, T.GROUP_ID, T.ROOT_CUSTOMER_ID, T.STATUS, T.OPPORTUNITY_ID, T.CURRENT_SALES_CHANNEL, T.DEALER_CODE, T.ADDRESS_ID, T.EXT_REF_NUM" // 10
									 + ", SERVICE_REQ_DATE, CREATION_DATE, APPLICATION_DATE, PROP_EXPIRY_DATE, CUST_ORDER_REF, CONTACT_ID, CUSTOMER_ID, DEPOSIT_ID, ORDER_UNIT_ID" // 20
									 + ", ORDER_UNIT_TYPE, PRIORITY, PROP_EXPIRY_DATE, RECONTACT_IN_MONTH, RECONTACT_IN_YEAR, RECONTACT_PERIOD, SOURCE_ORDER" // 30
									 + " FROM TBORDER T"
									 + " WHERE T.REFERENCE_NUMBER = '%1'";
	
	private String strQuerOmsOrderAction = "SELECT T.ORDER_UNIT_ID, T.PARENT_ORDER_UNIT, T.ORDER_ID, T.STATUS, T.ACTION_TYPE, T.DUE_DATE, T.AP_ID, T.PARENT_RELATION, T.MAIN_IND, T.AP_VERSION_ID" // 10"
									+ ", CONFIGURATION, LANGUAGE, PARENT_ASSOCIATED_ID, SERVICE_REQ_DATE, PRIORITY, APPLICATION_DATE" // 16
									+ ", ORG_OWNER_ID, CONTACT_ID, CUSTOMER_ID, REFERENCE_NUMBER, CREATOR_ID, APPLICATION_REF_ID"  //22
									+ ", ORDER_UNIT_TYPE, APPLICATION_NAME, EXT_REF_NUM, CREATION_DATE, SALES_CHANNEL, REASON_ID"	//28
									+ ", GROUP_ID, DYNAMIC_ATTRS, CUST_ORDER_REF, APPLICATION_REF_ID, EARLY_DATE, EXT_REF_REMARK"	//34
									+ ", ITEM_PARTITION_KEY, PRIVS_KEY, QSEQUENCE_NUM, QUANTITY, REASON_FREE_TEXT"	//39
									+ ", CUST_WILL_RCNT_IND, RECONTACT_PERIOD, RECONTACT_IN_MONTH, RECONTACT_IN_YEAR, REPLACED_OFFER_AP_ID"	//44
									+ ", REQUEST_LINE_ID"	//45
									+ " FROM TBORDER_ACTION T"
									+ " WHERE T.ORDER_ID = '%1'"
									+ " ORDER BY T.ORDER_UNIT_ID";
	
	private String strQueryBPM = "SELECT ID, BYTECODE, NAME, VERSION, OBJID, DEFINITION, STATUS, DEFINITION_VERSION, FOCUS_KIND, FOCUS_TYPE " // 10
								 + " FROM TABLE_BPM_PROCESS "
								 + " WHERE NAME = '%1'"
								 + " AND VERSION = '%2'";
	
	
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
			CONSUMER_LOGGER.log(LogLevel.SEVERE, "Error getting " + this.sNombreFich + ": " + e.getLocalizedMessage());
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
				CONSUMER_LOGGER.log(LogLevel.INFO, "URL:" + strURL_WLS);
				CONSUMER_LOGGER.log(LogLevel.INFO, "USER:" + strUser_WLS);
				CONSUMER_LOGGER.log(LogLevel.INFO, "DATASOURCE OMS:" + strDS_WLS_OMS);
				CONSUMER_LOGGER.log(LogLevel.INFO, "DATASOURCE PC:" + strDS_WLS_PC);
				CONSUMER_LOGGER.log(LogLevel.INFO, "DB:" + strDBName);			
				CONSUMER_LOGGER.log(LogLevel.INFO, "USER_OMS:" + m_credential.get("DB_USER_OMS"));
				CONSUMER_LOGGER.log(LogLevel.INFO, "USER_PC:" + m_credential.get("DB_USER_PC"));
				CONSUMER_LOGGER.log(LogLevel.INFO, "JDBC:" + m_credential.get("JDBC_DB"));
				CONSUMER_LOGGER.log(LogLevel.INFO, "PORT:" + m_credential.get("JDBC_PORT"));
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
	private int prepareConnWL() {
		
		if (getDebugMode()) {
			CONSUMER_LOGGER.log(LogLevel.INFO,"Entering prepareConnWL..."); 
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
				CONSUMER_LOGGER.log(LogLevel.INFO,"Properties created: " + propertiesCon.toString());
				
			}
			
			// Open a RMI connection to server
			InitialContext context = new InitialContext(propertiesCon);
			objref = context.lookup(JNDI);
			IOmsServicesRemoteHome AIFservice = (IOmsServicesRemoteHome) PortableRemoteObject.narrow(objref, IOmsServicesRemoteHome.class);
			service = AIFservice.create();
			
			
			
			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.INFO,"Session created.");
			}
			
			return 0;
			
		}catch(Exception e){
			
			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR APM Session initialization FAILED: " + e.toString());
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
		
		boolean commit = true;

		if (getDebugMode()) {
			CONSUMER_LOGGER.log(LogLevel.DEBUG, "Entering execProcess");			
		}
		
		 //Open WL connection through RMI
		if (prepareConnWL() < 0) {
			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR WL connection failed. Exiting...");				
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
      		MainClass.openDBConnection("LAUNCHORDER", getM_credential());
      		if (MainClass.oraConexionPC == null || MainClass.oraConexionOMS == null) {
      			// An error occurred. Exit method 
				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR checking DB connections. Exiting");	
				}
    			return out;
      		}


      		// Fill the input object: m_input
      		// ApplicationContext 
      		m_input.setAppContext(getInputAppContext());
      		
      		// OrderingContext
      		m_input.setOrderContext(getInputOrderingContext());
      		
      		// StartOrderInput
      		m_input.setOrder(getStartOrderInput(MainClass.oraConexionOMS, getStrIDContract()));
      		
      		// MaksInfo
      		m_input.setMask(getInputMaskInfo());

      		//Call the AIF Service
      		StartOrderOutput output = service.startOrder(m_input.getAppContext(), m_input.getOrderContext(), m_input.getOrder(), m_input.getMask());
      		
      		if (output != null) {
      			out.setM_order(output);
					
      		}
		} catch (Exception e) {
			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR creating connection to WL: " + e.toString());
			}
			return out;
			
		} finally {
			
			try {
				// cerramos las conexiones
				MainClass.closeDBConnection();
				
			}catch (Exception e1) {
				if (getDebugMode()) {
					CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR closing db connections: " + e1.toString());

				}
				return out;
			}
		}
		
		return out;
			
	}	

	/**
	 * Initialize ApplicationContext object
	 * @return ApplicationContext
	 */
	private ApplicationContext getInputAppContext() {
  		ApplicationContext ctx = new ApplicationContext();  		
  		ctx.setFormatLocale(MainClass.app.getGlobalSession().getLocale());
  		return ctx;
	}
	
	/**
	 * Initialize OrderingContext
	 * @return ApplicationContext
	 */
	private OrderingContext getInputOrderingContext() {
        OrderingContext ordCtx = new OrderingContext();
        ordCtx.setLocale(MainClass.app.getGlobalSession().getLocale());
        ordCtx.setSecurityToken(MainClass.app.getGlobalSession().getAsmTicket());
	
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
	private StartOrderInput getStartOrderInput(Connection conn, String orderId) {
		
		StartOrderInput sti = new StartOrderInput();

		CallableStatement sqlQuery = null;
		ResultSet result = null;
		
		if (getDebugMode()) {
			CONSUMER_LOGGER.log(LogLevel.DEBUG, "Getting StartOrderInput details: " + orderId);				
		}

		try{
			
			if (conn == null) {
				MainClass.openDBConnection("LAUNCHORDER", getM_credential()); // open session
			}

			sqlQuery = (CallableStatement) conn.prepareStatement(strQueryOmsOrder.replace("%1", orderId));
			result = sqlQuery.executeQuery();
			
			if (result.getFetchSize() > 0) {			

				OrderActionData[] oad = null;
				int iConta = 0;
				
				while (result.next()) {
					
					// DynamicAttribute
					// -------------------------
					DynamicAttribute dynAtr = new DynamicAttribute();
					String strDynamic = result.getString(5);
					
					if (!"".equals(strDynamic) && strDynamic != null) {
						
						String[] values = strDynamic.split("=");
						if (!"".equals(values[0]) && values[0] !=null) {
							dynAtr.setName(values[0]);
							dynAtr.setValue(values[1]);
						}
					}
					
					DynamicAttribute[] dynAtrHolder = null;
					dynAtrHolder[0] = dynAtr;
					
					//OrderAction
					// -------------------------
					OrderAction oaInfo = new OrderAction();					
					OrderActionID oaID = new OrderActionID();

					oaID.setOrderActionID(result.getString(1));
					oaInfo.setOrderActionID(oaID);

					//SubscriptionGroupID
					// -------------------------
					SubscriptionGroupID sgID = new SubscriptionGroupID();
					sgID.setID(result.getInt(23));		
					
					//OrganizationID
					// -------------------------
					OrganizationID oID = new OrganizationID();

					//OrderActionDetails
					// -------------------------
					OrderActionDetails oaDet = new OrderActionDetails();
					
					oaDet.setActionType(new OrderActionTypeRVT((result.getString(5))));
					oaDet.setStatus(new OrderActionStatusRVT(result.getString(4)));
					oaDet.setOriginator(result.getString(21));
					oaDet.setCurrentOwner(result.getString( 21));
					oaDet.setApplicationDate(result.getDate(16));
					oaDet.setServiceRequireDate(result.getDate(14));
					oaDet.setDueDate(result.getDate(6));
					oaDet.setSalesChannel(new SalesChannelRVT(result.getString(27)));
					oaDet.setCancelProcess(false);
					oaDet.setExternalOrderActionID(result.getString(25));
					oaDet.setCustomerOrderActionID(result.getString(31));
					oaDet.setSubscriptionGroupID(sgID);
					oaDet.setAmendProcess(false);
					oaDet.setCancelProcess(false);	
					oaDet.setOrganizationID(oID); 
					
					//OrderActionUserAction
					// -------------------------
					OrderActionUserAction[] ouAct = null;
					OrderActionUserActionRVT oauAct = new OrderActionUserActionRVT();					
					ouAct[0].setAction(oauAct);

					//OrderID
					// -------------------------
					OrderID orderID = new OrderID();
					orderID.setOrderID(orderId);

					//OrderModeRVT
					// -------------------------
					OrderModeRVT OrderMode = new OrderModeRVT(result.getString(2));

					//OrderStatus
					// -------------------------
					OrderStatusRVT orderStatus = new OrderStatusRVT(result.getString(5));

					//OrderHeader
					// -------------------------
					OrderHeader oh = new OrderHeader();
					
					oh.setOrderID(orderID);
					oh.setOrderMode(OrderMode);
					oh.setOrderStatus(orderStatus);
					oh.setApplicationDate(result.getDate(13));					
					oh.setServiceRequiredDate(result.getDate(11));
					
					oh.setSalesChannel(this.inputOrder.getOrderDetails().getSalesChannel());
					oh.setExpiryDate(this.inputOrder.getOrderDetails().getExpiryDate());
					oh.setCustomerOrderID(this.inputOrder.getOrderDetails().getCustomerOrderID());
					oh.setExternalOrderID(this.inputOrder.getOrderDetails().getExternalOrderID());
					oh.setAvailableUserActions(this.inputOrder.getAvailableUserActions());
					oh.setSalesChannel(this.inputOrder.getOrderDetails().getCurrentSalesChannel());
					oh.setLocked(true);
					oh.setAnonymous(false);					
					oh.setOrderRetrievalCriteria(dynAtrHolder);
					oh.setCustomerAgreedToPayDeposit(false);

					// OrderAction
					// -------------------------
					oaInfo.setOrderActionID(oaID);
					oaInfo.setOrderActionDetails(oaDet);
					oaInfo.setAvailableUserActions(ouAct);
					oaInfo.setOrderHeader(oh);

					// OrderActionData
					oad[iConta].setOrderActionInfo(oaInfo);
					oad[iConta].setDynamicAttributes(dynAtrHolder);
					iConta++;
					
				}
				
				// StartOrderInput
	      		sti.setOrder(this.inputOrder);
	      		sti.setOrderActionsData(oad);
	      		sti.setConfirmationChecksApproved(true);
	      		sti.setMarkOrderAsSaved(true);
				
			}
			
		}catch (Exception e){
			if (getDebugMode()) {
				CONSUMER_LOGGER.log(LogLevel.SEVERE, "ERROR getting StartOrderInput details: " + e.toString());	
			}
			
			return null;
			
			
			
		}finally {
	    	try {
		    	result.close();
		    	sqlQuery.close();
		    	
	    	}catch(Exception e) {}
			
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

}
