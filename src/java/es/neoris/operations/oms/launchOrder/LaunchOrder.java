package es.neoris.operations.oms.launchOrder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.amdocs.cih.common.datatypes.DynamicAttribute;
import com.amdocs.cih.common.datatypes.OrderActionUserAction;
import com.amdocs.cih.common.datatypes.OrderUserAction;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemote;
import com.amdocs.cih.services.oms.interfaces.IOmsServicesRemoteHome;
import com.amdocs.cih.services.oms.lib.StartOrderInput;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionStatusRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionTypeRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderActionUserActionRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderModeRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderStatusRVT;
import com.amdocs.cih.services.oms.rvt.domain.OrderUserActionRVT;
import com.amdocs.cih.services.oms.rvt.referencetable.SalesChannelRVT;
import com.amdocs.cih.services.order.lib.Order;
import com.amdocs.cih.services.order.lib.OrderHeader;
import com.amdocs.cih.services.orderaction.lib.OrderAction;
import com.amdocs.cih.services.orderaction.lib.OrderActionData;
import com.amdocs.cih.services.orderaction.lib.OrderActionDetails;
import com.amdocs.cih.services.orderaction.lib.OrderActionID;
import com.amdocs.cih.services.party.lib.OrganizationID;
import com.amdocs.cih.services.subscription.lib.SubscriptionGroupID;
import com.amdocs.svcparams.IOmsServicesStartOrderInputs;
import com.amdocs.svcparams.IOmsServicesStartOrderResults;

import es.neoris.operations.BaseAIF;

/**
 * @author Neoris
 *
 */
public class LaunchOrder 
extends es.neoris.operations.BaseAIF 
{
	// Logger
	//static final Logger CONSUMER_LOGGER = Logger.getLogger("es.neoris.operations.oms.launchorder.LaunchOrder");
	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/oms/launchorder";
	static final String sNombreFich = "launchorder.properties";
	static final String sRutaIni = "res/";

	// Properties for WL connection
	protected static Boolean debugMode = false;
	private HashMap<String, String> connectionProp = new HashMap<String, String>();
	private static HashMap<String, String> m_credential = new HashMap<String, String>();
	
	// Properties for WL connection
	private static final String JNDI = "/omsserver_weblogic/com/amdocs/cih/services/oms/interfaces/IOmsServicesRemote";
	private static IOmsServicesRemote service = null;


	// Input variables from main classes
	private String m_strIDContract;
	private String m_strProcessName;
	private String m_strVersion;
	private String m_strObjidLanzado;	
	private Order m_order;


	// Variables to call service
	private IOmsServicesStartOrderInputs m_input;
	private IOmsServicesStartOrderResults m_output;

	//Queries
	private String strQueryOmsOrder = "SELECT T.CTDB_LAST_UPDATOR, T.ORDER_MODE, T.GROUP_ID, T.ROOT_CUSTOMER_ID, T.STATUS, T.OPPORTUNITY_ID, T.CURRENT_SALES_CHANNEL, T.DEALER_CODE, T.ADDRESS_ID, T.EXT_REF_NUM" // 10
			 + ", SERVICE_REQ_DATE, CREATION_DATE, APPLICATION_DATE, PROP_EXPIRY_DATE, CUST_ORDER_REF, CONTACT_ID, CUSTOMER_ID, DEPOSIT_ID, ORDER_UNIT_ID" // 20
			 + ", ORDER_UNIT_TYPE, PRIORITY, PROP_EXPIRY_DATE, RECONTACT_IN_MONTH, RECONTACT_IN_YEAR, RECONTACT_PERIOD, SOURCE_ORDER" // 30
			 + " FROM TBORDER T"
			 + " WHERE T.REFERENCE_NUMBER = '%1'";

	
	/**
	 * Default no-operative constructor
	 */
	public LaunchOrder() {
		// no-op constructor
	}
	

	/**
	 * Reads .properties file to prepare a connection to WL
	 * @param getConf
	 * @throws Exception
	 */
	public LaunchOrder(Boolean getConf) 
		throws Exception { 
		
		if (getConf) {
			//Get info from .properties files 
			try {
				Properties properties = BaseAIF.getProperties(sRutaIni, sNombreFich);
				
				connectionProp.put("WLS_URL", properties.getProperty("WLS_URL"));
				connectionProp.put("WLS_USER", properties.getProperty("WLS_USER"));
				connectionProp.put("WLS_PASS", properties.getProperty("WLS_PASS"));
				connectionProp.put("WLS_DS_OMS", properties.getProperty("WLS_DS_OMS"));
				connectionProp.put("WLS_DS_PC", properties.getProperty("WLS_DS_PC"));
				connectionProp.put("DEBUG", properties.getProperty("DEBUG"));

				
				// Loading data for db connection
				m_credential.put("DB_USER_OMS", properties.getProperty("DB_USER_OMS"));
				m_credential.put("DB_PASS_OMS", properties.getProperty("DB_PASS_OMS"));
				m_credential.put("DB_USER_PC", properties.getProperty("DB_USER_PC"));
				m_credential.put("DB_PASS_PC", properties.getProperty("DB_PASS_PC"));
				m_credential.put("DB_NAME", properties.getProperty("DB"));
				m_credential.put("JDBC_DB", properties.getProperty("DB_JDBC"));
				m_credential.put("JDBC_PORT", properties.getProperty("DB_PORT"));
				
			
				if ((!"".equals(properties.getProperty("DEBUG"))) && "1".equals(properties.getProperty("DEBUG"))) { 
					LaunchOrder.debugMode = true;				
			
					System.out.println( "URL:" + properties.getProperty("WLS_URL"));
					System.out.println( "USER:" + properties.getProperty("WLS_USER"));
					System.out.println( "DATASOURCE OMS:" + properties.getProperty("WLS_DS_OMS"));
					System.out.println( "DATASOURCE PC:" + properties.getProperty("WLS_DS_PC"));
					System.out.println( "DB:" + properties.getProperty("DB"));			
					System.out.println( "USER_OMS:" + m_credential.get("DB_USER_OMS"));
					System.out.println( "USER_PC:" + m_credential.get("DB_USER_PC"));
					System.out.println( "JDBC:" + m_credential.get("JDBC_DB"));
					System.out.println( "PORT:" + m_credential.get("JDBC_PORT"));
				
				}
				
			}
			catch (Exception e) {
				
				System.out.println("Error getting " + sNombreFich + ": " + e.getLocalizedMessage());
				throw new Exception("Error reading .properties file");
			}
		}
		
	}
	

	
	/**
	 * Generate and execute process related to TBORDER 
	 * @return 0 --> OK
	 *        -1 --> ERROR
	 */
	public IOmsServicesStartOrderResults execProc() {
		
		if (LaunchOrder.debugMode) {
			System.out.println("Entering execProcess");			
		}

		
		// Calculate mandatory info to generate input object 
		try {
			
			//Open WL connection through RMI
			service = ((IOmsServicesRemoteHome) BaseAIF.createEJBObject(connectionProp, JNDI, LaunchOrder.debugMode)).create();

      		// Fill the input object: m_input
      		m_input.setApplicationContext(BaseAIF.appContext);
      		m_input.setOrderingContext(BaseAIF.orderingContext);
      		m_input.setMaskInfo(BaseAIF.maskInfo);
      		m_input.setStartOrderInput(getStartOrderInput(getM_order()));

      		//Call the AIF Service
      		m_output.setStartOrderOutput(service.startOrder(m_input.getApplicationContext(), m_input.getOrderingContext(), m_input.getStartOrderInput(), m_input.getMaskInfo()));

      		if (m_output.getStartOrderOutput().getOrderID() == null) {
				if (LaunchOrder.debugMode) System.out.println("ERROR launching order : " + m_input.getStartOrderInput().getOrder().getOrderID().getOrderID());
				
      		}
      		
		} catch (Exception e) {
			if (LaunchOrder.debugMode) {
				System.out.println("ERROR creating connection to WL: " + e.toString());
			}
			return m_output;
		}
		
		return m_output;
			
	}	

	
	public int writeResult() {
		
		//Guardamos el objid recuperado en un fichero de texto
		BufferedWriter bw = null;
		try {
			
			File fichero = new File(sDirEject + "/out", getStrIDContract() + "_" + getStrVersion());
			bw = new BufferedWriter(new FileWriter(fichero));
			//bw.write(output.getM_order().getOrderID().toString());

		} catch (IOException e) {
			if (LaunchOrder.debugMode) {
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
	 * Fill the StartOrderInput object
	 * @param conn  	--> db connection. If closed, open it
	 * @param orderId   --> order id when the process will be attached
	 * @return StartOrderInput
	 */
	private StartOrderInput getStartOrderInput(Order order) {
		
		StartOrderInput sti = new StartOrderInput();
		
		if (LaunchOrder.debugMode) {
			System.out.println("Getting StartOrderInput details: " + order.getOrderID().getOrderID());				
		}

		try{
			// StartOrderInput
			sti.setOrder(getM_order());
			sti.setOrderActionsData(getOrderActionData(getM_order()));
			sti.setMarkOrderAsSaved(true);
			sti.setConfirmationChecksApproved(true);
			
		}catch (Exception e){
			if (LaunchOrder.debugMode) {
				System.out.println("ERROR getting StartOrderInput details: " + e.toString());	
			}
			
			return null;
			
		}
		
		return sti;
	}			
	

	
	private OrderActionData[] getOrderActionData(Order order) {
		
		Map<String, Connection> conn = new HashMap<String, Connection>();
		OrderActionData[] oad = new OrderActionData[1];
		
		CallableStatement sqlQuery = null;
		ResultSet result = null;
		
		if (LaunchOrder.debugMode) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Getting OrderActionData details: " + order.getOrderID().getOrderID());
		}
		
		try{
			
			conn = BaseAIF.openDBConnection(LaunchOrder.class.getName().toUpperCase(), m_credential, LaunchOrder.debugMode);

			sqlQuery = (CallableStatement) ((Connection) conn.get("OMS")).prepareStatement(strQueryOmsOrder.replace("%1", order.getOrderID().getOrderID()));
			result = sqlQuery.executeQuery();

			if (result.getFetchSize() > 0) {			

				int iConta = 0;
				while (result.next()) {

					DynamicAttribute[] dynAtr = new DynamicAttribute[10];
					String strDynamic = result.getString(5);
					
					if (!"".equals(strDynamic) && strDynamic != null) {
						
						String[] values = strDynamic.split("=");
						if (!"".equals(values[0]) && values[0] !=null) {
							dynAtr[0].setName(values[0]);
							dynAtr[0].setValue(values[1]);
						}
					}
					
					//OrderAction
					OrderAction oaInfo = new OrderAction();
					
					OrderActionID oaID = new OrderActionID();
					oaID.setOrderActionID(result.getString(1));
					oaInfo.setOrderActionID(oaID);

					//OrderActionDetails
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
					
					SubscriptionGroupID sgID = new SubscriptionGroupID();
					sgID.setID(result.getInt(23));							
					oaDet.setSubscriptionGroupID(sgID);
					oaDet.setAmendProcess(false);
					oaDet.setCancelProcess(false);	
					
					OrganizationID oID = new OrganizationID();
					oID.setId(null); // TODO Find the correct value...
					oaDet.setOrganizationID(oID); 
					
					//OrderActionUserAction
					OrderActionUserAction[] ouAct = new OrderActionUserAction[10];
					ouAct[0].setAction(new OrderActionUserActionRVT());

					//SalesChannelRVT 
					SalesChannelRVT sc = new SalesChannelRVT(result.getString(7));
					
					//OrderUserAction
					OrderUserActionRVT ouaRVT = null;
					OrderUserAction[] oua = new OrderUserAction[1];
					oua[0].setAction(ouaRVT);
					oua[0].setAllowed(true);
					oua[0].setRelinquishChannels(0, sc);

					//OrderHeader
					OrderHeader oh = new OrderHeader();

					oh.setOrderID(order.getOrderID());
					oh.setOrderMode(new OrderModeRVT(result.getString(2)));
					oh.setOrderStatus(new OrderStatusRVT(result.getString(5)));
					oh.setApplicationDate(result.getDate(13));
					oh.setServiceRequiredDate(result.getDate(11));
					oh.setSalesChannel(sc);
					oh.setExpiryDate(result.getDate(14));
					oh.setCustomerOrderID(result.getString(17));
					oh.setExternalOrderID(result.getString(10));
					oh.setAvailableUserActions(oua);
					oh.setSalesChannel(new SalesChannelRVT(result.getString(7)));
					oh.setLocked(true);
					oh.setAnonymous(false);
					
					oh.setOrderRetrievalCriteria(dynAtr);
					oh.setCustomerAgreedToPayDeposit(false);

					// Fill OrderAction
					oaInfo.setOrderActionID(oaID);
					oaInfo.setOrderActionDetails(oaDet);
					oaInfo.setAvailableUserActions(ouAct);
					oaInfo.setOrderHeader(oh);

					// OrderActionData
					oad[iConta].setOrderActionInfo(oaInfo);
					oad[iConta].setDynamicAttributes(dynAtr);
		      		
		      		iConta++;
					
				}
			}			
			
		}catch (Exception e){
			if (LaunchOrder.debugMode) {
				System.out.println("------------------------------------------------------------------------------------------");
				System.out.println("ERROR getting OrderActionData details: " + e.toString());
			}
			
			return oad;
		
		}finally {
	    	try {
		    	result.close();
		    	sqlQuery.close();
		    	
	    	}catch(Exception e) {}
			
		}

		return oad;
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
		LaunchOrder.service = service;
	}

	public Order getM_order() {
		return m_order;
	}


	public void setM_order(Order m_order) {
		this.m_order = m_order;
	}

}
