package es.neoris.operations.oms.launchorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

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
import com.amdocs.cih.services.order.lib.OrderDetails;
import com.amdocs.cih.services.order.lib.OrderHeader;
import com.amdocs.cih.services.order.lib.OrderID;
import com.amdocs.cih.services.orderaction.lib.OrderAction;
import com.amdocs.cih.services.orderaction.lib.OrderActionData;
import com.amdocs.cih.services.orderaction.lib.OrderActionDetails;
import com.amdocs.cih.services.orderaction.lib.OrderActionID;
import com.amdocs.cih.services.party.lib.OrganizationID;
import com.amdocs.cih.services.party.lib.PersonHeader;
import com.amdocs.cih.services.party.lib.PersonID;
import com.amdocs.cih.services.subscription.lib.SubscriptionGroupID;
import com.clarify.cbo.SqlExec;

import amdocs.core.logging.Logger;
import amdocs.epi.datamanager.DataManagerCls;
import amdocs.epi.lock.DistributedLockManager;
import amdocs.epi.lock.LockManagerFactory;
import amdocs.epi.proxy.session.bean.ProxySession;
import amdocs.epi.proxy.session.bean.ProxySessionHome;
import amdocs.epi.session.EpiSessionContext;
import amdocs.epi.session.EpiSessionId;
import amdocs.epi.util.EpiCollections;
import amdocs.epi.util.IdGen;
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
import amdocs.oms.infra.IlSession;
import amdocs.oms.infra.domains.ActionTypeTP;
import amdocs.oms.infra.domains.BooleanValTP;
import amdocs.oms.infra.domains.ConfigurationTP;
import amdocs.oms.infra.domains.LanguageCodeTP;
import amdocs.oms.infra.domains.MilestoneStatusTP;
import amdocs.oms.infra.domains.OrderActionParentRelationTP;
import amdocs.oms.infra.domains.OrderActionStatusTP;
import amdocs.oms.infra.domains.OrderStatusTP;
import amdocs.oms.infra.domains.OrderUnitTypeTP;
import amdocs.oms.infra.domains.RecontactPeriodTP;
import amdocs.oms.infra.domains.ActionTypeTP.ActionType;
import amdocs.oms.infra.domains.BooleanValTP.BooleanVal;
import amdocs.oms.infra.domains.LanguageCodeTP.LanguageCode;
import amdocs.oms.infra.domains.OrderActionParentRelationTP.OrderActionParentRelation;
import amdocs.oms.infra.domains.OrderActionStatusTP.OrderActionStatus;
import amdocs.oms.infra.domains.OrderStatusTP.OrderStatus;
import amdocs.oms.infra.domains.OrderUnitTypeTP.OrderUnitType;
import amdocs.oms.infra.domains.RecontactPeriodTP.RecontactPeriod;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP;
import amdocs.oms.infra.domains.dynamic.DynOrderModeTP.DynOrderMode;
import amdocs.oms.ocs.InitialProcessService;
import amdocs.oms.oem.OmOrder;
import amdocs.oms.oem.OmOrderAction;
import amdocs.oms.oem.OmOrderHolder;
import amdocs.oms.osmancust.OmOrderAddition;
import amdocs.oms.pc.PcProcessDefinition;
import amdocs.oms.pc.PcProcessDefinitionAug;
import amdocs.oms.rootset.AlRootSet;
import amdocs.oms.rootset.RootSetBase;
import es.neoris.operations.MainClass;
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
import amdocs.core.logging.LogLevel;

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
	private static String strJdbc_DB = null;
	private static String strJdbc_Port = null;

	
	// Properties for WL connection
	private static final String JNDI = "com/amdocs/cih/services/oms/interfaces/IOmsServicesRemote";
	private final static String initialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	private Object objref = null;
	private EpiSessionId tPooledId = null;
	private ProcMgrSession procSess = null;
	private ProcMgrSessionHome procMgrLocal = null;
	private ProxySession pSession = null; 
	private IlSession session = null;
	
	
	// Input variables from main classes
	private String m_strIDContract;
	private String m_strProcessName;
	private String m_strVersion;
	private String m_strObjidLanzado;	

	// Variables to call service
	private InputParamsLaunchOrder m_input;
	private OutputParamsLaunchOrder m_output;
	
	/**
	 * Default no-op constructor
	 */
	public LaunchOrder() {
		/* default no-op constructor */
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

		// Init class properties
		setStrIDContract(strIDContract);
		setStrProcessName(strProcessName);
		setStrVersion(strVersion);
		
		//Get info from .properties files 
		try {
			LaunchOrder.getConfiguration();
		}
		catch (Exception e) {
			CONSUMER_LOGGER.log(LogLevel.SEVERE, "Error getting " + this.sNombreFich + ": " + e.getLocalizedMessage());
			throw new Exception("Error al leer el fichero .properties");
			
		}
		
	}
	

	/**
	 * Gets service configuration from .properties file
	 * @throws IOException
	 */
	private static void getConfiguration() 
			throws IOException, Exception {
		
		try {
			//Get enviroment values from .propertis
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
	
			strDBName = properties.getProperty("DB");
			// Loading credentials
			m_credential.put("DB_USER_OMS", properties.getProperty("DB_USER_OMS"));
			m_credential.put("DB_PASS_OMS", properties.getProperty("DB_PASS_OMS"));
			m_credential.put("DB_USER_PC", properties.getProperty("DB_USER_PC"));
			m_credential.put("DB_PASS_PC", properties.getProperty("DB_PASS_PC"));

			strJdbc_DB = properties.getProperty("DB_JDBC");
			strJdbc_Port = properties.getProperty("DB_PORT");

			
			if ((!"".equals(strDebug)) && "1".equals(strDebug)) {
				
				setDebugMode(true);
				
				CONSUMER_LOGGER.log(LogLevel.INFO, "URL:" + strURL_WLS);
				CONSUMER_LOGGER.log(LogLevel.INFO, "USER:" + strUser_WLS);
				CONSUMER_LOGGER.log(LogLevel.INFO, "DATASOURCE OMS:" + strDS_WLS_OMS);
				CONSUMER_LOGGER.log(LogLevel.INFO, "DATASOURCE PC:" + strDS_WLS_PC);
				CONSUMER_LOGGER.log(LogLevel.INFO, "DB:" + strDBName);			
				CONSUMER_LOGGER.log(LogLevel.INFO, "USER_OMS:" + m_credential.get("DB_USER_OMS"));
				CONSUMER_LOGGER.log(LogLevel.INFO, "USER_PC:" + m_credential.get("DB_USER_PC"));
				CONSUMER_LOGGER.log(LogLevel.INFO, "JDBC:" + strJdbc_DB);
				CONSUMER_LOGGER.log(LogLevel.INFO, "PORT:" + strJdbc_Port);
			}
		}			
		catch(Exception e) {
			
			CONSUMER_LOGGER.log(LogLevel.SEVERE,"ERROR Opening .properties FAILED: " + e.toString()); 
			throw new Exception("Error loading .properties file. Exiting...");

		}

	}		
	


	/**
	 * Prepared WL connection to execute the opertaion
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
			IOmsServicesRemote AIFservice = (IOmsServicesRemote) PortableRemoteObject.narrow(objref, IOmsServicesRemote.class);
			
			
			
			/*
			objref = context.lookup("/omsserver_weblogic/amdocs/bpm/ejb/ProcMgrSession");
			procMgrLocal = (ProcMgrSessionHome) PortableRemoteObject.narrow(objref, ProcMgrSessionHome.class);
			procSess = procMgrLocal.create();
			*/
			/*
			proxy = (ProxySessionHome) PortableRemoteObject.narrow(objref, ProxySessionHome.class);
			
			pSession = proxy.create();
			tPooledId = pSession.createSession("");
			 */
			
			/*
			objref = context.lookup("/omsserver_weblogic/amdocs/bpm/ejb/ProcMgrSession");
			procMgrLocal = (ProcMgrSessionHome) PortableRemoteObject.narrow(objref, ProcMgrSessionHome.class);
			procSess = procMgrLocal.create();
			*/
			//tPooledId = procSess.createProcMgrSession(IdGen.uniqueId());
			

			if (getDebugMode()) {
				System.out.println("Session created: ");
				System.out.println(tPooledId.toString());
			}
			
			return 0;
			
		}catch(Exception e){
			
			if (getDebugMode()) {
				System.out.println("------------------------------------------------------------------------------------------");
				System.out.println("ERROR APM Session initialization FAILED: " + e.toString());
			}
			
			if (e.toString().contains("javax.naming.NameNotFoundException")) {
				
			}
			return -1;
		}
		
	}
		

	/**
	 * Lanzamiento proceso. Genera todos los objetos necesarios recuperandolos de BBDD
	 * @return 0 --> Lanzamiento OK
	 *        -1 --> Error en el lanzamiento
	 */
	private int execProcess() {

		boolean commit = true;

		if (getDebugMode()) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Entering execProcess ");
		}
		
		 //PARA LAS PRUEBAS CON WL CAIDO
		if (prepareConnWL() < 0) {

			if (getDebugMode()) {
				System.out.println("ERROR WL connection failed. Exiting...");
			}

			return -1;
		}
		
		 
		
		// Generamos los objetos necesarios para lanzar el proceso asociado a la orden
		try {
	          String caseId = null;
	          StringHolder caseIdH = new StringHolder(caseId);
	          	  
	          session = (IlSession) EpiSessionContext.findSession(tPooledId);	     
	          	          
	          session.startTransaction();  // Llamamos asi, aunque es static, para garantizar que se use la sesion a la que nos hemos conectado
			  
			  if (getDebugMode()) {
				System.out.println("Transaction started: ");
				System.out.println(session.toString());
			  }

	          
      		// generamos las conexiones
      		MainClass.openDBConnection("BOTH");
      		//if (("".equals(clfySessionOms.getSessionTag())) && ("".equals(clfySessionPC.getSessionTag()))) {
      		if (oraConexionPC == null || oraConexionOMS == null) {
      			// Si no hemos podido abrir las sesiones, salimos del procedimiento. 
				if (getDebugMode()) {
					System.out.println("ERROR checking DB connections. Exiting");
				}
    			return -1;
      		}
      		
			//Recuperamos la info del proceso a lanzar
      		//this.setProcessDef(getProcessDetails(clfySqlExecPC, getStrProcessName()));
      		this.setProcessDef(getProcessDetails(oraConexionPC, getStrProcessName()));  
      		if (this.getProcessDef() == null) {
				if (getDebugMode()) {
					System.out.println("------------------------------------------------------------------------------------------");
					System.out.println("ERROR cannot get process definition. Exiting");
				}

      			return -1;
      		}	

      		//recuperamos los valores de la orden a partir del id
			try {
				
				 if (getDebugMode()) {
					System.out.println("Creating order objects... ");
				 }
				
	      		//generamos el objeto OmOrder
	      		//this.order = getOmOrderDetails(clfySqlExecOms, getStrIDContract());
	      		this.order = getOmOrderDetails(oraConexionOMS, getStrIDContract());
	      		this.inputOrder = getInputOrderDetails(this.order);
	      		
	      		//this.startOrderInput = getStartOrderInput(clfySqlExecOms, this.order.getsourceOrder());
	      		this.startOrderInput = getStartOrderInput(oraConexionOMS, this.order.getsourceOrder());

				 if (getDebugMode()) {
					System.out.println("Order objects created");
				 }

				String custID = this.order.getrootCustomerId();
						
				//Nos quedamos con la ultima OA (se obtienen ordenadas)
				ListSet oma = this.order.getOMORDER_ACTION_CHILDs();					
				OmOrderAction existingOA = (OmOrderAction) oma.get(oma.size());
						
				//Recorremos los distintos Order Action que se hayan recuperado
				for (int i = 0; i < startOrderInput.getOrderActionsData().length; i++){

					if (getDebugMode()) {
						System.out.println("Loop over order actions");
					}

					OrderActionData oaData = this.startOrderInput.getOrderActionsData()[i];
					OrderActionDetails oaDetails = oaData.getOrderActionInfo().getOrderActionDetails();
					String actionType = oaDetails.getActionType().getValueAsString();
					ActionTypeTP.ActionType oaType = (ActionTypeTP.ActionType)ActionTypeTP.def.findByCodeNoAutoCreate(actionType);
							
					String lineOfBusiness = null;
					if (oaDetails.getLineOfBusiness() != null)
						lineOfBusiness = oaDetails.getLineOfBusiness().getValueAsString();
							
					Date serviceRequiredDate = oaDetails.getServiceRequireDate();

					if (getDebugMode()) {
						System.out.println("Creating creationInfo object");
					}

					InitialProcessService.OrderActionCreationInfo creationInfo = new InitialProcessService.OrderActionCreationInfo(oaType, this.order.getcurrentSalesChannel(), lineOfBusiness, serviceRequiredDate);

					DynamicAttribute[] dynamicAttrs = oaData.getDynamicAttributes();
					if (dynamicAttrs != null) 
						creationInfo.dynamicAttributes = CihOrderActionFilter.createDynamicAttributesMap(dynamicAttrs);


					if (getDebugMode()) {
						System.out.println("Starting and executing process");
					}

					//Lanzamos el proceso
					//caseIdH = startProcess(clfySqlExecOms, creationInfo, custID, existingOA, oaData);
					caseIdH = startProcess(oraConexionOMS, creationInfo, custID, existingOA, oaData);
							
					// y recuperamos el caseid generado
					if (caseIdH.value != null) {
								
						caseId = caseIdH.value;
						this.setStrObjidLanzado(caseId);
								
						if (getDebugMode()) {
							System.out.println("Launched process id: " + caseId);
						}

						//Guardamos el objid recuperado en un fichero de texto
						BufferedWriter bw = null;
						try {
							File fichero = new File(sRutaIni + "/out", this.getStrIDContract() + "_" + this.getStrVersion());
							bw = new BufferedWriter(new FileWriter(fichero));
							bw.write(this.getStrObjidLanzado());

						} catch (IOException e) {
							if (getDebugMode()) {
								System.out.println("ERROR handling output file : " + e.toString());
							}
								
						} finally {
							try {
								bw.close();
							} catch (Exception e) {}
						}
					}
				}
			} catch (Exception e) {
	
				if (getDebugMode()) {
					System.out.println("ERROR generic exception creating session objects" );
					System.out.println(e.toString());
				}
				return -1;
			}
				
				
		} catch (Exception e) {
			if (getDebugMode()) {
				System.out.println("ERROR al generar los objetos de conexion al APM");
				System.out.println(e.toString());
			}
			return -1;
			
		} finally {
			
			try {
				// cerramos las conexiones
				MainClass.closeDBConnection();
				
	      		
				/* PARA PRUEBAS CON WL CAIDO
				session.endTransaction(commit);
				clfyAppOms.release();
				clfyAppPC.release();
				session.close();
				*/
				
			}catch (Exception e1) {
				if (getDebugMode()) {
					System.out.println("ERROR closing connections");
					System.out.println(e1.toString());
				}
				return -1;
			}
		}
		
		return 0;
			
	}	

	/**
	 * Inicia los subprocesos asociados a un proceso
	 * @param processInstance
	 * @param traversal
	 * @param createInfo
	 * @return 0 -> OK
	 *        <0 -> Error
	 */
	private int initSubProcesses(BaseProcInst processInstance, ProcInstTraversal traversal, ProcessCreateInfo createInfo)
	{

		int iControl = 0;

		if (getDebugMode()) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Starting subprocesses");
		}
		

		List subProcesses = processInstance.getSubprocInsts(traversal);
		
		for (Iterator i = subProcesses.iterator(); i.hasNext();)
		{
			BaseProcInst subProcInst = (BaseProcInst)i.next();
			
			try {
				OmOrderAction oa = (OmOrderAction) createInfo.getProcessObject();
				RootSetBase rsb = (RootSetBase) subProcInst.getRootContext();
				AlRootSet mr = (AlRootSet) subProcInst.getRootProcInst().getRootContext();
			
				rsb.setmainRoot(mr);
				rsb.setorderAction(oa);
				if (rsb instanceof AlRootSet) 
					((AlRootSet) rsb).setCaseDoneVal("N");
			
			}catch (Exception e) {
				return -1;
			}
			
			// Llamamos a la misma funcion con el valor de los subprocesos
			iControl = initSubProcesses(subProcInst, traversal, createInfo);
			if (iControl != 0) 
				return -1;
			
		}
		return 0;
	}

	/**
	 * Lanza el proceso asociado a una order action
	 * @param sqlQuery
	 * @param creationInfo
	 * @param customerID
	 * @param existingOA
	 * @param orderActionData
	 * @return
	 * @throws OmsDataNotFoundException
	 * @throws OmsCreateRequestFailedException
	 * @throws OmsInvalidImplementationException
	 * @throws OmsInvalidUsageException
	 */
	
	/** @deprecated
	 */
	protected StringHolder startProcess(SqlExec sqlQuery, InitialProcessService.OrderActionCreationInfo creationInfo, String customerID, OmOrderAction existingOA, OrderActionData orderActionData)
		    throws Exception
	{

		String caseId = null;
		StringHolder caseIdH = new StringHolder(caseId);
		
		if (getDebugMode()) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Starting process");
		}
		
		
		try {

			// Generamos la consulta con los datos del proceso a ejecutar
			sqlQuery.execute(strQueryBPM.replace("%1", getStrProcessName()).replace("%2", getStrVersion()));
			
			if (sqlQuery.getRowCount() > 0) {
			
				if (getDebugMode()) {
					System.out.println("Creating objects to launch the process");
				}

				 //createCase(procName, existingOA, true, true, caseIdH)
				 ProcessCreateInfo createInfo = new ProcessCreateInfo();
				 createInfo.setBusinessProcessName(this.getStrProcessName());
				 createInfo.setVersion(getStrVersion());
				 createInfo.setIsInitialProcess();
				 createInfo.setUseLatest(true);
				 createInfo.setFirstInQuantity(false);
				 createInfo.setStart(true);

				if (getDebugMode()) {
					System.out.println("ProcessCreateInfo:" + createInfo.toString());
				}

				 
				 //OmsBpaCase cse = (OmsBpaCase)ActivityManager.getInstance().createProcessInstance(createInfo);
				 BusinessProcess bproc = null;
				 bproc.setId((String)sqlQuery.getValue(1, 1));
				 bproc.setName((String)sqlQuery.getValue(1, 3));				
				 bproc.setStatus(ProcessStatus.valueOf((Integer)sqlQuery.getValue(1, 7)));
				 bproc.setDefinitionVersion((Integer)sqlQuery.getValue(1, 8));
				 bproc.setFocusKind(FocusKind.valueOf((Integer)sqlQuery.getValue(1, 9)));
				 bproc.setFocusType((String)sqlQuery.getValue(1, 10));
				 //bproc.setDynamicInstantiation(true);
				 bproc.setSubclassingPolicyName("default");
				 
				if (getDebugMode()) {
					System.out.println("BusinessProcess:" + bproc.toString());
				}

				 
				 RootCreateInfo info = new RootCreateInfo();
				 info.setMakeBeginStepInstAvailable(false);
				 
				 ProcInstManager piMgr = ProcInstManagerFactory.get();
				 
				if (getDebugMode()) {
						System.out.println("ProcInstManager:" + piMgr.toString());
				}

				 
				 BaseProcInst procInst = null;				 
				 // Lanzamos el proceso
				 procInst = (BaseProcInst) piMgr.createRootProcInst(bproc, null, info);

				 if (procInst != null) {
					if (getDebugMode()) {
						System.out.println("BaseProcInst:" + procInst.toString());
					}
				 
				 
					//Asignamos el proceso a la OA
					existingOA.assignProcessInstanceId(procInst.getId(), true);
					createInfo.setProcessObject(existingOA);
					
					OmsBpaCase caseObj = (OmsBpaCase) procInst;
					if (getDebugMode()) {
						System.out.println("OmsBpaCase:" + caseObj.toString());
					}
					
					caseObj.setLastUpdatedBy(session.getLogicalServerName());
				 
					ProcInstTraversal traversal = piMgr.createProcInstTraversal(procInst);
					if (getDebugMode()) {
						System.out.println("ProcInstTraversal:" + traversal.toString());
					}
				 
					
					if (initSubProcesses(procInst, traversal, createInfo) != 0) {
						if (getDebugMode()) {
							System.out.println("ERROR initializing subprocess");
						}
						return caseIdH;	
					};


					BaseStepInst stepInst = (BaseStepInst) procInst.getBeginStepInst();
					stepInst.setReferenceText(stepInst.getId());
				 
					if (getDebugMode()) {
						System.out.println("BaseStepInst:" + stepInst.toString());
					}
					
					
					if (stepInst.getStep() instanceof MilestoneStep ) {
						Step st = stepInst.getStep();
					 
						Milestone m = null;
						m.setStepInstanceId(stepInst.getId());
						m.setProcessObject(existingOA);
						m.setMilestoneStatus(MilestoneStatusTP.IN);
						m.setMilestoneType(((MilestoneStep) st).getMilestoneType());
						m.setIndex(0);
						m.setConfiguration(existingOA.getConfiguration());
					 
						m.initiateAchievementDate(null);
						m.initiateReachingDate(null);
						m.initiateEarlyDate(null);
						m.setDueDate(null);
					 
						existingOA.addMilestone(m);
						if (getDebugMode()) {
							System.out.println("Milestone:" + m.toString());
						}
						
					}
				 
					try {
						 procInst.startRootProcInst(); 
						 existingOA.setIsProcessStarted(true);
						 
						 caseIdH.value = caseObj.getId();
						 
						 if (getDebugMode()) {
							System.out.println("CaseId generated: " + caseObj.getId());
						}

					}catch (Exception e) {
						 if (getDebugMode()) {
							 System.out.println("ERROR Exception: " + e.toString());
						 }

						 return caseIdH;
					 }
					 
				 }else{

					if (getDebugMode()) {
						System.out.println("ERROR ProcInst not created");
					}

					 
				 }
			}
		}catch(Exception e) {
			 if (getDebugMode()) {
				 System.out.println("ERROR Generic Exception: " + e.toString());
			 }

			 return caseIdH;
		}
		finally {
			sqlQuery.release();
		}
		
		return caseIdH;
	}	


	/** 
	 * Lanza el proceso asociado a una order action
	 * @param conn
	 * @param creationInfo
	 * @param customerID
	 * @param existingOA
	 * @param orderActionData
	 * @return
	 * @throws Exception
	 */
	
	protected StringHolder startProcess(Connection conn, InitialProcessService.OrderActionCreationInfo creationInfo, String customerID, OmOrderAction existingOA, OrderActionData orderActionData)
		    throws Exception
	{

		String caseId = null;
		StringHolder caseIdH = new StringHolder(caseId);

		CallableStatement sqlQuery = null;
		ResultSet result = null; 
		
		if (getDebugMode()) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Starting process");
		}
		
		
		try {

			if (conn == null) {
				MainClass.openDBConnection("OMS"); // abrimos la sesion para PC
			}
			
			// Generamos la consulta con los datos del proceso a ejecutar
			sqlQuery = (CallableStatement) conn.prepareStatement(strQueryBPM.replace("%1", getStrProcessName().replace("%2", getStrVersion())));			
			result = sqlQuery.executeQuery();
			
			if (result.getFetchSize() > 0) {
			
				if (getDebugMode()) {
					System.out.println("Creating objects to launch the process");
				}

				 //createCase(procName, existingOA, true, true, caseIdH)
				 ProcessCreateInfo createInfo = new ProcessCreateInfo();
				 createInfo.setBusinessProcessName(this.getStrProcessName());
				 createInfo.setVersion(getStrVersion());
				 createInfo.setIsInitialProcess();
				 createInfo.setUseLatest(true);
				 createInfo.setFirstInQuantity(false);
				 createInfo.setStart(true);

				if (getDebugMode()) {
					System.out.println("ProcessCreateInfo:" + createInfo.toString());
				}

				 
				 //OmsBpaCase cse = (OmsBpaCase)ActivityManager.getInstance().createProcessInstance(createInfo);
				 BusinessProcess bproc = null;
				 bproc.setId(result.getString(1));
				 bproc.setName(result.getString(3));				
				 bproc.setStatus(ProcessStatus.valueOf(result.getInt(7)));
				 bproc.setDefinitionVersion(result.getInt(8));
				 bproc.setFocusKind(FocusKind.valueOf(result.getInt(9)));
				 bproc.setFocusType(result.getString(10));
				 //bproc.setDynamicInstantiation(true);
				 bproc.setSubclassingPolicyName("default");
				 
				if (getDebugMode()) {
					System.out.println("BusinessProcess:" + bproc.toString());
				}

				 
				 RootCreateInfo info = new RootCreateInfo();
				 info.setMakeBeginStepInstAvailable(false);
				 
				 ProcInstManager piMgr = ProcInstManagerFactory.get();
				 
				if (getDebugMode()) {
						System.out.println("ProcInstManager:" + piMgr.toString());
				}

				 
				 BaseProcInst procInst = null;				 
				 // Lanzamos el proceso
				 procInst = (BaseProcInst) piMgr.createRootProcInst(bproc, null, info);

				 if (procInst != null) {
					if (getDebugMode()) {
						System.out.println("BaseProcInst:" + procInst.toString());
					}
				 
				 
					//Asignamos el proceso a la OA
					existingOA.assignProcessInstanceId(procInst.getId(), true);
					createInfo.setProcessObject(existingOA);
					
					OmsBpaCase caseObj = (OmsBpaCase) procInst;
					if (getDebugMode()) {
						System.out.println("OmsBpaCase:" + caseObj.toString());
					}
					
					caseObj.setLastUpdatedBy(session.getLogicalServerName());
				 
					ProcInstTraversal traversal = piMgr.createProcInstTraversal(procInst);
					if (getDebugMode()) {
						System.out.println("ProcInstTraversal:" + traversal.toString());
					}
				 
					
					if (initSubProcesses(procInst, traversal, createInfo) != 0) {
						if (getDebugMode()) {
							System.out.println("ERROR initializing subprocess");
						}
						return caseIdH;	
					};


					BaseStepInst stepInst = (BaseStepInst) procInst.getBeginStepInst();
					stepInst.setReferenceText(stepInst.getId());
				 
					if (getDebugMode()) {
						System.out.println("BaseStepInst:" + stepInst.toString());
					}
					
					
					if (stepInst.getStep() instanceof MilestoneStep ) {
						Step st = stepInst.getStep();
					 
						Milestone m = null;
						m.setStepInstanceId(stepInst.getId());
						m.setProcessObject(existingOA);
						m.setMilestoneStatus(MilestoneStatusTP.IN);
						m.setMilestoneType(((MilestoneStep) st).getMilestoneType());
						m.setIndex(0);
						m.setConfiguration(existingOA.getConfiguration());
					 
						m.initiateAchievementDate(null);
						m.initiateReachingDate(null);
						m.initiateEarlyDate(null);
						m.setDueDate(null);
					 
						existingOA.addMilestone(m);
						if (getDebugMode()) {
							System.out.println("Milestone:" + m.toString());
						}
						
					}
				 
					try {
						 procInst.startRootProcInst(); 
						 existingOA.setIsProcessStarted(true);
						 
						 caseIdH.value = caseObj.getId();
						 
						 if (getDebugMode()) {
							System.out.println("CaseId generated: " + caseObj.getId());
						}

					}catch (Exception e) {
						 if (getDebugMode()) {
							 System.out.println("ERROR Exception: " + e.toString());
						 }

						 return caseIdH;
					 }
					 
				 }else{

					if (getDebugMode()) {
						System.out.println("ERROR ProcInst not created");
					}

					 
				 }
			}
		}catch(Exception e) {
			 if (getDebugMode()) {
				 System.out.println("ERROR Generic Exception: " + e.toString());
			 }

			 return caseIdH;
		}
		finally {
			try {
				//Liberamos la consulta
		  		result.close();
		  		sqlQuery.close();
			}catch(Exception e) {}
		}
		
		return caseIdH;
	}	
	


	/**
	 * Devuelve un objeto con el detalle del proceso a lanzar
	 * @param conn
	 * @param strProcessName
	 * @return
	 */
    private PcProcessDefinition getProcessDetails(Connection conn, String strProcessName) {
       try {
		    //++paco
			DataManager = new DataManagerCls(settingMap);
			System.out.println("---------------------------");
			System.out.println("objeto DataManager creado OK");
			
			dManagerFactory.set(DataManager);
			System.out.println("---------------------------");
			System.out.println("DataManagerFactoty inicializado OK con el objeto DataManager");
		    //--paco			
			PcProcessDefinitionAug pd = (PcProcessDefinitionAug) PcProcessDefinitionAug.create(IdGen.uniqueId()) ;
            PreparedStatement sqlQuery = null;           
            ResultSet result = null;
            if (getDebugMode()) {
                System.out.println("------------------------------------------------------------------------------------------");
                System.out.println("Getting process definition details: " + strProcessName);
            }            
            try {    
                if (conn == null) {
                    MainClass.openDBConnection("PC"); // abrimos la sesion para PC
                }                
                sqlQuery = (PreparedStatement) conn.prepareStatement(strQueryProcessDef.replace("%1", strProcessName));
                result = sqlQuery.executeQuery();             
                if (result.getFetchSize() == 1) {
                      pd.setcId(result.getString(1));
                      pd.setlineOfBusiness(result.getString(4));
                      pd.setsalesChannel(result.getString(5));
                      pd.setprocessMapAction((ActionType)ActionTypeTP.def.findByCode(result.getString(3)));
                      pd.setspecifiedVersionId(result.getInt(2));
                      
                }
            }catch(Exception e){
                if (getDebugMode()) {
                    System.out.println("------------------------------------------------------------------------------------------");
                    System.out.println("ERROR getting process definition details: " + e.toString());
                }
                return null;               
            }finally{        
                try {
                    //Liberamos la consulta
                      result.close();
                      sqlQuery.close();
                }catch(Exception e) {}                
            }           
            return (PcProcessDefinition) pd;
       }catch(Exception e) {
           if (getDebugMode()) {
                System.out.println("------------------------------------------------------------------------------------------");
                System.out.println("Error initializing PcProcessDefinition " + e.toString());
            }          
            return null;
       }
   }	 
	


	/**
	 *  Devuelve un objeto con el detalle de la orden obtenido de bbdd
	 * @param con
	 * @param orderId
	 * @return
	 */
	private OmOrder getOmOrderDetails(Connection conn, String orderId) {
		OmOrder om = null;
		CallableStatement sqlQuery = null;
		ResultSet result = null;
		
		if (getDebugMode()) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Getting OMOrder details: " + orderId);
		}
		
		try {

			if (conn == null) {
				MainClass.openDBConnection("OMS"); // abrimos la sesion para OMS
			}
			
			//++paco		
			DistributedLockManager lockManager = new DistributedLockManager(settingMap);
			System.out.println("Objeto lockManager creado OK");
			System.out.println("------------------------------");					
			LockManagerFactory lmanagerFactory = null;
			lmanagerFactory.set(lockManager);
			System.out.println("Asignacion al LockManagerFactory el objeto lockManager OK");
			System.out.println("------------------------------");			
			//--paco				
			
	  		om = (OmOrder) OmOrder.create(getStrIDContract());
			System.out.println("OmOrder.Create OK");
	  		om.setclfyOrderIdVal(getStrIDContract());
			System.out.println("setclfyOrderIdVal OK");



			sqlQuery = (CallableStatement) conn.prepareStatement(strQueryOmsOrder.replace("%1", orderId));
			result = sqlQuery.executeQuery();
			
			if (result.getFetchSize() == 1) {

	  			om.setorderMode((DynOrderMode) DynOrderModeTP.def.findByCode(result.getString(2)));
	  			om.setgroupId(result.getString(3));
	  			om.setrootCustomerId(result.getString(4));
	  			om.setorderStatus((OrderStatus) OrderStatusTP.def.findByCode(result.getString(5)));
	  			om.setopportunityId(result.getString(6));
	  			om.setcurrentSalesChannel(result.getString(7));
	  			om.setdealerCode(result.getString(8));
	  			om.setaddressId(result.getString(9));
	  			om.setisCreatedAnonymous(false);
	      		
	      		OmOrderHolder h = new OmOrderHolder();
	      		h.setValue(om);
	      		
	      		OmOrderAddition.createNewOrderAddition(orderId, h);
	      		om.setORDER_ADDITION(((OmOrder) h.getValue()).getORDER_ADDITION()); 
	      		
	      		om.setapplicationDate( result.getDate(13));
	      		om.setcontactId(result.getString(16));
	      		om.setcreationDate(result.getDate(12));
	      		om.setcustomerId(result.getString(17));
	      		om.setcustOrderReference(result.getString(15));
	      		om.setdepositID(result.getString(19));
	      		om.setDueDate(result.getDate(14));
	      		om.setextReferenceNum(result.getString(10));
	      		om.setisSaved(true);
	      		om.setorderUnitId(result.getString(20));
	      		om.setorderUnitType((OrderUnitType) OrderUnitTypeTP.def.findByCode(result.getString(21)));
	      		om.setpriority(result.getInt(22));
	      		om.setproposalExpiryDate(result.getDate(23));
	      		om.setrecontact_in_month(result.getInt(24));
	      		om.setrecontact_in_year(result.getInt(25));
	      		om.setrecontactPeriod((RecontactPeriod) RecontactPeriodTP.def.findByCode(result.getString(26)));
	      		om.setreferenceNumber(orderId);
	      		om.setsalesChannel(result.getString(7));
	      		om.setserviceRequiredDate(result.getDate(11));
	      		om.setsourceOrder(result.getString(27));

	  		}
	  		
		}catch (Exception e){
			if (getDebugMode()) {
				System.out.println("------------------------------------------------------------------------------------------");
				System.out.println("ERROR getting OMOrder details: " + e.toString());
			}
			
			return null;
			
	    }finally{
	    	try {
		    	result.close();
		    	sqlQuery.close();
		    	
	    	}catch(Exception e) {}
	    	
	    }
		
		return om;
	}
	/**
	 * Recupera los datos de Order
	 * @param om  --> OMOrder desde la propiedad this.order
	 * @return Order
	 */
	private Order getInputOrderDetails(OmOrder om) {

		Order o = new Order();
		
		if (getDebugMode()) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Getting Order details: " + om.getId());
		}
		
		try {
			//definimos los objetos que necesitaremos para generar inputOrder
			OrderID oid = null;
			oid.setOrderID(om.getId());
			
			SalesChannelRVT sc = new SalesChannelRVT(om.getsalesChannel());
			
			OrderUserActionRVT ouaRVT = null;
			OrderUserAction[] oua = null;
			oua[0].setAction(ouaRVT);
			oua[0].setAllowed(true);
			oua[0].setRelinquishChannels(0, sc);
	
			OrderDetails od = null;			
			od.setServiceRequiredDate(om.getserviceRequiredDate());
			od.setCreationDate(om.getcreationDate());
			od.setApplicationDate(om.getapplicationDate());
			od.setExpiryDate(om.getDueDate());
			od.setOrderStatus(new OrderStatusRVT(om.getOrderStatusVal().getCode()));
			od.setOrderMode(new OrderModeRVT(om.getorderMode().getCode()));
			od.setSalesChannel(sc);
			od.setExternalOrderID(om.getextReferenceNum());
			od.setCustomerOrderID(om.getcustOrderReference());
			od.setCurrentSalesChannel(sc);

			PersonID pid = null;
			pid.setId(om.getcontactId());

			PersonHeader ph = null;
			ph.setPersonID(pid);
			ph.setId(pid.getId());
			
			// formamos el objeto Order
			o.setOrderDetails(od);
			o.setOrderID(oid);
			o.setAvailableUserActions(oua);
			o.setAnonymous(false);
			o.setAddressIDX9(om.getaddressId());
			o.setBiometricCheckValidatedSuccessfullyX9(true); //Esta accion del proceso pasara siempre como completada
			o.setExternalOrderIdX11(om.getextReferenceNum());
			o.setContact(ph);
			o.setAddressIDX9(om.getaddressId());
			o.setExternalOrderIdX9(om.getextReferenceNum());
			
		}catch(Exception e) {
			
			if (getDebugMode()) {
				System.out.println("------------------------------------------------------------------------------------------");
				System.out.println("ERROR getting Order details: " + e.toString());
			}
			
			return null;
		}
		
		return o;
	}
	


	/**
	 * Recupera los datosde StartOrderInput
	 * @param sqlQuery  --> consulta sobre OMS
	 * @param orderId   --> id de orden
	 * @return StartOrderInput
	 */
	private StartOrderInput getStartOrderInput(Connection conn, String orderId) {
		
		StartOrderInput sti = new StartOrderInput();

		CallableStatement sqlQuery = null;
		ResultSet result = null;
		
		if (getDebugMode()) {
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Getting StartOrderInput details: " + orderId);
		}

		try{
			
			if (conn == null) {
				MainClass.openDBConnection("OMS"); // abrimos la sesion para OMS
			}

			sqlQuery = (CallableStatement) conn.prepareStatement(strQueryOmsOrder.replace("%1", orderId));
			result = sqlQuery.executeQuery();
			
			if (result.getFetchSize() > 0) {			

				OrderActionData[] oad = null;
				OmOrderAction[] oma = null;
				OmOrderAction omoa = null;
				int iConta = 0;
				
				while (result.next()) {

					DynamicAttribute[] dynAtr = null;
					String strDynamic = result.getString(5);
					
					if (!"".equals(strDynamic) && strDynamic != null) {
						
						String[] values = strDynamic.split("=");
						if (!"".equals(values[0]) && values[0] !=null) {
							dynAtr[0].setName(values[0]);
							dynAtr[0].setValue(values[1]);
						}
					}
					
					//formamos el objeto OrderAction
					OrderAction oaInfo = null;
					
					OrderActionID oaID = null;
					oaID.setOrderActionID(result.getString(1));
					oaInfo.setOrderActionID(oaID);

					//OrderActionDetails
					OrderActionDetails oaDet = null;
					
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
					
					OrganizationID oID = null;
					oID.setId(null); // Pendiente de buscar el valor a usar
					oaDet.setOrganizationID(oID); 
					
					//OrderActionUserAction
					OrderActionUserAction[] ouAct = null;
					OrderActionUserActionRVT oauAct = null;
					ouAct[0].setAction(oauAct);
					
					//OrderHeader
					OrderHeader oh = null;

					oh.setOrderID(this.inputOrder.getOrderID());
					oh.setOrderMode(this.inputOrder.getOrderDetails().getOrderMode());
					oh.setOrderStatus(this.inputOrder.getOrderDetails().getOrderStatus());
					oh.setApplicationDate(this.inputOrder.getOrderDetails().getApplicationDate());
					oh.setServiceRequiredDate(this.inputOrder.getOrderDetails().getServiceRequiredDate());
					oh.setSalesChannel(this.inputOrder.getOrderDetails().getSalesChannel());
					oh.setExpiryDate(this.inputOrder.getOrderDetails().getExpiryDate());
					oh.setCustomerOrderID(this.inputOrder.getOrderDetails().getCustomerOrderID());
					oh.setExternalOrderID(this.inputOrder.getOrderDetails().getExternalOrderID());
					oh.setAvailableUserActions(this.inputOrder.getAvailableUserActions());
					oh.setSalesChannel(this.inputOrder.getOrderDetails().getCurrentSalesChannel());
					oh.setLocked(true);
					oh.setAnonymous(false);
					
					oh.setOrderRetrievalCriteria(dynAtr);
					oh.setCustomerAgreedToPayDeposit(false);

					// Rellenamos el objeto OrderAction
					oaInfo.setOrderActionID(oaID);
					oaInfo.setOrderActionDetails(oaDet);
					oaInfo.setAvailableUserActions(ouAct);
					oaInfo.setOrderHeader(oh);

					// OrderActionData
					oad[iConta].setOrderActionInfo(oaInfo);
					oad[iConta].setDynamicAttributes(dynAtr);
					
					// Generamos la info de OMOrderAction con la misma consulta
					omoa = null;
		      		
		      		omoa.setApId(result.getString(7));
		      		
		      		ApItem api = null;					      		
		      		api.setAPId(result.getString(7));
		      		api.setVersionIdVal(result.getString(10));
		      		omoa.setAPITEM_REL(api);
		      		
		      		omoa.setapplicationDate(result.getDate(16));
		      		omoa.setApplicationReferenceIdVal(result.getString(32));
		      		omoa.setApVersionId(result.getString(10));					      		
		      		omoa.setBpNameVal(this.getStrProcessName()); //Nombre del proceso a ejecutar!
		      		
		      		omoa.setconfigOA(ConfigurationTP.R);
		      		omoa.setcontactId(result.getString(18));
		      		omoa.setcreationDate(result.getDate( 26));
		      		omoa.setcreatorId(result.getString(21));
		      		omoa.setcustomerId(result.getString(19));
		      		omoa.setcustOrderReference(result.getString(31));
		      		omoa.setDueDate(result.getDate( 6));
		      		omoa.setearlyDate(result.getDate(33));
		      		omoa.setextReferenceNum(result.getString(25));
		      		omoa.setextReferenceRemark(result.getString(34));
		      		omoa.setInitialProcessDefinition(this.getProcessDef());
		      		omoa.setisActive(BooleanValTP.TRUE);
		      		omoa.setisMain((BooleanVal) BooleanValTP.def.findByCode(result.getString(9)));
		      		omoa.setIsProcessStarted(false);
		      		omoa.setLanguageVal((LanguageCode) LanguageCodeTP.def.findByCode(result.getString(12)));
		      		omoa.setItemPartitionKeyVal(result.getInt(35));
		      		omoa.setorder(this.order);
		      		omoa.setorderActionStatus((OrderActionStatus) OrderActionStatusTP.def.findByCode(result.getString(4)));
		      		omoa.setorderUnitId(result.getString(1));
		      		omoa.setorderTypeName((ActionType) ActionTypeTP.def.findByCode(result.getString(5)));
		      		omoa.setorderUnitType(this.order.getorderUnitType());
		      		omoa.setorganizationOwnerId(result.getString(17));
		      		omoa.setparentOrderUnitId(result.getString(2));
		      		omoa.setparentRelation((OrderActionParentRelation) OrderActionParentRelationTP.def.findByCode(result.getString(8)));
		      		omoa.setpriority(result.getInt(15));
		      		omoa.setPrivsKeyVal(result.getString(36));
		      		omoa.setQSequenceNumVal(result.getInt(37));
		      		omoa.setQuantityVal(result.getInt(38));
		      		omoa.setreasonFreeText(result.getString(39));
		      		omoa.setreasonId(result.getString(28));
		      		omoa.setcustomerWillReContactInd((1 == result.getInt(40)));
		      		omoa.setrecontactPeriod((RecontactPeriod)RecontactPeriodTP.def.findByCode(result.getString(41)));
		      		omoa.setrecontact_in_month(result.getInt(42));
		      		omoa.setrecontact_in_year(result.getInt(43));
		      		omoa.setreferenceNumber(result.getString(20));
		      		omoa.setreplacedOfferApID(result.getString(44));
		      		omoa.setrequestLineId(result.getString(45));
		      		omoa.setSalesChannelVal(result.getString(27));					      		
		      		omoa.setserviceRequiredDate(this.order.getserviceRequiredDate());
		      		
		      		//sumamos la OA al array
		      		oma[iConta] = omoa;
		      		iConta++;
					
				}
				
	      		this.order.setOMORDER_ACTION_CHILDs(EpiCollections.singletonTypedListSet(OmOrderAction.class, oma));
				
				// StartOrderInput
	      		sti.setOrder(this.inputOrder);
	      		sti.setOrderActionsData(oad);
	      		sti.setConfirmationChecksApproved(true);
	      		sti.setMarkOrderAsSaved(true);
				
			}
			
		}catch (Exception e){
			if (getDebugMode()) {
				System.out.println("------------------------------------------------------------------------------------------");
				System.out.println("ERROR getting StartOrderInput details: " + e.toString());
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
	
	
	
	public OutputParamsLaunchOrder execProcess() {
		
		
		return(getM_output());
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
	
}
